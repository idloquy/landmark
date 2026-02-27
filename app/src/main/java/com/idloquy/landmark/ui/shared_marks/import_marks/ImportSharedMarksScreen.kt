package com.idloquy.landmark.ui.shared_marks.import_marks

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.idloquy.landmark.ui.shared_marks.ProgressIndicatorDialog
import com.idloquy.landmark.ui.shared_marks.SharedMarksErrorDialog
import java.util.UUID

@Composable
fun ImportSharedMarksScreen(
    viewModel: ImportSharedMarksViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
) {
    val context = LocalContext.current
    var indicatorText by rememberSaveable { mutableStateOf("") }
    var importError by rememberSaveable { mutableStateOf<Exception?>(null)}

    ImportSharedMarksContent(
        onBack = onBack,
        onImportMarks = { id ->
            indicatorText = "Importing mark group..."
            viewModel.importMarkGroup(
                id = id,
                onSuccess = {
                    Toast(context).apply { setText("Mark group imported successfully") }.show()
                    indicatorText = ""
                    onSuccess(id)
                },
                onError = {
                    Log.d("landmark", "got error: $it")
                    indicatorText = ""
                    importError = it
                }
            )
        }
    )

    if (importError != null) {
        Log.d("landmark", "import error: $importError")
        SharedMarksErrorDialog(
            title = "Failed to import marks",
            error = importError!!,
            onDismiss = { importError = null },
            onRetry = null,
        )
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
fun ImportSharedMarksContent(
    onBack: () -> Unit,
    onImportMarks: (String) -> Unit
) {
    var groupId by rememberSaveable { mutableStateOf("") }
    var groupIdError by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
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
                        )
                    }
                }, actions = {
                    TextButton(
                        onClick = {
                            val groupId = groupId.trim()
                            runCatching { UUID.fromString(groupId) }
                                .onSuccess { onImportMarks(groupId) }
                                .onFailure { groupIdError = "Invalid group ID" }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    ) {
                        Text("Next")
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
                .padding(paddingValues)
                .padding(top = 20.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Import shared marks",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "This will import a group of marks. The imported mark group will be displayed in the shared marks screen.\nThe ID of the group needs to be specified below.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Group ID") },
                value = groupId,
                onValueChange = { groupId = it },
                singleLine = true,
                isError = groupIdError.isNotEmpty(),
                supportingText = if (groupIdError.isNotEmpty()) {
                    {
                        Text(groupIdError)
                    }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val groupId = groupId.trim()
                    runCatching { UUID.fromString(groupId) }
                        .onSuccess { onImportMarks(groupId) }
                        .onFailure { groupIdError = "Invalid group ID" }
                }),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ImportSharedMarksPreview() {
    ImportSharedMarksContent(
        onBack = {},
        onImportMarks = {},
    )
}