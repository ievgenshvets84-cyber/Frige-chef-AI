package com.example.data.local

import com.example.data.model.Ingredient
import com.example.data.model.Recipe

object DefaultRecipes {

    val initialStarterIngredients = listOf(
        Ingredient(
            name = "Eier",
            category = "Milch & Eier",
            emoji = "🥚",
            quantity = "4 Stück",
            shelfLifeDays = 2,
            isUrgent = false
        ),
        Ingredient(
            name = "Käse (Gouda)",
            category = "Milchprodukte",
            emoji = "🧀",
            quantity = "150g",
            shelfLifeDays = 3,
            isUrgent = false
        ),
        Ingredient(
            name = "Tomaten",
            category = "Gemüse",
            emoji = "🍅",
            quantity = "3 Stück",
            shelfLifeDays = 1,
            isUrgent = true // Today!
        ),
        Ingredient(
            name = "Milch",
            category = "Milchprodukte",
            emoji = "🥛",
            quantity = "500ml",
            shelfLifeDays = 1,
            isUrgent = true // Open milk, today!
        ),
        Ingredient(
            name = "Paprika",
            category = "Gemüse",
            emoji = "🫑",
            quantity = "1 Stück",
            shelfLifeDays = 4,
            isUrgent = false
        ),
        Ingredient(
            name = "Zwiebeln",
            category = "Gemüse",
            emoji = "🧅",
            quantity = "2 Stück",
            shelfLifeDays = 10,
            isUrgent = false
        )
    )

    fun getRecipeCatalog(): List<Recipe> = listOf(
        Recipe(
            id = "rec_frittata",
            title = "Zero-Waste Pfannen-Frittata",
            description = "Die ultimative Reste-Verwertung: Schmeckt fluffig, herzhaft und nutzt fast alles aus dem Gemüse- und Käsefach.",
            prepTimeMinutes = 12,
            difficulty = "Super Einfach",
            calories = 340,
            servings = 2,
            usedIngredients = listOf("Eier", "Käse (Gouda)", "Tomaten", "Paprika", "Zwiebeln"),
            missingIngredients = listOf("Olivenöl", "Salz & Pfeffer"),
            urgentIngredientsSaved = listOf("Tomaten", "Eier"),
            zeroWasteScore = 98,
            matchPercentage = 100,
            emojiHero = "🍳",
            tags = listOf("Express <15 Min", "Zero-Waste Hero", "Low Carb", "Vegetarisch"),
            steps = listOf(
                "Zwiebeln und Tomaten in feine Würfel schneiden, Paprika in Streifen teilen.",
                "Eier in einer Schüssel mit einer Prise Salz und Pfeffer verquirlen, optional einen Schluck Milch zugeben.",
                "Etwas Öl in der Pfanne erhitzen, Gemüse 3 Minuten anschwitzen.",
                "Ei-Mischung darübergießen, geriebenen Käse darauf verteilen und bei mittlerer Hitze 5-7 Minuten stocken lassen.",
                "Direkt aus der Pfanne warm genießen!"
            )
        ),
        Recipe(
            id = "rec_tomatensuppe",
            title = "Cremige Express-Tomaten-Käse-Suppe",
            description = "Perfekt, um reife Tomaten und offene Milch oder Sahne in 15 Minuten in Seelenfutter zu verwandeln.",
            prepTimeMinutes = 15,
            difficulty = "Einfach",
            calories = 290,
            servings = 2,
            usedIngredients = listOf("Tomaten", "Milch", "Zwiebeln", "Käse (Gouda)"),
            missingIngredients = listOf("Basilikum", "Olivenöl"),
            urgentIngredientsSaved = listOf("Tomaten", "Milch"),
            zeroWasteScore = 95,
            matchPercentage = 100,
            emojiHero = "🥣",
            tags = listOf("Express <15 Min", "Warm & Seelenfutter", "Zero Waste"),
            steps = listOf(
                "Zwiebeln grob hacken und in einem Topf mit etwas Öl glasig dünsten.",
                "Reife Tomaten würfeln und für 5 Minuten im Topf einkochen lassen.",
                "Milch dazugeben, mit Salz und Pfeffer würzen und kurz aufkochen.",
                "Mit dem Pürierstab fein pürieren, mit Käseflocken garnieren und schmelzen lassen."
            )
        ),
        Recipe(
            id = "rec_grilled_cheese",
            title = "Gourmet Panini Grilled Cheese",
            description = "Kross gebackenes Käsebrot mit saftigen Tomaten und gebräunten Zwiebeln – knusprig und unwiderstehlich.",
            prepTimeMinutes = 10,
            difficulty = "Einfach",
            calories = 410,
            servings = 1,
            usedIngredients = listOf("Käse (Gouda)", "Tomaten", "Zwiebeln"),
            missingIngredients = listOf("Brot/Toast", "Butter"),
            urgentIngredientsSaved = listOf("Tomaten"),
            zeroWasteScore = 88,
            matchPercentage = 90,
            emojiHero = "🥪",
            tags = listOf("Express <15 Min", "Knusprig", "Snack"),
            steps = listOf(
                "Brot- oder Toastscheiben von außen dünn mit Butter bestreichen.",
                "Innen mit reichlich Käse, Tomatenscheiben und Zwiebelringen belegen.",
                "In einer heißen Pfanne von beiden Seiten 3-4 Minuten goldbraun rösten, bis der Käse schmilzt."
            )
        ),
        Recipe(
            id = "rec_pasta_primavera",
            title = "Kühlschrank-Pasta Primavera",
            description = "Schnelle Pasta-Pfanne mit karamellisiertem Gemüse und herzhaft geschmolzenem Käse.",
            prepTimeMinutes = 20,
            difficulty = "Einfach",
            calories = 520,
            servings = 2,
            usedIngredients = listOf("Paprika", "Tomaten", "Zwiebeln", "Käse (Gouda)"),
            missingIngredients = listOf("Nudeln", "Olivenöl"),
            urgentIngredientsSaved = listOf("Tomaten"),
            zeroWasteScore = 92,
            matchPercentage = 85,
            emojiHero = "🍝",
            tags = listOf("Normal <30 Min", "Pasta Lovers", "Familien-Hit"),
            steps = listOf(
                "Nudeln in reichlich Salzwasser bissfest kochen.",
                "Währenddessen Paprika, Zwiebeln und Tomaten in einer Pfanne 6 Minuten anbraten.",
                "Gekochte Pasta mit 2 EL Nudelwasser zum Gemüse geben.",
                "Käse unterheben, bis sich eine sämige Soße bildet."
            )
        ),
        Recipe(
            id = "rec_shakshuka",
            title = "Zero-Waste Shakshuka",
            description = "Würzige Tomaten-Paprika-Pfanne mit pochierten Eiern. Einer der besten Wege, Gemüse vor der Tonne zu bewahren!",
            prepTimeMinutes = 22,
            difficulty = "Mittel",
            calories = 360,
            servings = 2,
            usedIngredients = listOf("Tomaten", "Paprika", "Zwiebeln", "Eier"),
            missingIngredients = listOf("Kreuzkümmel", "Knoblauch", "Olivenöl"),
            urgentIngredientsSaved = listOf("Tomaten", "Eier"),
            zeroWasteScore = 96,
            matchPercentage = 85,
            emojiHero = "🥘",
            tags = listOf("Normal <30 Min", "Würzig", "High Protein"),
            steps = listOf(
                "Zwiebeln und Paprika klein würfeln und in einer Pfanne weich dünsten.",
                "Tomaten zerkleinern, dazugeben und 10 Minuten zu einer dicklichen Soße einköcheln.",
                "Mit einem Löffel kleine Mulden in die Soße formen und Eier hineingleiten lassen.",
                "Deckel auflegen und bei milder Hitze ca. 6 Minuten stocken lassen, bis das Eiweiß fest ist."
            )
        ),
        Recipe(
            id = "rec_auflauf",
            title = "Bunter Reste-Gemüse-Auflauf",
            description = "Herzhaft überbacken aus dem Ofen – schluckt fast jedes Gemüse und schmeckt der ganzen Familie.",
            prepTimeMinutes = 35,
            difficulty = "Mittel",
            calories = 460,
            servings = 3,
            usedIngredients = listOf("Paprika", "Tomaten", "Zwiebeln", "Eier", "Milch", "Käse (Gouda)"),
            missingIngredients = listOf("Muskatnuss", "Salz & Pfeffer"),
            urgentIngredientsSaved = listOf("Tomaten", "Milch", "Eier"),
            zeroWasteScore = 99,
            matchPercentage = 100,
            emojiHero = "🍲",
            tags = listOf("Ausführlich >30 Min", "Ofengericht", "Mega Zero-Waste"),
            steps = listOf(
                "Gemüse in mundgerechte Stücke schneiden und in eine Auflaufform geben.",
                "Eier mit Milch, Salz, Pfeffer und einer Prise Muskat kräftig verquirlen.",
                "Den Guss über das Gemüse gießen und mit dem geriebenen Käse bestreuen.",
                "Im vorgeheizten Backofen bei 180°C ca. 25-30 Minuten goldbraun überbacken."
            )
        )
    )

