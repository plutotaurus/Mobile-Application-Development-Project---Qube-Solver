package metropolia.myapplication.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import metropolia.myapplication.data.local.UserDao

@Composable
internal fun EditProfileDialog(dao: UserDao, username: String, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var userId by remember { mutableStateOf<String?>(null) }
    var method by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(dao, username) {
        try {
            val user = requireNotNull(dao.findByUsername(username))
            method = user.solvingMethod.orEmpty()
            brand = user.cubeBrand.orEmpty()
            email = user.email.orEmpty()
            userId = user.userId
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            error = "Couldn’t load your profile."
        }
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onClose() },
        title = { Text("Edit Profile") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(username, style = MaterialTheme.typography.titleMedium)
                if (userId != null) {
                    OutlinedTextField(method, { method = it }, enabled = !saving,
                        singleLine = true, label = { Text("Solving method") })
                    OutlinedTextField(brand, { brand = it }, enabled = !saving,
                        singleLine = true, label = { Text("Cube brand") })
                    OutlinedTextField(email, { email = it }, enabled = !saving,
                        singleLine = true, label = { Text("Email") })
                } else if (error == null) {
                    CircularProgressIndicator()
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = userId != null && !saving, onClick = {
                val id = userId ?: return@TextButton
                saving = true
                error = null
                scope.launch {
                    try {
                        check(dao.updateProfile(id, method.trim().ifEmpty { null },
                            brand.trim().ifEmpty { null }, email.trim().ifEmpty { null }) == 1)
                        onClose()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        error = "Couldn’t save your changes. Try again."
                    } finally {
                        saving = false
                    }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onClose) { Text("Cancel") } },
    )
}
