package com.idloquy.landmark.ui.shared_marks

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.ui.shared_marks.share_marks.SelectMarksScreen
import kotlinx.coroutines.flow.onEach

@Composable
fun SharedMarkGroupAddMarksScreen(
    viewModel: SharedMarksViewModel = hiltViewModel(),
    groupId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current

    val marks by viewModel.getMarks().collectAsStateWithLifecycle(null)
    val group by viewModel.getMarkGroupWithMarks(groupId).onEach {
        if (it == null) {
            Toast(context).apply {
                setText("Group deleted")
            }.show()
        }
    }.collectAsStateWithLifecycle(null)
    var indicatorText by rememberSaveable { mutableStateOf("") }
    var uploadError by rememberSaveable { mutableStateOf<Exception?>(null) }

    SharedMarkGroupAddMarksContent(
        marks = group?.let { group ->
            marks?.filter { mark ->
                group.marks.none {
                    Mark(
                        location = it.location, description = it.description
                    ) == mark.copy(id = 0)
                }
            }
        },
        onBack = onBack,
        onDone = { marks ->
            indicatorText = "Uploading marks..."
            viewModel.addMarks(
                group = group!!.sharedMarkGroup,
                marks = marks,
                onSuccess = {
                    indicatorText = ""
                    onDone()
                },
                onError = {
                    indicatorText = ""
                    uploadError = it
                }
            )
        },
    )

    if (uploadError != null) {
        uploadError?.let {
            SharedMarksErrorDialog(
                title = "Failed to add marks",
                error = it,
                onDismiss = { uploadError = null },
                onRetry = null,
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

@Composable
fun SharedMarkGroupAddMarksContent(
    marks: List<Mark>?,
    onBack: () -> Unit,
    onDone: (List<Mark>) -> Unit,
) {
    val selectedIdxs = remember { mutableStateListOf<Int>() }

    SelectMarksScreen(
        marks = marks,
        selectedIdxs = selectedIdxs,
        onSelect = { selectedIdxs.add(it) },
        onDeselect = { selectedIdxs.remove(it) },
        onClearSelection = { selectedIdxs.clear() },
        onBack = onBack,
        onDone = {
            val marks = marks!!.filterIndexed { idx, _ -> selectedIdxs.contains(idx) }
            onDone(marks)
        }
    )
}