/*
 * Vian AI Greenhouse - Pane Tab Header Component
 * PRD v4.3 Section 1.5 + UI Additions: Browser-style tab switching
 */

package com.jamal2367.styx.vgh.pane

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaneTabHeader(
    currentPane: VghPane,
    onPaneSelected: (VghPane) -> Unit,
    onWorkspaceSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF6200EE)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab buttons
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            VghPane.values().forEach { pane ->
                PaneTabButton(
                    pane = pane,
                    isSelected = pane == currentPane,
                    accentColor = accentColor,
                    onClick = { onPaneSelected(pane) }
                )
            }
        }

        // Workspace settings cog
        IconButton(
            onClick = onWorkspaceSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onWorkspaceSettingsClick)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Workspace Settings",
                tint = accentColor
            )
        }
    }
}

@Composable
private fun PaneTabButton(
    pane: VghPane,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) accentColor.copy(alpha = 0.1f) else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = pane.displayName,
            fontSize = 12.sp,
            color = if (isSelected) accentColor else Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(accentColor)
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        content()
    }
}
