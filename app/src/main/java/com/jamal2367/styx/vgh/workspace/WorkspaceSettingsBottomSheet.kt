/*
 * Vian AI Greenhouse - Workspace Settings Bottom Sheet
 * PRD v4.3 Section 16 + UI Additions: Workspace-scoped settings shortcut
 */

package com.jamal2367.styx.vgh.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WorkspaceSettingsBottomSheet(
    workspaceState: WorkspaceState,
    onDismiss: () -> Unit,
    onAiToolSelected: (String) -> Unit,
    onRepoUrlChanged: (String) -> Unit,
    onBranchChanged: (String) -> Unit,
    onCiUrlChanged: (String) -> Unit,
    onPushEncodingToggled: (Boolean) -> Unit,
    onPwaUrlChanged: (String) -> Unit,
    onManageWorkspaceClick: () -> Unit,
    accentColor: Color = Color(0xFF6200EE)
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings: ${workspaceState.workspaceName}",
                style = MaterialTheme.typography.h6
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Text(
            text = "AI Tool",
            style = MaterialTheme.typography.subtitle2,
            color = accentColor
        )
        Text(
            text = "Current: ${workspaceState.aiToolEntryName}",
            style = MaterialTheme.typography.body2
        )

        Text(
            text = "Repository",
            style = MaterialTheme.typography.subtitle2,
            color = accentColor
        )
        Text(
            text = "URL: ${workspaceState.repoUrl}",
            style = MaterialTheme.typography.body2
        )
        Text(
            text = "Branch: ${workspaceState.branch}",
            style = MaterialTheme.typography.body2
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Push Encoding: Base64")
            Switch(
                checked = workspaceState.pushEncodingBase64,
                onCheckedChange = onPushEncodingToggled
            )
        }

        TextButton(
            onClick = onManageWorkspaceClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Manage Workspace →")
        }
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        content()
    }
}
