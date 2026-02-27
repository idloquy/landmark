package com.idloquy.landmark.ui.shared_marks.share_marks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idloquy.landmark.ui.theme.LandmarkTheme

@Composable
fun ShareMarksGroupNameScreen(
    onBack: () -> Unit,
    onNext: (String) -> Unit,
) {
    ShareMarksGroupNameContent(
        onBack = onBack,
        onNext = onNext,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareMarksGroupNameContent(
    onBack: () -> Unit,
    onNext: (String) -> Unit,
) {
    var groupName by rememberSaveable { mutableStateOf("") }
    var groupNameError by rememberSaveable { mutableStateOf("") }

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
                actions = {
                    TextButton(
                        onClick = {
                            if (groupName.isNotBlank())
                                onNext(groupName)
                            else
                                groupNameError = "Group name must not be empty"
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    ) {
                        Text(
                            "Next",
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
        Column {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 20.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Choose a mark group name",
                    style = MaterialTheme.typography.headlineMedium,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "The group describes all marks in the group and helps distinguish groups from one another.",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Group name") },
                    onValueChange = { groupName = it },
                    isError = groupNameError.isNotEmpty(),
                    supportingText = if (groupNameError.isNotEmpty()) {
                        { Text(groupNameError) }
                    } else {
                        null
                    },
                    singleLine = true,
                    value = groupName,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShareMarksGroupNamePreview() {
    LandmarkTheme {
        ShareMarksGroupNameContent(
            onBack = {},
            onNext = {},
        )
    }
}