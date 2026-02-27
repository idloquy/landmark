package com.idloquy.landmark.ui.shared_marks

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idloquy.landmark.data.database.model.SharedMarkGroup
import com.idloquy.landmark.ui.SelectableLazyColumnItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SharedMarksScreen(
    sharedMarksViewModel: SharedMarksViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onViewGroup: (String) -> Unit,
    onShareMarks: () -> Unit,
    onImportMarks: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val groups by sharedMarksViewModel.getMarkGroups().collectAsStateWithLifecycle(listOf())
    var indicatorText by rememberSaveable { mutableStateOf("") }
    var deleteGroupError by rememberSaveable { mutableStateOf<Exception?>(null) }
    var showNoMarksDialog by rememberSaveable { mutableStateOf(false) }

    SharedMarksContent(
        groups = groups,
        onBack = onBack,
        onViewGroup = onViewGroup,
        onShareMarks = {
            coroutineScope.launch {
                val marks = sharedMarksViewModel.getMarks().first()
                if (marks.isEmpty()) {
                    showNoMarksDialog = true
                } else {
                    onShareMarks()
                }
            }
        },
        onImportMarks = onImportMarks,
        onDeleteGroups = {
            indicatorText = "Deleting groups..."
            sharedMarksViewModel.deleteGroups(
                groups = groups,
                onSuccess = {
                    indicatorText = ""
                },
                onError = {
                    indicatorText = ""
                    deleteGroupError = it
                },
            )
        },
    )

    if (showNoMarksDialog) {
        NoMarksDialog(onDismiss = { showNoMarksDialog = false })
    }

    if (deleteGroupError != null) {
        deleteGroupError?.let {
            Log.d("landmark", "delete group error: $deleteGroupError")
            SharedMarksErrorDialog(
                title = "Failed to delete groups",
                error = it,
                onDismiss = { deleteGroupError = null },
            )
        }
    }

    if (indicatorText.isNotEmpty()) {
        ProgressIndicatorDialog(
            onDismiss = { indicatorText = "" },
            text = indicatorText,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMarksContent(
    groups: List<SharedMarkGroup>,
    onBack: () -> Unit,
    onViewGroup: (String) -> Unit,
    onDeleteGroups: (List<SharedMarkGroup>) -> Unit,
    onShareMarks: () -> Unit,
    onImportMarks: () -> Unit,
) {
    val selectedGroups = remember { mutableStateListOf<SharedMarkGroup>() }

    Scaffold(
        topBar = {
            if (selectedGroups.isEmpty()) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Shared marks",
                            fontWeight = FontWeight.Bold,
                        )
                    }, navigationIcon = {
                        IconButton(
                            onClick = onBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }, actions = {
                        var showActions by rememberSaveable { mutableStateOf(false) }
                        IconButton(
                            onClick = { showActions = true },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Actions",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            DropdownMenu(
                                expanded = showActions,
                                onDismissRequest = { showActions = false },
                                modifier = Modifier.fillMaxWidth(0.4f)
                            ) {
                                DropdownMenuItem(text = {
                                    Text("Share marks")
                                }, onClick = {
                                    showActions = false
                                    onShareMarks()
                                })
                                DropdownMenuItem(text = {
                                    Text("Import shared marks")
                                }, onClick = {
                                    showActions = false
                                    onImportMarks()
                                })
                            }
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedGroups.size}",
                            fontWeight = FontWeight.Bold,
                        )
                    }, navigationIcon = {
                        IconButton(
                            onClick = onBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }, actions = {
                        var showMoreActions by rememberSaveable { mutableStateOf(false) }

                        IconButton(
                            onClick = {
                                onDeleteGroups(selectedGroups.toList())
                                selectedGroups.clear()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }

                        IconButton(
                            onClick = { showMoreActions = true },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Actions",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            DropdownMenu(
                                expanded = showMoreActions,
                                onDismissRequest = { showMoreActions = false },
                                modifier = Modifier.fillMaxWidth(0.4f)
                            ) {
                                DropdownMenuItem(text = {
                                    Text("Select all")
                                }, onClick = {
                                    groups.forEach {
                                        if (!selectedGroups.contains(it)) selectedGroups.add(it)
                                    }
                                    showMoreActions = false
                                })
                            }
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            }
        }
    ) { paddingValues ->
        if (groups.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(groups) { markGroup ->
                    SharedMarkGroupItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        markGroup = markGroup,
                        onClick = {
                            val selected = selectedGroups.contains(markGroup)
                            if (selectedGroups.isEmpty()) {
                                onViewGroup(markGroup.id)
                            } else {
                                if (selected) {
                                    selectedGroups.remove(markGroup)
                                } else {
                                    selectedGroups.add(markGroup)
                                }
                            }
                        },
                        onLongClick = {
                            Log.d("landmark sharedmarkscreen", "onlongclick handler")
                            if (selectedGroups.contains(markGroup)) {
                                selectedGroups.remove(markGroup)
                            } else {
                                selectedGroups.add(markGroup)
                            }
                        },
                        selected = selectedGroups.contains(markGroup),
                    )
                    HorizontalDivider()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextButton(
                    onClick = onShareMarks,
                ) {
                    Text("Share marks")
                }

                TextButton(
                    onClick = { onImportMarks() },
                ) {
                    Text(
                        "Import shared marks",
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun SharedMarkGroupItem(
    markGroup: SharedMarkGroup,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    SelectableLazyColumnItem(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Row(modifier = modifier) {
            Text(
                text = markGroup.name,
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
fun NoMarksDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("No marks to share")
        },
        text = {
            Text("You don't have any marks to share")
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SharedMarksPreview() {
    val marks = listOf(
        SharedMarkGroup(
            id = "0000-0000", name = "Tracks",
        )
    )
    SharedMarksContent(
        groups = marks,
        onBack = {},
        onViewGroup = {},
        onShareMarks = {},
        onDeleteGroups = {},
        onImportMarks = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SharedMarksEmptyPreview() {
    SharedMarksContent(
        groups = listOf(),
        onBack = {},
        onViewGroup = {},
        onShareMarks = {},
        onDeleteGroups = {},
        onImportMarks = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NoMarksDialogPreview() {
    Box(
        modifier = Modifier.fillMaxSize()
    )
    NoMarksDialog(onDismiss = {})
}