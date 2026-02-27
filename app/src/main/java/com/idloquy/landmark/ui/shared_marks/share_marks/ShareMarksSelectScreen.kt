package com.idloquy.landmark.ui.shared_marks.share_marks

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.model.Location
import com.idloquy.landmark.ui.MarkListItemInfo
import com.idloquy.landmark.ui.shared_marks.ProgressIndicatorDialog
import com.idloquy.landmark.ui.shared_marks.SharedMarksErrorDialog

@Composable
fun ShareMarksSelectScreen(
    viewModel: ShareMarksViewModel = hiltViewModel(),
    groupName: String,
    onBack: () -> Unit,
    onNext: (String) -> Unit,
) {
    Log.d("landmark sharemarksselectscreen", "viewmodel: $viewModel")

    val marks by viewModel.getAllMarks().collectAsStateWithLifecycle(null)
    var indicatorText by rememberSaveable { mutableStateOf("") }
    var uploadError by rememberSaveable { mutableStateOf<Exception?>(null) }

    ShareMarksSelectContent(
        marks = marks,
        onBack = onBack,
        onNext = { marks ->
            indicatorText = "Uploading marks..."
            viewModel.shareMarks(
                groupName = groupName,
                marks = marks,
                onSuccess = {
                    Log.d("landmark", "onsuccess with groupid: $it")
                    indicatorText = ""
                    onNext(it)
                },
                onError = {
                    indicatorText = ""
                    uploadError = it
                },
            )
        }
    )

    if (uploadError != null) {
        uploadError?.let {
            Log.d("landmark", "upload error: $uploadError")
            SharedMarksErrorDialog(
                title = "Failed to share marks",
                error = uploadError!!,
                onDismiss = { uploadError = null },
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
fun SelectMarksScreen(
    marks: List<Mark>?,
    selectedIdxs: List<Int>,
    onSelect: (Int) -> Unit,
    onDeselect: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
    doneButtonLabel: String = "Done"
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val size = selectedIdxs.size
                    if (size == 0) {
                        Text(
                            text = "Share marks",
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text("Share $size mark${if (size == 1) "" else "s"}")
                    }
                },
                navigationIcon = {
                    if (selectedIdxs.isEmpty()) {
                        IconButton(
                            onClick = onBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onClearSelection,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Clear selection",
                            )
                        }
                    }
                },
                actions = {
                    if (selectedIdxs.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                if (selectedIdxs.isNotEmpty()) {
                                    onDone()
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                        ) {
                            Text(doneButtonLabel)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
    ) { paddingValues ->
        if (marks == null) {
            Column(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Loading...")
            }
        } else {
            // Note that the case where there are no marks is handled by SharedMarksScreen.
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                itemsIndexed(marks) { idx, mark ->
                    MarkItem(
                        modifier = Modifier.padding(10.dp),
                        mark = mark,
                        selected = selectedIdxs.contains(idx),
                        onClick = {
                            if (selectedIdxs.contains(idx)) onDeselect(idx)
                            else onSelect(idx)
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ShareMarksSelectContent(
    marks: List<Mark>?,
    onBack: () -> Unit,
    onNext: (List<Mark>) -> Unit,
) {
    val selectedIdxs = remember { mutableStateListOf<Int>() }

    SelectMarksScreen(
        marks = marks,
        selectedIdxs,
        onSelect = {
            selectedIdxs.add(it)
        },
        onDeselect = {
            selectedIdxs.remove(it)
        },
        onClearSelection = {
            selectedIdxs.clear()
        },
        onBack = onBack,
        onDone = { onNext(marks!!.filterIndexed { idx, _ -> selectedIdxs.contains(idx) }) },
        doneButtonLabel = "Next",
    )
}

@Composable
fun MarkItem(
    mark: Mark,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.toggleable(
            value = selected,
            onValueChange = { onClick() },
        )
    ) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            MarkListItemInfo(
                mark = mark,
                selected = false,
            )

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape)
                    .toggleable(selected, onValueChange = { onClick() })
                    .then(if (selected) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SharedMarkSelectPreview() {
    ShareMarksSelectContent(
        marks = listOf(
            Mark(
                location = Location(
                    0.12345,
                    0.12345,
                ),
                description = "test",
            )
        ),
        onBack = {},
        onNext = {},
    )
}