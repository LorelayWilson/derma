package com.example.dermaApp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableInfoCard(
    title: String,
    probabilityText: String,
    mainContentColor: Color = MaterialTheme.colorScheme.onSurface,
    expandedContent: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "iconRotation")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = mainContentColor
                    )
                    Text(
                        text = probabilityText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = mainContentColor
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                    expandedContent()
                }
            }
        }
    }
}