    fun matchRecipes(availableIngredients: List<Ingredient>): List<Recipe> {
        val ingredientNames = availableIngredients.map { it.name.lowercase().trim() }
        val urgentNames = availableIngredients
            .filter { it.isUrgent || it.shelfLifeDays <= 1 }
            .map { it.name.lowercase().trim() }

        return getRecipeCatalog().map { recipe ->
            val usedInRecipe = recipe.usedIngredients.filter { req ->
                ingredientNames.any { it.contains(req.lowercase()) || req.lowercase().contains(it) }
            }
            val missingInRecipe = recipe.usedIngredients.filterNot { req ->
                ingredientNames.any { it.contains(req.lowercase()) || req.lowercase().contains(it) }
            }
            val urgentSaved = recipe.usedIngredients.filter { req ->
                urgentNames.any { it.contains(req.lowercase()) || req.lowercase().contains(it) }
            }

            val totalReq = recipe.usedIngredients.size
            val matchPct = if (totalReq > 0) ((usedInRecipe.size.toFloat() / totalReq) * 100).toInt() else 100
            val zeroWasteBonus = if (urgentSaved.isNotEmpty()) 20 else 0
            val calculatedScore = (matchPct * 0.8f + zeroWasteBonus).coerceIn(40f, 100f).toInt()

            recipe.copy(
                usedIngredients = if (usedInRecipe.isNotEmpty()) usedInRecipe else recipe.usedIngredients,
                missingIngredients = missingInRecipe,
                urgentIngredientsSaved = urgentSaved,
                matchPercentage = matchPct.coerceAtMost(100),
                zeroWasteScore = calculatedScore
            )
        }.sortedWith(
            // Sort by: Zero Waste urgency first, then match %, then prep time
            compareByDescending<Recipe> { it.urgentIngredientsSaved.size }
                .thenByDescending { it.matchPercentage }
                .thenBy { it.prepTimeMinutes }
        )
    }
}
