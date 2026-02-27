package com.idloquy.landmark.ui.shared_marks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idloquy.landmark.data.repository.exceptions.GroupAlreadyExistsException
import com.idloquy.landmark.data.repository.exceptions.InvalidEditTokenException
import com.idloquy.landmark.data.repository.exceptions.InvalidGroupIdException
import com.idloquy.landmark.ui.shared_marks.exceptions.InvalidServerResponseException
import com.idloquy.landmark.ui.shared_marks.exceptions.TemporaryServerException

@Composable
fun SharedMarksErrorDialog(
    title: String,
    error: Exception,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                when (error) {
                    is InvalidServerResponseException -> {
                        if (error.message != null) {
                            Text("Received an invalid response from the server: ${error.message}")
                        } else {
                            Text("Received an invalid response from the server.")
                        }

                        Spacer(Modifier.height(10.dp))

                        Text("This is likely a bug. Please file a bug report.")
                    }

                    is InvalidGroupIdException -> {
                        Text("Invalid group ID: ${error.message}")
                    }

                    is GroupAlreadyExistsException -> {
                        Text("Group already exists")
                    }

                    is InvalidEditTokenException -> {
                        Text("Invalid edit token")
                    }

                    is TemporaryServerException -> {
                        var message: String? = null
                        var e: Throwable? = error
                        while (e != null) {
                            if (e.message != null) {
                                message = e.message
                                break
                            }
                            e = e.cause
                        }

                        if (message != null) {
                            Text("Temporary error: $message")
                        } else {
                            Text("Temporary error.")
                        }

                        Spacer(Modifier.height(10.dp))

                        Text("Please check your network connection and retry. If the issue persists, please file a bug report.")
                    }

                    else -> {
                        Text("Please check your network connection and retry. If the issue persists, please file a bug report.")
                    }
                }
            }
        },
        dismissButton = when (error) {
            is InvalidServerResponseException -> null
            is InvalidGroupIdException -> null
            is GroupAlreadyExistsException -> null
            is InvalidEditTokenException -> null
            is TemporaryServerException -> {
                if (onRetry != null) {
                    {
                        TextButton(
                            onClick = onDismiss,
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    null
                }
            }

            else -> {
                {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        },
        confirmButton = {
            when (error) {
                is InvalidServerResponseException, is InvalidGroupIdException, is GroupAlreadyExistsException, is InvalidEditTokenException -> {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }

                is TemporaryServerException -> {
                    if (onRetry != null) {
                        TextButton(
                            onClick = onRetry,
                        ) {
                            Text("Retry")
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                        ) {
                            Text("OK")
                        }
                    }
                }

                else -> {
                    if (onRetry != null) {
                        TextButton(
                            onClick = onRetry,
                        ) {
                            Text("Retry")
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    )
}