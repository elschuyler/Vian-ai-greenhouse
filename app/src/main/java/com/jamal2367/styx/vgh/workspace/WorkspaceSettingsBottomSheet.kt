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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jamal2367.styx.R
import com.jamal2367.styx.vgh.pane.VghPane

@Composable
fun WorkspaceSettingsBottomSheet(
    workspaceState: WorkspaceState,
    onDismiss: () -> Unit,
    onAiToolSelected: (String) -> Unit,
    onRepoUrlChanged: (String) -> Unit,
    onBranchChanged: (String) -> Unit,
    onCiUrlChanged: (String) -> Unit,
    onPushEncodingToggled: (Boolean) -> Unit, // true = Base64, false = Raw
    onPwaUrlChanged: (String) -> Unit,
    onManageWorkspaceClick: () -> Unit,
    accentColor: Color = MaterialTheme.colors.primary
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    ModalBottomSheetLayout(
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.vgh_workspace_settings_title, workspaceState.workspaceName),
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.vgh_close))
                    }
                }

                // AI Tool Selector
                WorkspaceSettingSection(title = stringResource(R.string.vgh_section_ai_tool)) {
                    DropdownMenuSection(
                        label = stringResource(R.string.vgh_ai_tool_entry),
                        currentValue = workspaceState.aiToolEntryName,
                        options = workspaceState.availableAiTools,
                        onOptionSelected = onAiToolSelected
                    )
                }

                // Repo Settings
                WorkspaceSettingSection(title = stringResource(R.string.vgh_section_repo)) {
                    TextFieldSection(
                        label = stringResource(R.string.vgh_repo_url),
                        value = workspaceState.repoUrl,
                        onValueChange = onRepoUrlChanged
                    )
                    TextFieldSection(
                        label = stringResource(R.string.vgh_repo_branch),
                        value = workspaceState.branch,
                        onValueChange = onBranchChanged
                    )
                    TextFieldSection(
                        label = stringResource(R.string.vgh_ci_url),
                        value = workspaceState.ciUrl.orEmpty(),
                        onValueChange = onCiUrlChanged,
                        placeholder = stringResource(R.string.vgh_ci_url_placeholder)
                    )
                }

                // Push Encoding Toggle
                WorkspaceSettingSection(title = stringResource(R.string.vgh_section_push)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.vgh_push_encoding))
                        Switch(
                            checked = workspaceState.pushEncodingBase64,
                            onCheckedChange = onPushEncodingToggled
                        )
                    }
                    Text(
                        text = if (workspaceState.pushEncodingBase64) 
                            stringResource(R.string.vgh_encoding_base64) 
                        else 
                            stringResource(R.string.vgh_encoding_raw),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }

                // PWA URL
                WorkspaceSettingSection(title = stringResource(R.string.vgh_section_pwa)) {
                    TextFieldSection(
                        label = stringResource(R.string.vgh_pwa_url),
                        value = workspaceState.pwaUrl.orEmpty(),
                        onValueChange = onPwaUrlChanged,
                        placeholder = stringResource(R.string.vgh_pwa_url_placeholder)
                    )
                }

                // Manage Workspace Link
                TextButton(
                    onClick = onManageWorkspaceClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.vgh_manage_workspace))
                }
            }
        },
        sheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Expanded,
            confirmValueChange = { false } // Prevent dismiss by swipe
        ),
        onDismiss = onDismiss,
        sheetBackgroundColor = MaterialTheme.colors.surface,
        sheetElevation = 16.dp
    ) {
        // Transparent overlay - content is in sheetContent
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun WorkspaceSettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
        content()
    }
}

@Composable
private fun TextFieldSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = MaterialTheme.typography.body2.fontSize)
    )
}

@Composable
private fun DropdownMenuSection(
    label: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = currentValue,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(text = option)
                }
            }
        }
    }
}
