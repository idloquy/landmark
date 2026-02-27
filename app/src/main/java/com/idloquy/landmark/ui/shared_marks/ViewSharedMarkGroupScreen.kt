package com.idloquy.landmark.ui.shared_marks

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.data.database.model.SharedMark
import com.idloquy.landmark.data.database.model.SharedMarkGroup
import com.idloquy.landmark.data.database.model.SharedMarkGroupWithMarks
import com.idloquy.landmark.data.repository.exceptions.InvalidGroupIdException
import com.idloquy.landmark.model.Location
import com.idloquy.landmark.ui.CopyableMarkListItem
import com.idloquy.landmark.ui.SelectableLazyColumnItem
import com.idloquy.landmark.ui.shared_marks.exceptions.InvalidServerResponseException
import com.idloquy.landmark.ui.shared_marks.exceptions.TemporaryServerException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun ViewSharedMarkGroupScreen(
    viewModel: SharedMarksViewModel = hiltViewModel(),
    groupId: String,
    onBack: () -> Unit,
    onAddMarks: () -> Unit,
    onViewMark: (Int) -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val group by viewModel.getMarkGroupWithMarks(groupId).onEach {
        if (it == null) {
            Toast(context).apply { setText("Mark group deleted") }.show()
            onBack()
        }
    }.collectAsStateWithLifecycle(null)
    var indicatorText by rememberSaveable { mutableStateOf("") }
    var refreshError by rememberSaveable { mutableStateOf<Exception?>(null) }
    var deleteMarkError by rememberSaveable { mutableStateOf<Exception?>(null) }
    var deleteGroupError by rememberSaveable { mutableStateOf<Exception?>(null) }
    var importTokenError by rememberSaveable { mutableStateOf<Exception?>(null) }

    ViewSharedMarkGroupContent(
        group = group,
        onBack = onBack,
        onRefresh = {
            indicatorText = "Refreshing..."
            viewModel.refreshMarkGroup(
                id = groupId,
                onSuccess = {
                    indicatorText = ""
                },
                onError = {
                    if (it is InvalidGroupIdException) {
                        Toast(context).apply { setText("Group deleted") }.show()
                        indicatorText = "Handling deleted group..."
                        viewModel.deleteGroup(
                            group = group!!.sharedMarkGroup,
                            onSuccess = {
                                indicatorText = ""
                                onBack()
                            },
                            onError = {
                                indicatorText = ""
                                deleteGroupError = it
                            }
                        )
                        return@refreshMarkGroup
                    }
                    indicatorText = ""
                    refreshError = it
                },
            )
        },
        onAddMarks = onAddMarks,
        onDeleteMarks = { marks ->
            indicatorText = "Deleting marks..."
            viewModel.deleteMarks(
                group = group!!.sharedMarkGroup,
                marks = marks,
                onSuccess = {
                    indicatorText = ""
                },
                onError = {
                    indicatorText = ""
                    deleteMarkError = it
                },
            )
        },
        onDeleteGroup = {
            indicatorText = "Deleting group..."
            viewModel.deleteGroup(
                group = group!!.sharedMarkGroup,
                onSuccess = {
                    indicatorText = ""
                },
                onError = {
                    indicatorText = ""
                    deleteGroupError = it
                },
            )
        },
        onCopyToClipboard = { sharedMark ->
            coroutineScope.launch {
                clipboardManager.setClipEntry(
                    ClipEntry(
                        ClipData.newPlainText(
                            "coordinates",
                            "${sharedMark.location.latitude}, ${sharedMark.location.longitude}",
                        )
                    )
                )
            }
        },
        onViewMark = onViewMark,
        onImportEditToken = { token ->
            indicatorText = "Importing token..."

            viewModel.importEditToken(
                group = group!!.sharedMarkGroup,
                editToken = token,
                onSuccess = {
                    indicatorText = ""
                    Toast(context).apply {
                        setText("Edit token imported successfully")
                    }.show()
                },
                onError = {
                    indicatorText = ""
                    importTokenError = it
                }
            )
        }
    )

    if (refreshError != null) {
        refreshError?.let {
            Log.d("landmark", "refresh error: $it")
            SharedMarksErrorDialog(
                title = "Failed to refresh",
                error = it,
                onDismiss = { refreshError = null },
            )
        }
    }

    if (deleteMarkError != null) {
        deleteMarkError?.let {
            Log.d("landmark", "delete mark error: $deleteMarkError")
            SharedMarksErrorDialog(
                title = "Failed to delete marks",
                error = it,
                onDismiss = { deleteMarkError = null },
            )
        }
    }

    if (deleteGroupError != null) {
        deleteGroupError?.let {
            Log.d("landmark", "delete group error: $deleteGroupError")
            SharedMarksErrorDialog(
                title = "Failed to delete group",
                error = it,
                onDismiss = { deleteGroupError = null },
            )
        }
    }

    if (importTokenError != null) {
        importTokenError?.let {
            Log.d("landmark", "import token error: $importTokenError")
            SharedMarksErrorDialog(
                title = "Failed to import edit token",
                error = it,
                onDismiss = { importTokenError = null },
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
fun ViewSharedMarkGroupContent(
    group: SharedMarkGroupWithMarks?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onAddMarks: () -> Unit,
    onDeleteMarks: (List<SharedMark>) -> Unit,
    onDeleteGroup: () -> Unit,
    onCopyToClipboard: (SharedMark) -> Unit,
    onViewMark: (Int) -> Unit,
    onImportEditToken: (String) -> Unit,
) {
    val selectedMarks = remember { mutableStateListOf<SharedMark>() }
    var showImportEditTokenDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (selectedMarks.isEmpty()) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Shared marks",
                            fontWeight = FontWeight.Bold,
                        )
                    }, navigationIcon = {
                        IconButton(
                            onClick = onBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }, actions = {
                        var showMoreActions by rememberSaveable { mutableStateOf(false) }
                        val coroutineScope = rememberCoroutineScope()
                        val clipboardManager = LocalClipboard.current

                        if (group != null) {
                            if (group.sharedMarkGroup.editToken.isEmpty()) {
                                IconButton(
                                    onClick = onRefresh,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }

                            if (group.sharedMarkGroup.editToken.isNotEmpty()) {
                                IconButton(
                                    onClick = onAddMarks,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add marks",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showMoreActions = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More actions",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                DropdownMenu(
                                    expanded = showMoreActions,
                                    onDismissRequest = { showMoreActions = false },
                                    modifier = Modifier.fillMaxWidth(0.4f),
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Copy group ID")
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                clipboardManager.setClipEntry(
                                                    ClipEntry(
                                                        ClipData.newPlainText(
                                                            "group ID",
                                                            group.sharedMarkGroup.id,
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    )
                                    if (group.sharedMarkGroup.editToken.isNotEmpty()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text("Export edit token")
                                            },
                                            onClick = {
                                                coroutineScope.launch {
                                                    clipboardManager.setClipEntry(
                                                        ClipEntry(
                                                            ClipData.newPlainText(
                                                                "edit token",
                                                                group.sharedMarkGroup.editToken
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = {
                                                Text("Import edit token")
                                            },
                                            onClick = {
                                                showMoreActions = false
                                                showImportEditTokenDialog = true
                                            },
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text("Delete group")
                                        },
                                        onClick = onDeleteGroup,
                                    )
                                }
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
                            text = "${selectedMarks.size}",
                        )
                    }, navigationIcon = {
                        IconButton(
                            onClick = { selectedMarks.clear() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Clear selection",
                            )
                        }
                    }, actions = {
                        var showMoreActions by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = {
                                showMoreActions = false
                                onDeleteMarks(selectedMarks)
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
                                    for (mark in group!!.marks) {
                                        if (!selectedMarks.contains(mark)) {
                                            selectedMarks.add(mark)
                                        }
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
        if (group == null) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Loading...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(group.marks) { mark ->
                    if (group.sharedMarkGroup.editToken.isNotEmpty()) {
                        SelectableLazyColumnItem(
                            selected = selectedMarks.contains(mark),
                            onClick = {
                                if (selectedMarks.isEmpty()) {
                                    onViewMark(mark.id)
                                } else {
                                    if (selectedMarks.contains(mark)) {
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
                        ) {
                            CopyableMarkListItem(
                                mark = Mark(
                                    location = mark.location,
                                    description = mark.description,
                                ),
                                modifier = Modifier.padding(10.dp),
                                selected = selectedMarks.contains(mark),
                                onCopy = {
                                    onCopyToClipboard(mark)
                                }
                            )
                        }
                    } else {
                        Box(
                            Modifier
                                .clickable(
                                    onClick = {
                                        onViewMark(mark.id)
                                    })
                                .padding(10.dp)
                        ) {
                            CopyableMarkListItem(
                                mark = Mark(
                                    location = mark.location,
                                    description = mark.description,
                                ),
                                onCopy = {
                                    onCopyToClipboard(mark)
                                }
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        if (showImportEditTokenDialog) {
            ImportEditTokenDialog(
                onDismiss = {
                    showImportEditTokenDialog = false
                },
                onImport = {
                    showImportEditTokenDialog = false
                    onImportEditToken(it)
                }
            )
        }
    }
}

@Composable
fun ImportEditTokenDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit,
) {
    var editToken by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(10.dp)
                    .padding(top = 10.dp),
            ) {
                OutlinedTextField(
                    value = editToken,
                    onValueChange = { editToken = it },
                    label = { Text("Edit Token") },
                    singleLine = true,
                    supportingText = if (isError) {
                        {
                            Text("Invalid token")
                        }
                    } else {
                        null
                    },
                    isError = isError,
                )

                Spacer(Modifier.height(5.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.weight(1f))

                    TextButton(
                        modifier = Modifier.weight(1f), onClick = {
                            if (editToken.isNotEmpty()) {
                                onImport(editToken)
                            } else {
                                isError = true
                            }
                        }) {
                        Text("Import")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ViewSharedMarkGroupPreview() {
    ViewSharedMarkGroupContent(
        group = SharedMarkGroupWithMarks(
            sharedMarkGroup = SharedMarkGroup(
                "0000-0000",
                "group1",
            ), marks = listOf(
                SharedMark(
                    location = Location(
                        latitude = 0.12345,
                        longitude = 0.12345,
                    ), description = "lorem ipsum",
                    remoteId = "0000-0000",
                    groupId = "0000-0000"
                ), SharedMark(
                    location = Location(
                        latitude = 0.12345,
                        longitude = 0.12345,
                    ), description = "lorem ipsum",
                    remoteId = "0000-0000",
                    groupId = "0000-0000"
                )
            )
        ),
        onBack = {},
        onRefresh = {},
        onAddMarks = {},
        onCopyToClipboard = {},
        onDeleteMarks = {},
        onDeleteGroup = {},
        onViewMark = {},
        onImportEditToken = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ViewSharedMarkGroupOwnedPreview() {
    ViewSharedMarkGroupContent(
        group = SharedMarkGroupWithMarks(
            sharedMarkGroup = SharedMarkGroup(
                id = "0000-0000",
                name = "group1",
                editToken = "lorem ipsum",
            ), marks = listOf(
                SharedMark(
                    location = Location(
                        latitude = 0.12345,
                        longitude = 0.12345,
                    ), description = "lorem ipsum",
                    remoteId = "0000-0000",
                    groupId = "0000-0000"
                ), SharedMark(
                    location = Location(
                        latitude = 0.12345,
                        longitude = 0.12345,
                    ), description = "lorem ipsum",
                    remoteId = "0000-0000",
                    groupId = "0000-0000"
                )
            )
        ),
        onBack = {},
        onRefresh = {},
        onAddMarks = {},
        onDeleteMarks = {},
        onDeleteGroup = {},
        onCopyToClipboard = {},
        onViewMark = {},
        onImportEditToken = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ViewSharedMarkGroupError() {
    ViewSharedMarkGroupContent(
        group = SharedMarkGroupWithMarks(
            sharedMarkGroup = SharedMarkGroup(
                id = "0000-0000",
                name = "group1",
            ), marks = listOf(
                SharedMark(
                    location = Location(
                        latitude = 0.12345,
                        longitude = 0.12345,
                    ), description = "lorem ipsum",
                    remoteId = "0000-0000",
                    groupId = "0000-0000"
                ), SharedMark(
                    location = Location(
                        latitude = 0.12345,
                        longitude = 0.12345,
                    ), description = "lorem ipsum",
                    remoteId = "0000-0000",
                    groupId = "0000-0000"
                )
            )
        ),
        onBack = {},
        onRefresh = {},
        onAddMarks = {},
        onDeleteMarks = {},
        onDeleteGroup = {},
        onCopyToClipboard = {},
        onViewMark = {},
        onImportEditToken = {},
    )
}