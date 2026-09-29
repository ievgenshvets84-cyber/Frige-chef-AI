package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ingredient
import com.example.ui.theme.GoodGreen
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.WarningAmber

@Composable
fun IngredientChip(
    ingredient: Ingredient,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when {
        ingredient.isUrgent || ingredient.shelfLifeDays <= 1 -> UrgentRed
        ingredient.shelfLifeDays <= 3 -> WarningAmber
        else -> GoodGreen
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .testTag("ingredient_chip_${ingredient.name}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Freshness Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Emoji
            Text(
                text = ingredient.emoji,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Name
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (ingredient.isUrgent) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "• Verbrauchen!",
                    style = MaterialTheme.typography.labelSmall,
                    color = UrgentRed,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_ingredient_${ingredient.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Entfernen",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
