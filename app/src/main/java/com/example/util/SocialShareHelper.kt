package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Recipe
import java.io.File
import java.io.FileOutputStream

object SocialShareHelper {

    fun generateShareText(recipe: Recipe): String {
        return """
            🍳 Ich habe aus meinem fast leeren Kühlschrank das hier gezaubert: ${recipe.title}! ✨
            
            🌱 Zero-Waste Score: ${recipe.zeroWasteScore}% gerettet
            ⏱️ Dauer: ${recipe.prepTimeMinutes} Minuten
            🥗 Verwertete Zutaten: ${recipe.usedIngredients.take(4).joinToString(", ")}
            
            Gemacht mit FridgeChef AI 🌿
            
            #ZeroWasteCook #FridgeScan #CookingHacks #WhatICooked #FridgeChefAI #ResteEssen
        """.trimIndent()
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FridgeChef Recipe", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text mit Hashtags in Zwischenablage kopiert! 📋", Toast.LENGTH_SHORT).show()
    }

    /**
     * Generates a 9:16 high-resolution vertical Story card bitmap (1080 x 1920)
     */
    fun createStoryCardBitmap(recipe: Recipe): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient Background (Deep Culinary Green to Dark Forest)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(0xFF132A13.toInt(), 0xFF1E3A20.toInt(), 0xFF0D180E.toInt()),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Top App Header Badge
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33FFFFFF
        }
        val pillRect = RectF( width / 2f - 220f, 110f, width / 2f + 220f, 180f)
        canvas.drawRoundRect(pillRect, 35f, 35f, pillPaint)

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🍳 FridgeChef AI • Story", width / 2f, 158f, headerTextPaint)

        // Central Glass Card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xE6223B24.toInt()
        }
        val cardRect = RectF(70f, 240f, width - 70f, height - 260f)
        canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)

        // Border around card
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = 0x4481C784.toInt()
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, borderPaint)

        // Hero Emoji / Icon circle
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2E7D32.toInt()
        }
        canvas.drawCircle(width / 2f, 420f, 120f, circlePaint)

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 110f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(recipe.emojiHero, width / 2f, 460f, emojiPaint)

        // Catchy Viral Slogan
        val viralSloganPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFB74D.toInt()
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Ich habe aus meinem leeren Kühlschrank", width / 2f, 620f, viralSloganPaint)
        canvas.drawText("das hier gezaubert: ✨", width / 2f, 675f, viralSloganPaint)

        // Dish Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val title = if (recipe.title.length > 28) recipe.title.take(26) + "..." else recipe.title
        canvas.drawText(title, width / 2f, 790f, titlePaint)

        // Stats Badges Row: Time | Score | Portions
        val statY = 920f
        val statPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF192A1B.toInt()
        }
        val statBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = 0x66A5D6A7.toInt()
        }

        // Badge 1: Zero Waste Score
        val b1 = RectF(110f, statY, 370f, statY + 120f)
        canvas.drawRoundRect(b1, 24f, 24f, statPaint)
        canvas.drawRoundRect(b1, 24f, 24f, statBorderPaint)

        // Badge 2: Prep Time
        val b2 = RectF(410f, statY, 670f, statY + 120f)
        canvas.drawRoundRect(b2, 24f, 24f, statPaint)
        canvas.drawRoundRect(b2, 24f, 24f, statBorderPaint)

        // Badge 3: Match
        val b3 = RectF(710f, statY, 970f, statY + 120f)
        canvas.drawRoundRect(b3, 24f, 24f, statPaint)
        canvas.drawRoundRect(b3, 24f, 24f, statBorderPaint)

        val statValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF81C784.toInt()
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xAAFFFFFF.toInt()
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("${recipe.zeroWasteScore}%", 240f, statY + 55f, statValuePaint)
        canvas.drawText("Zero Waste", 240f, statY + 95f, statLabelPaint)

        canvas.drawText("${recipe.prepTimeMinutes} Min", 540f, statY + 55f, statValuePaint)
        canvas.drawText("Schnellkoch", 540f, statY + 95f, statLabelPaint)

        canvas.drawText("${recipe.matchPercentage}%", 840f, statY + 55f, statValuePaint)
        canvas.drawText("Zutaten Match", 840f, statY + 95f, statLabelPaint)

        // Used Ingredients Highlights
        val subHeadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFC8E6C9.toInt()
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🌱 VOR DER TONNE GERETTET:", width / 2f, 1140f, subHeadPaint)

        val ingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        val ingredientsText = recipe.usedIngredients.take(5).joinToString("  •  ")
        canvas.drawText(ingredientsText, width / 2f, 1210f, ingPaint)

        // Quote Box
        val quoteBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33000000
        }
        val quoteRect = RectF(120f, 1310f, width - 120f, 1490f)
        canvas.drawRoundRect(quoteRect, 24f, 24f, quoteBgPaint)

        val quotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE0E0E0.toInt()
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        canvas.drawText("\"Kochen mit dem, was da ist –", width / 2f, 1380f, quotePaint)
        canvas.drawText("spart Geld, schmeckt besser und rettet Essen!\"", width / 2f, 1435f, quotePaint)

        // Bottom Watermark
        val bottomPill = RectF(width / 2f - 240f, 1540f, width / 2f + 240f, 1610f)
        val pillBrand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2E7D32.toInt()
        }
        canvas.drawRoundRect(bottomPill, 35f, 35f, pillBrand)

        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Gemacht mit FridgeChef AI", width / 2f, 1585f, brandPaint)

        // Hashtags footer
        val hashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x88FFFFFF.toInt()
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("#ZeroWasteCook  #FridgeScan  #WhatICooked", width / 2f, 1750f, hashPaint)

        return bitmap
    }

    fun shareStory(context: Context, recipe: Recipe) {
        try {
            val bitmap = createStoryCardBitmap(recipe)
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "fridgechef_story_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, generateShareText(recipe))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Story teilen (Instagram, TikTok, WhatsApp)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback to text share
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, generateShareText(recipe))
            }
            context.startActivity(Intent.createChooser(textIntent, "Rezept teilen"))
        }
    }
}
