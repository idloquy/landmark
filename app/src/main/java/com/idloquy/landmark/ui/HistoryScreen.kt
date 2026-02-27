package com.idloquy.landmark.ui

import android.content.ClipData
import android.util.Log
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.model.Location
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    viewModel: LandmarkViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSharedMarks: () -> Unit,
    onViewMark: (Int) -> Unit,
) {
    val marks = viewModel.marks.collectAsStateWithLifecycle(listOf())
    val clipboardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    HistoryContent(
        marks = marks.value,
        onBack = onBack,
        onSharedMarks = onSharedMarks,
        onViewMark = onViewMark,
        onCopyToClipboard = { mark ->
            coroutineScope.launch {
                clipboardManager.setClipEntry(
                    ClipEntry(
                        ClipData.newPlainText(
                            "coordinates", "${mark.location.latitude}, ${mark.location.longitude}"
                        )
                    )
                )
            }
        },
        onDeleteMarks = {
            viewModel.deleteMarks(it)
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    marks: List<Mark>,
    onBack: () -> Unit,
    onViewMark: (Int) -> Unit,
    onCopyToClipboard: (Mark) -> Unit,
    onDeleteMarks: (List<Mark>) -> Unit,
    onSharedMarks: () -> Unit,
) {
    val selectedMarks = remember { mutableStateListOf<Mark>() }

    Scaffold(
        topBar = {
            HistoryTopBar(
                selectedMarks = selectedMarks,
                onBack = onBack,
                onSharedMarks = onSharedMarks,
                onClearSelection = {
                    selectedMarks.clear()
                },
                onDeleteSelected = {
                    onDeleteMarks(selectedMarks.toList())
                    selectedMarks.clear()
                },
                onSelectAll = {
                    marks.forEach {
                        if (!selectedMarks.contains(it)) selectedMarks.add(it)
                    }
                })
        },
    ) { paddingValues ->
        if (marks.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(marks) { mark ->
                    val selected = selectedMarks.contains(mark)
                    HistoryItem(
                        mark,
                        selected = selected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        onClick = {
                            if (selectedMarks.isEmpty()) {
                                onViewMark(mark.id)
                            } else {
                                if (selected) {
                                    selectedMarks.remove(mark)
                                } else {
                                    selectedMarks.add(mark)
                                }
                            }
                        },
                        onLongClick = {
                            if (selectedMarks.contains(mark)) {
                                selectedMarks.remove(mark)
                            } else {
                                selectedMarks.add(mark)
                            }
                        },
                        onCopyToClipboard = {
                            onCopyToClipboard(mark)
                        })
                    HorizontalDivider()
                }
            }
        } else {
            Log.d("landmark", "rendering empty history screen")
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No marks to display",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopBar(
    selectedMarks: List<Mark>,
    onBack: () -> Unit,
    onSharedMarks: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSelectAll: () -> Unit,
) {
    if (selectedMarks.isEmpty()) {
        TopAppBar(
            title = {
                Text(
                    text = "History",
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
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            actions = {
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
                        DropdownMenuItem(
                            text = { Text("Shared marks") },
                            onClick = {
                                showActions = false
                                onSharedMarks()
                            },
                        )
                    }
                }
            }
        )
    } else {
        var showMoreActions by rememberSaveable { mutableStateOf(false) }

        TopAppBar(
            title = {
                Text("${selectedMarks.size}")
            }, navigationIcon = {
                IconButton(
                    onClick = onClearSelection
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Clear selection",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ), actions = {
                IconButton(
                    onClick = {
                        showMoreActions = false
                        onDeleteSelected()
                    },
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete selected",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                IconButton(
                    onClick = {
                        showMoreActions = !showMoreActions
                    }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    DropdownMenu(
                        expanded = showMoreActions,
                        onDismissRequest = { showMoreActions = false },
                        modifier = Modifier.fillMaxWidth(0.4f)
                    ) {
                        DropdownMenuItem(text = {
                            Text(
                                text = "Select all",
                                fontSize = 15.sp,
                            )
                        }, onClick = {
                            onSelectAll()
                            showMoreActions = false
                        })
                    }
                }
            })
    }
}

@Composable
fun SelectableLazyColumnItem(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: (@Composable () -> Unit),
) {
    val localIndication = LocalIndication.current
    var boxModifier = Modifier.combinedClickable(
        enabled = true,
        onClickLabel = null,
        onLongClickLabel = null,
        onLongClick = onLongClick,
        onDoubleClick = null,
        onClick = onClick,
        role = null,
        indication = if (!selected) localIndication else null,
        interactionSource = remember { MutableInteractionSource() },
        hapticFeedbackEnabled = true
    )
    if (selected) {
        boxModifier =
            boxModifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    }

    Box(
        modifier = boxModifier,
    ) {
        content()
    }
}

@Composable
fun MarkListItemInfo(
    mark: Mark,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row {
            Text(
                text = "${mark.location.latitude}, ${mark.location.longitude}",
                fontSize = 12.sp,
                color = if (!selected) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(5.dp))

        val lines = mark.description.lines()
        val text = if (lines.size > 1) {
            "${lines.first()}..."
        } else {
            mark.description
        }

        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
fun CopyableMarkListItem(
    mark: Mark,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onCopy: () -> Unit,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        MarkListItemInfo(mark, selected)

        Spacer(Modifier.weight(1f))

        IconButton(
            onClick = onCopy,
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy to clipboard",
            )
        }
    }
}

@Composable
fun HistoryItem(
    mark: Mark,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCopyToClipboard: () -> Unit,
) {
    SelectableLazyColumnItem(
        selected = selected,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        CopyableMarkListItem(
            mark = mark,
            modifier = modifier,
            selected = selected,
            onCopy = onCopyToClipboard,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistoryPreview() {
    val marks = listOf(
        Mark(
            location = Location(
                latitude = 0.12345,
                longitude = 0.12345,
            ),
            description = "Lorem",
        ), Mark(
            location = Location(
                latitude = 0.12345,
                longitude = 0.12345,
            ),
            description = "Lorem ipsum\ndorsit amet",
        )
    )
    HistoryContent(
        marks = marks,
        onBack = {},
        onSharedMarks = {},
        onViewMark = {},
        onCopyToClipboard = {},
        onDeleteMarks = {})
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistoryNoMarksPreview() {
    val marks = listOf<Mark>()
    HistoryContent(
        marks = marks,
        onBack = {},
        onSharedMarks = {},
        onViewMark = {},
        onCopyToClipboard = {},
        onDeleteMarks = {})
}