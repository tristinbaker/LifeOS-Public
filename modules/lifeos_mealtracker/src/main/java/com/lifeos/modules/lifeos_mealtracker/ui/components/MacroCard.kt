package com.lifeos.modules.lifeos_mealtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_mealtracker.ui.theme.CarbsColor
import com.lifeos.modules.lifeos_mealtracker.ui.theme.FatColor
import com.lifeos.modules.lifeos_mealtracker.ui.theme.ProteinColor

@Composable
fun MacroCard(
    label: String,
    current: Int,
    target: Int?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${current}g",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (target != null) {
                Text(
                    text = " / ${target}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MacroRow(
    protein: Int,
    carbs: Int,
    fat: Int,
    proteinTarget: Int?,
    carbsTarget: Int?,
    fatTarget: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MacroCard(
            label = "Protein",
            current = protein,
            target = proteinTarget,
            color = ProteinColor,
            modifier = Modifier.weight(1f)
        )
        MacroCard(
            label = "Carbs",
            current = carbs,
            target = carbsTarget,
            color = CarbsColor,
            modifier = Modifier.weight(1f)
        )
        MacroCard(
            label = "Fat",
            current = fat,
            target = fatTarget,
            color = FatColor,
            modifier = Modifier.weight(1f)
        )
    }
}
