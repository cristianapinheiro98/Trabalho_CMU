package pt.ipp.estg.trabalho_cmu.ui.screens.user

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.preferences.LanguagePreferences
import pt.ipp.estg.trabalho_cmu.utils.LocaleHelper

/**
 * Screen responsible for displaying and managing user preferences and profile information.
 *
 * This screen allows the user to:
 * - View personal details (Name, Address, Phone, Email) loaded from the database.
 * - Toggle notification settings.
 * - Change the application language (PT/EN), which triggers a locale update and activity restart.
 *
 * @param userViewModel The ViewModel responsible for fetching user data.
 * @param userId The unique identifier of the current user.
 * @param navController Controller for handling navigation events.
 */
@Composable
fun PreferencesScreen(
    userViewModel: UserViewModel = viewModel(),
    userId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedLanguage by remember {
        mutableStateOf(
            if (LanguagePreferences.getLanguage(context) == "pt") "PT" else "EN"
        )
    }

    LaunchedEffect(userId) {
        userViewModel.loadUserById(userId)
    }

    val user by userViewModel.user.observeAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLanguageChangedDialog by remember { mutableStateOf(false) }
    var dialogContext by remember { mutableStateOf(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.notifications_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C3E50)
                )
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    stringResource(R.string.language_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedLanguage == "PT",
                            onClick = {
                                selectedLanguage = "PT"
                                val newContext = LocaleHelper.setLocale(context, "pt")
                                LanguagePreferences.saveLanguage(context, "pt")
                                dialogContext = newContext
                                showLanguageChangedDialog = true
                            }
                        )
                        Text("PT")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedLanguage == "EN",
                            onClick = {
                                selectedLanguage = "EN"
                                val newContext = LocaleHelper.setLocale(context, "en")
                                LanguagePreferences.saveLanguage(context, "en")
                                dialogContext = newContext
                                showLanguageChangedDialog = true
                            }
                        )
                        Text("EN")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (user != null) {
                    UserInfoRow(Icons.Outlined.Person, user!!.name)
                    UserInfoRow(Icons.Outlined.Home, user!!.address)
                    UserInfoRow(Icons.Outlined.Phone, user!!.phone)
                    UserInfoRow(Icons.Outlined.Email, user!!.email)
                } else {
                    CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { /* TODO: editar dados */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.edit_profile_label))
                }
            }
        }
    }

    if (showLanguageChangedDialog) {
        val title = dialogContext.getString(R.string.language_changed_title)
        val message = dialogContext.getString(R.string.language_changed_message)
        val confirm = dialogContext.getString(R.string.go_back_home)

        AlertDialog(
            onDismissRequest = { showLanguageChangedDialog = false },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    showLanguageChangedDialog = false

                    activity?.let { act ->
                        val intent = act.intent
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        act.finish()
                        act.startActivity(intent)
                    }
                }) {
                    Text(confirm)
                }
            }
        )
    }
}

/**
 * Reusable helper component to display a single row of user information.
 *
 * Renders an icon on the left followed by the corresponding text information.
 * Used within the profile card to display fields like Email or Phone.
 *
 * @param icon The visual icon representing the data type (e.g., Email, Phone).
 * @param text The actual user data string to display.
 */
@Composable
fun UserInfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2C3E50),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )
    }
}

@Preview
@Composable
private fun PreferenceScreenPreview() {
    MaterialTheme {
        PreferencesScreen(
            userId = "preview_id",
            navController = rememberNavController()
        )
    }
}
