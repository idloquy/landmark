package com.idloquy.landmark.ui.shared_marks.share_marks

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.idloquy.landmark.ui.shared_marks.share_marks.ShareMarksViewModel
import com.idloquy.landmark.ui.theme.LandmarkTheme
import kotlinx.coroutines.launch

@Composable
fun ShareMarksDoneScreen(
    groupId: String,
    onBack: () -> Unit,
) {
    ShareMarksDoneContent(
        groupId = groupId,
        onBack = onBack,
    )
}

// NOTE: unlike the other screens, this screen needs to handle the back button, so the onBack callback
//  is also called in that case.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareMarksDoneContent(
    groupId: String?,
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showBackConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (groupId == null) {
            showBackConfirmationDialog = true
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Share marks",
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
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { paddingValues ->
        if (groupId == null) {
            Column(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Sharing marks...",
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 20.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "Marks uploaded",
                    style = MaterialTheme.typography.headlineMedium,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Share the ID below with the people you want to share the marks with",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth().clickable {
                            coroutineScope.launch {
                                clipboard.setClipEntry(
                                    ClipEntry(
                                        ClipData.newPlainText(
                                            "mark group ID",
                                            groupId
                                        )
                                    )
                                )
                                Toast(context).apply { setText("Group ID copied to clipboard") }
                                    .show()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = groupId.uppercase(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center,
                            )

                            IconButton(
                                onClick = {}
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy to clipboard",
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Tap to copy",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        if (showBackConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showBackConfirmationDialog = false },
                title = {
                    Text(
                        "Are you sure you want to go back?"
                    )
                },
                text = {
                    Text("This will abort the upload of the marks")
                },
                dismissButton = {
                    TextButton(onClick = { showBackConfirmationDialog = false }) {
                        Text("Cancel")
                    }
                },
                confirmButton = {
                    TextButton(onClick = onBack) {
                        Text("Confirm")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShareMarksDonePreview() {
    LandmarkTheme {
        ShareMarksDoneContent(
            groupId = "8be4df61-93ca-11d2-aa0d-00e098032b8c".uppercase(),
            onBack = {},
        )
    }
}