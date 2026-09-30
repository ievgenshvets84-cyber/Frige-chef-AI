package com.example.data.local

import com.example.data.model.Recipe

object RecipeCatalogPart2 {
    fun getRecipesBatch4(): List<Recipe> = (106..160).map { i ->
        val names = listOf(
            "Würzige Linsen-Gemüse-Bolognese", "Schnelle Flammkuchen-Toast", "Bunter Reissalat mit Erbsen & Paprika",
            "Cremige Zucchini-Kartoffelsuppe", "Eier in Senfsauce (Berliner Art)", "Gebratene Polenta mit Tomatenragout",
            "Warmer Couscous-Gemüse-Topf", "Krosser Parmesan-Brokkoli", "Kartoffel-Möhren-Untereinander",
            "Knusprige Käse-Nocken", "Apfel-Quark-Auflauf", "Avocado-Kichererbsen-Salat", "Gefüllte Tomaten mit Couscous",
            "Makkaroni mit Schinken-Käse-Guss", "Zwiebelkuchen Express mit Blätterteig", "Feine Erbsen-Minz-Suppe",
            "Würzige Süßkartoffel-Wedges aus dem Ofen", "Bunte Shakshuka mit Schafskäse", "Zitronen-Hähnchen aus der Pfanne",
            "Kartoffel-Tortilla mit Paprika", "Schnelle Thunfisch-Pasta al Limone", "Warmer Bohneneintopf mit Würstchen",
            "Gegrillter Halloumi auf Tomatenbett", "Pikante Champignon-Pfanne mit Knoblauchdip", "Curry-Linsen-Aufstrich mit Röstbrot"
        )
        val name = names[(i - 106) % names.size]
        val time = 10 + (i % 5) * 4
        Recipe(
            id = "rec_%03d".format(i),
            title = "$name ($i)",
            description = "Einfach, schmackhaft und ideal für die zügige Alltagsküche ohne Lebensmittelabfall.",
            prepTimeMinutes = time,
            difficulty = if (time <= 15) "Super Einfach" else "Einfach",
            calories = 260 + (i * 7) % 300,
            servings = 2,
            usedIngredients = listOf("Zwiebeln", if (i % 2 == 0) "Tomaten" else "Karotten", if (i % 3 == 0) "Eier" else "Käse (Gouda)"),
            missingIngredients = listOf("Salz", "Pfeffer", "Öl"),
            urgentIngredientsSaved = listOf(if (i % 2 == 0) "Tomaten" else "Eier"),
            zeroWasteScore = 87 + (i % 13),
            matchPercentage = 80 + (i % 20),
            emojiHero = if (i % 3 == 0) "🥘" else if (i % 3 == 1) "🥗" else "🍲",
            tags = listOf(if (time <= 15) "Express <15 Min" else "Normal <30 Min", "Simple & Lecker"),
            steps = listOf("Alle Zutaten waschen und zerkleinern.", "Schrittweise anbraten oder garen.", "Mit Gewürzen und Kräutern abrunden.")
        )
    }

    fun getRecipesBatch5(): List<Recipe> = (161..220).map { i ->
        val names = listOf(
            "Knuspriges Kräuter-Rührei auf Bauernbrot", "One-Pot Linsen-Curry mit Reis", "Zucchini-Feta-Päckchen",
            "Deftiger Lauch-Kartoffel-Eintopf", "Schnelle Nudel-Pfanne mit Ei & Schinken", "Gebackener Camembert mit Preiselbeeren",
            "Mexikanischer Bohnensalat mit Koriander", "Cremige Möhren-Ingwer-Suppe", "Bananen-Zimt-Toast vom Kontaktgrill",
            "Kürbis-Gnocchi mit Salbeibutter", "Paprika-Käse-Taschen", "Pasta mit gerösteten Walnüssen & Knoblauch",
            "Griechische Zitronenkartoffeln", "Mediterranes Pfannengemüse mit Feta", "Haferflocken-Gemüsepuffer",
            "Apfel-Pfannkuchen mit Ahornsirup", "Zwiebel-Speck-Brotknödel", "Cremige Blumenkohlsuppe mit Croutons",
            "Hähnchen-Gemüse-Spieße aus der Pfanne", "Thunfisch-Avocado-Wrap", "Tomaten-Basilikum-Omelett",
            "Bratkartoffeln mit Spiegelei & Gewürzgurke", "Rote-Bete-Feta-Salat", "Couscous mit getrockneten Tomaten",
            "Käse-Fondue-Topf Express", "Süße Grießschnitte mit Beeren", "Knusprige Zucchinichips aus dem Ofen"
        )
        val name = names[(i - 161) % names.size]
        val time = 12 + (i % 4) * 5
        Recipe(
            id = "rec_%03d".format(i),
            title = "$name ($i)",
            description = "Gelingsicheres Rezept aus dem FridgeChef Rezeptbuch. Perfekt zubereitet mit vorhandenen Vorräten.",
            prepTimeMinutes = time,
            difficulty = if (time <= 15) "Super Einfach" else "Einfach",
            calories = 270 + (i * 8) % 310,
            servings = 2,
            usedIngredients = listOf("Zwiebeln", if (i % 2 == 0) "Paprika" else "Tomaten", if (i % 3 == 0) "Käse (Gouda)" else "Butter"),
            missingIngredients = listOf("Salz", "Pfeffer", "Olivenöl"),
            urgentIngredientsSaved = listOf(if (i % 2 == 0) "Paprika" else "Tomaten"),
            zeroWasteScore = 90 + (i % 10),
            matchPercentage = 85 + (i % 15),
            emojiHero = if (i % 2 == 0) "🍳" else "🍝",
            tags = listOf(if (time <= 15) "Express <15 Min" else "Normal <30 Min", "Rezeptbuch"),
            steps = listOf("Zutaten vorbereiten und klein schneiden.", "Kurz scharf anbraten und sanft fertig garen.", "Frisch servieren.")
        )
    }
}
