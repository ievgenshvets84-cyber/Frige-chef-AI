package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.DefaultRecipes

class DailyRecipeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val EXTRA_RECIPE_ID = "extra_recipe_id"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val sampleRecipe = DefaultRecipes.getRecipeCatalog().firstOrNull()

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (sampleRecipe != null) {
                    putExtra(EXTRA_RECIPE_ID, sampleRecipe.id)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_daily_recipe).apply {
                if (sampleRecipe != null) {
                    setTextViewText(R.id.widget_recipe_title, sampleRecipe.title)
                    setTextViewText(
                        R.id.widget_recipe_subtitle,
                        "Rettet: ${sampleRecipe.usedIngredients.take(3).joinToString(", ")} • In ${sampleRecipe.prepTimeMinutes} Min"
                    )
                }
                setOnClickPendingIntent(R.id.widget_daily_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
