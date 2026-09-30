package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRecipeGeneratorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateCustomRecipe(
        selectedIngredients: List<String>,
        cookingStyle: String = "BELIEBIG",
        dietary: String = "ALLES"
    ): Result<Recipe> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiRecipe", "No valid GEMINI_API_KEY; using smart culinary chef engine.")
            return@withContext Result.success(generateSmartLocalChefRecipe(selectedIngredients, cookingStyle, dietary))
        }

        try {
            val styleInstruction = when (cookingStyle) {
                "PFANNE" -> "Kreation als knuspriges oder geschmortes Pfannengericht mit schönen Röstaromen."
                "PASTA" -> "Kreation rund um ein aromatisches Nudel- oder Pastagericht mit sämiger Soßenbindung."
                "OFEN" -> "Kreation als goldbrauner Ofenauflauf, Gratin oder Blechgericht."
                "SUPPE" -> "Kreation als sämige Seelenschmeichler-Suppe oder Eintopf."
                "EXPRESS" -> "Kreation als Turbo-Gericht in maximal 12 Minuten mit maximalem Geschmack."
                "LEICHT" -> "Kreation als frisches, leichtes, aromatisches Gericht."
                else -> "Wähle die für die Zutaten geschmacklich beste und kreativste Zubereitungsart."
            }

            val dietInstruction = when (dietary) {
                "VEGETARISCH" -> "Das Gericht MUSS strikt vegetarisch sein (kein Fleisch/Fisch)."
                "LOW_CARB" -> "Das Gericht soll kohlenhydratarm und reich an gesunden Fetten/Proteinen sein."
                "PROTEIN" -> "Fokus auf hohen Proteingehalt und gute Sättigung."
                else -> ""
            }

            val prompt = """
                Du bist ein leidenschaftlicher Meisterkoch und kulinarischer Mentor.
                Der Nutzer hat folgende Zutaten manuell ausgewählt, aus denen du ein authentisches, kreatives Rezept entwickeln sollst:
                ZUTATEN: ${selectedIngredients.joinToString(", ")}
                
                STIL-VORGABE: $styleInstruction
                ERNÄHRUNG: $dietInstruction
                
                WICHTIGE ANWEISUNG (KRITISCH):
                Erstelle KEIN banales oder faules Rezept (wie "alles in die Pfanne hauen und aufwärmen")!
                Entwickle stattdessen ein ECHTES, geschmackvolles Rezept mit authentischen Kochtechniken:
                - Schneide- und Vorbereitungstechniken (z.B. feine Streifen, Würfel, Knoblauch andrücken).
                - Röst- & Anbratprozess (Temperaturführung, Reihenfolge, welcher Geschmack zuerst ins Öl wandert).
                - Soßenemulsion, Ablöschen oder Schmelzvorgang (z.B. Kochwasser, Schmand, Eiermilch, sanftes Köcheln).
                - Feinabstimmung (Kräuter, Zitrone, frischer Pfeffer, Textur).
                - Mindestens 4 bis 5 präzise, meisterhafte Zubereitungsschritte.
                
                Antworte AUSSCHLIESSLICH als gültiges JSON-Objekt ohne Markdown:
                {
                  "title": "Klangvoller, verlockender Rezeptname",
                  "description": "2 appetitanregende Sätze über das Geschmacksprofil und warum diese Kombination funktioniert.",
                  "prepTimeMinutes": 12 bis 30,
                  "difficulty": "Einfach" oder "Mittel",
                  "calories": 300 bis 650,
                  "servings": 2,
                  "usedIngredients": ["Zutat aus Nutzerwunsch 1", "Zutat aus Nutzerwunsch 2"],
                  "missingIngredients": ["Basiszutat aus der Speisekammer wie Olivenöl", "Salz & frisch gemahlener Pfeffer"],
                  "chefTip": "Ein echter Profi-Geheimtipp für die Zubereitung (z.B. 'Pfanne vor dem Servieren von der Herdplatte nehmen...').",
                  "emojiHero": "🍳",
                  "tags": ["Chef-Kreation", "Individuell"],
                  "steps": [
                    "Schritt 1: ...",
                    "Schritt 2: ...",
                    "Schritt 3: ...",
                    "Schritt 4: ..."
                  ]
                }
            """.trimIndent()

            val partsArray = JSONArray().apply {
                put(JSONObject().apply { put("text", prompt) })
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", partsArray)
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiRecipe", "API Call failed (${response.code}): $responseBody")
                return@withContext Result.success(generateSmartLocalChefRecipe(selectedIngredients, cookingStyle, dietary))
            }

            val rootJson = JSONObject(responseBody)
            val candidate = rootJson.optJSONArray("candidates")?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val rawText = content?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""

            val parsedRecipe = parseGeminiRecipe(rawText, selectedIngredients)
            Result.success(parsedRecipe)
        } catch (e: Exception) {
            Log.e("GeminiRecipe", "Error generating custom recipe", e)
            Result.success(generateSmartLocalChefRecipe(selectedIngredients, cookingStyle, dietary))
        }
    }

    private fun parseGeminiRecipe(rawJson: String, selectedIngredients: List<String>): Recipe {
        val clean = rawJson.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val obj = JSONObject(clean)
            val usedList = mutableListOf<String>()
            val usedArr = obj.optJSONArray("usedIngredients")
            if (usedArr != null) {
                for (i in 0 until usedArr.length()) usedList.add(usedArr.getString(i))
            } else {
                usedList.addAll(selectedIngredients)
            }

            val missingList = mutableListOf<String>()
            val missingArr = obj.optJSONArray("missingIngredients")
            if (missingArr != null) {
                for (i in 0 until missingArr.length()) missingList.add(missingArr.getString(i))
            }

            val stepsList = mutableListOf<String>()
            val stepsArr = obj.optJSONArray("steps")
            if (stepsArr != null) {
                for (i in 0 until stepsArr.length()) stepsList.add(stepsArr.getString(i))
            }

            val tagsList = mutableListOf<String>()
            val tagsArr = obj.optJSONArray("tags")
            if (tagsArr != null) {
                for (i in 0 until tagsArr.length()) tagsList.add(tagsArr.getString(i))
            }
            tagsList.add("Individuell kreiert")

            val chefTip = obj.optString("chefTip", "")
            val finalDescription = if (chefTip.isNotBlank()) {
                "${obj.optString("description")}\n\n💡 Chefkoch-Tipp: $chefTip"
            } else {
                obj.optString("description")
            }

            Recipe(
                id = "custom_${System.currentTimeMillis()}",
                title = obj.optString("title", "Kreative Küchen-Kreation"),
                description = finalDescription,
                prepTimeMinutes = obj.optInt("prepTimeMinutes", 18),
                difficulty = obj.optString("difficulty", "Einfach"),
                calories = obj.optInt("calories", 420),
                servings = obj.optInt("servings", 2),
                usedIngredients = usedList.ifEmpty { selectedIngredients },
                missingIngredients = missingList.ifEmpty { listOf("Olivenöl", "Salz & Pfeffer") },
                urgentIngredientsSaved = selectedIngredients.take(2),
                zeroWasteScore = 98,
                matchPercentage = 100,
                steps = stepsList.ifEmpty { defaultChefSteps(selectedIngredients) },
                tags = tagsList,
                emojiHero = obj.optString("emojiHero", "👨‍🍳")
            )
        } catch (e: Exception) {
            Log.e("GeminiRecipe", "Failed parsing Gemini JSON, using local chef engine", e)
            generateSmartLocalChefRecipe(selectedIngredients)
        }
    }

    private fun generateSmartLocalChefRecipe(
        ingredients: List<String>,
        cookingStyle: String = "BELIEBIG",
        dietary: String = "ALLES"
    ): Recipe {
        val lower = ingredients.map { it.lowercase() }
        val hasPasta = lower.any { it.contains("pasta") || it.contains("nudel") || it.contains("spaghetti") }
        val hasEggs = lower.any { it.contains("ei") || it.contains("eier") }
        val hasCheese = lower.any { it.contains("käse") || it.contains("gouda") || it.contains("feta") || it.contains("parmesan") }
        val hasTomatoes = lower.any { it.contains("tomate") }
        val hasZucchini = lower.any { it.contains("zucchini") }

        val title: String
        val emoji: String
        val steps: List<String>
        val prepTime: Int
        val description: String

        when {
            hasPasta -> {
                title = "Mediterrane Pasta Padellata mit geschmolzenen Aromen"
                emoji = "🍝"
                prepTime = 16
                description = "Ein italienischer Klassiker, bei dem frische Zutaten in feinem Olivenöl mit Knoblauch geschmort und mit Nudelwasser zu einer seidigen Emulsion vollendet werden.\n\n💡 Chefkoch-Tipp: Schöpfe 50 ml stärkehaltiges Kochwasser vor dem Abgießen ab – es bindet die Soße perfekt an die Pasta!"
                steps = listOf(
                    "Pasta in reichlich sprudelndem Salzwasser al dente kochen. 1 Kelle stärkehaltiges Nudelwasser vor dem Abgießen auffangen.",
                    "Zutaten vorbereiten: Frisches Gemüse wie Tomaten oder Zucchini in mundgerechte Stücke schneiden, Knoblauch andrücken.",
                    "3 EL Olivenöl in einer großen Pfanne auf mittlerer Hitze erwärmen. Zuerst Knoblauch 1 Minute sanft duften lassen, dann das Gemüse zugeben und 4 Minuten mit einer Prise Meersalz anbraten.",
                    "Die abgetropfte Pasta zusammen mit dem aufgefangenen Kochwasser direkt in die Pfanne schwenken, sodass sich eine glänzende Emulsion bildet.",
                    if (hasCheese) "Von der Hitze nehmen, Käse unterheben und schmelzen lassen. Mit frisch gemahlenem Pfeffer und Kräutern servieren!" else "Mit frischen Kräutern und einem Spritzer Olivenöl vollenden und dampfend heiß anrichten."
                )
            }
            hasEggs && hasCheese -> {
                title = "Goldgelbe Frittata Rustica mit Zartschmelz"
                emoji = "🍳"
                prepTime = 14
                description = "Fluffig aufgeschlagene Eier umhüllen sanft gebratenes Pfannengemüse, während geschmolzener Käse für herzhaften Schmelz sorgt.\n\n💡 Chefkoch-Tipp: Bei milder Hitze zugedeckt stocken lassen, damit der Boden knusprig wird, das Innere aber saftig und seidig bleibt!"
                steps = listOf(
                    "Eier in einer Schüssel mit einer Prise Meersalz, frischem Pfeffer und 2 EL Wasser oder Milch kräftig schaumig verquirlen.",
                    "Gemüse in feine, gleichmäßige Würfel oder Scheiben schneiden, damit alles die gleiche Garzeit hat.",
                    "Etwas Butter oder Olivenöl in einer beschichteten Pfanne aufschäumen lassen. Das Gemüse 3 Minuten bei mittlerer Hitze anschwitzen.",
                    "Die Eiermischung gleichmäßig darübergießen und die Hitze sofort auf kleine Stufe reduzieren. Mit geriebenem oder gewürfeltem Käse bestreuen.",
                    "Einen Deckel auflegen und die Frittata ca. 6-8 Minuten sanft stocken lassen. In Tortenstücke schneiden und warm genießen."
                )
            }
            hasZucchini || hasTomatoes -> {
                title = "Aromatisches Schmorgemüse aus der Pfanne mit Kräutern"
                emoji = "🥘"
                prepTime = 18
                description = "Gemüse auf den Punkt gegart: Außen mit verführerischen Röstaromen, innen saftig und voll natürlicher Süße.\n\n💡 Chefkoch-Tipp: Das Gemüse in heißem Öl zunächst 2 Minuten unberührt anbraten lassen – so entstehen echte Karamellaromen!"
                steps = listOf(
                    "Gemüse waschen, trockentupfen und in mundgerechte Stücke teilen (Zucchini in Halbmonde, Tomaten halbieren).",
                    "Pfanne mit hochwertigem Pflanzenöl sehr heiß werden lassen. Gemüse hineingeben und 2 Minuten ohne Wenden scharf anrösten.",
                    "Hitze reduzieren, mit einer Prise Salz, Pfeffer und optionalen Kräutern würzen. Weitere 4 Minuten unter gelegentlichem Schwenken bissfest garen.",
                    if (hasCheese) "Käse darüberbröckeln, Pfanne von der Platte nehmen und 2 Minuten zugedeckt schmelzen lassen." else "Mit einem Spritzer Zitrone oder Balsamico ablöschen und sofort anrichten."
                )
            }
            else -> {
                title = "Kreative Chef-Pfanne mit ${ingredients.take(2).joinToString(" & ")}"
                emoji = "✨"
                prepTime = 15
                description = "Perfekt ausbalancierte Alltagsküche: Gezielte Garstufen heben den Eigengeschmack jeder Zutat hervor.\n\n💡 Chefkoch-Tipp: Gewürze erst gegen Ende des Bratens zugeben, damit sie nicht verbrennen, sondern ihre ätherischen Öle entfalten."
                steps = defaultChefSteps(ingredients)
            }
        }

        return Recipe(
            id = "custom_local_${System.currentTimeMillis()}",
            title = title,
            description = description,
            prepTimeMinutes = prepTime,
            difficulty = "Einfach",
            calories = 390,
            servings = 2,
            usedIngredients = ingredients,
            missingIngredients = listOf("Olivenöl / Butter", "Salz & schwarzer Pfeffer"),
            urgentIngredientsSaved = ingredients.take(2),
            zeroWasteScore = 98,
            matchPercentage = 100,
            steps = steps,
            tags = listOf("Individuell kreiert", "Chefkoch-Methode", "Zero Waste"),
            emojiHero = emoji
        )
    }

    private fun defaultChefSteps(ingredients: List<String>): List<String> {
        return listOf(
            "Vorbereitung: Die Zutaten gründlich waschen, trocken tupfen und in mundgerechte, gleichmäßige Stücke schneiden.",
            "Aromen ansetzen: 2 EL Olivenöl oder Butter in einer heißen Pfanne erhitzen. Feste Zutaten zuerst 3-4 Minuten anbraten, bis feine Röstaromen entstehen.",
            "Garpunkt & Verfeinerung: Zartere Zutaten hinzugeben, Hitze reduzieren und alles 3-5 Minuten sanft durchziehen lassen.",
            "Abschmecken: Mit Meersalz, frisch gemahlenem Pfeffer und Kräutern nach Wahl harmonisch abschmecken.",
            "Servieren: Auf vorgewärmten Tellern anrichten und heiß genießen!"
        )
    }
}
