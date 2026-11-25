package metropolia.myapplication.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import metropolia.myapplication.data.local.PasswordHasher
import metropolia.myapplication.data.local.UserEntity
import metropolia.myapplication.data.local.rememberQubeDatabase
import kotlin.uuid.Uuid

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String) -> Unit
) {
    val database = rememberQubeDatabase()
    val scope = rememberCoroutineScope()
    var usernameInput by remember { mutableStateOf("SpeedySam") }
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account Authentication") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        usernameInput = it
                        errorMessage = null
                    },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val nameToUse = usernameInput.ifBlank { "SpeedySam" }
                        scope.launch {
                            val existingUser = database.users().findByUsername(nameToUse)
                            if (existingUser != null) {
                                errorMessage = "User already exists! Please log in."
                            } else {
                                val hash = PasswordHasher.hashPassword(passwordInput)
                                val newUser = UserEntity(
                                    userId = Uuid.random().toString(),
                                    username = nameToUse,
                                    passwordHash = hash
                                )
                                database.users().save(newUser)
                                onLogin(nameToUse)
                                onDismiss()
                            }
                        }
                    }
                ) {
                    Text("Register")
                }

                Button(
                    onClick = {
                        val nameToUse = usernameInput.ifBlank { "SpeedySam" }
                        scope.launch {
                            val existingUser = database.users().findByUsername(nameToUse)
                            if (existingUser == null) {
                                errorMessage = "User not found! Please register first."
                            } else if (existingUser.passwordHash != null && !PasswordHasher.verifyPassword(passwordInput, existingUser.passwordHash)) {
                                errorMessage = "Invalid password!"
                            } else {
                                onLogin(nameToUse)
                                onDismiss()
                            }
                        }
                    }
                ) {
                    Text("Log In")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


