package com.idloquy.landmark.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.model.Location
import kotlinx.coroutines.flow.onEach

@Composable
fun MarkScreen(
    markId: Int,
    viewModel: LandmarkViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val mark by viewModel.getMarkById(markId).onEach {
        if (it == null) {
            Toast(context).apply {
                setText("Mark deleted")
            }.show()
            onBack()
        }
    }.collectAsStateWithLifecycle(null)

    MarkContent(
        mark = mark,
        onUpdate = { mark ->
            viewModel.updateMark(mark)
        },
        onDelete = {
            viewModel.deleteMark(mark!!)
            Toast(context).apply {
                setText("Mark deleted")
            }.show()
            onBack()
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkContent(
    mark: Mark?,
    onUpdate: (Mark) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mark",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
                actions = {
                    if (mark != null) {
                        IconButton(
                            onClick = { showEditDialog = true },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            if (mark != null) {
                LocationRow(mark.location)

                Spacer(Modifier.height(10.dp))

                Text(
                    text = mark.description,
                    fontSize = 16.sp,
                )

                if (showEditDialog) {
                    MarkLocationDialog(
                        Location(
                            mark.location.latitude,
                            mark.location.longitude,
                        ),
                        description = mark.description,
                        onDismiss = { showEditDialog = false },
                        onMark = { _, description ->
                            onUpdate(
                                mark.copy(description = description)
                            )
                            showEditDialog = false
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Loading...",
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MarkScreenPreview() {
    MarkContent(
        mark = Mark(
            location = Location(
                latitude = 1.123451234512345,
                longitude = 1.123451234512345,
            ),
            description = "test",
        ),
        onUpdate = {},
        onDelete = {},
        onBack = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MarkScreenNoMarkPreview() {
    MarkContent(
        mark = null,
        onUpdate = {},
        onDelete = {},
        onBack = {},
    )
}