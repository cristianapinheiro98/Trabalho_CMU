package pt.ipp.estg.trabalho_cmu.ui.screens.User

import android.app.Activity
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
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.preferences.LanguagePreferences
import pt.ipp.estg.trabalho_cmu.utils.LocaleHelper

/*@Composable
fun PreferencesScreen(
    windowSize: WindowWidthSizeClass,
    userViewModel: UserViewModel = viewModel(),
    userId: String
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
                    activity?.recreate()
                }) {
                    Text(confirm)
                }
            }
        )
    }


}

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
        PreferencesScreen(userId = "preview_id", windowSize = WindowWidthSizeClass.Compact)
    }
}*/
@Composable
fun PreferencesScreen(
    windowSize: WindowWidthSizeClass,
    userViewModel: UserViewModel = viewModel(),
    userId: String
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

    val isTablet = windowSize == WindowWidthSizeClass.Medium || windowSize == WindowWidthSizeClass.Expanded

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = if (isTablet) 800.dp else 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(if (isTablet) 32.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (isTablet) 24.dp else 16.dp)
        ) {
            PreferenceCards(
                isTablet = isTablet,
                notificationsEnabled = notificationsEnabled,
                onNotificationsChange = { notificationsEnabled = it },
                selectedLanguage = selectedLanguage,
                onLanguageChange = { lang, newContext ->
                    selectedLanguage = lang
                    dialogContext = newContext
                    showLanguageChangedDialog = true
                },
                user = user
            )
        }
    }

    if (showLanguageChangedDialog) {
        LanguageChangedDialog(
            dialogContext = dialogContext,
            onDismiss = { showLanguageChangedDialog = false },
            onConfirm = {
                showLanguageChangedDialog = false
                activity?.recreate()
            }
        )
    }
}

@Composable
private fun PreferenceCards(
    isTablet: Boolean,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String, android.content.Context) -> Unit,
    user: User?
) {
    if (isTablet) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PreferenceCard(
                modifier = Modifier.weight(1f),
                isTablet = isTablet
            ) {
                NotificationsContent(isTablet, notificationsEnabled, onNotificationsChange)
            }

            PreferenceCard(
                modifier = Modifier.weight(1f),
                isTablet = isTablet
            ) {
                LanguageContent(isTablet, selectedLanguage, onLanguageChange)
            }
        }
    } else {
        PreferenceCard(
            modifier = Modifier.fillMaxWidth(),
            isTablet = isTablet
        ) {
            NotificationsContent(isTablet, notificationsEnabled, onNotificationsChange)
        }

        PreferenceCard(
            modifier = Modifier.fillMaxWidth(),
            isTablet = isTablet
        ) {
            LanguageContent(isTablet, selectedLanguage, onLanguageChange)
        }
    }

    PreferenceCard(
        modifier = Modifier.fillMaxWidth(),
        isTablet = isTablet
    ) {
        UserInfoContent(isTablet, user)
    }
}

@Composable
private fun PreferenceCard(
    modifier: Modifier = Modifier,
    isTablet: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTablet) 2.dp else 0.dp)
    ) {
        Box(modifier = Modifier.padding(if (isTablet) 24.dp else 16.dp)) {
            content()
        }
    }
}

@Composable
private fun NotificationsContent(
    isTablet: Boolean,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.notifications_label),
            fontSize = if (isTablet) 18.sp else 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C3E50)
        )
        Switch(
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsChange
        )
    }
}

@Composable
private fun LanguageContent(
    isTablet: Boolean,
    selectedLanguage: String,
    onLanguageChange: (String, android.content.Context) -> Unit
) {
    val context = LocalContext.current

    Column {
        Text(
            stringResource(R.string.language_label),
            fontSize = if (isTablet) 18.sp else 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C3E50)
        )
        Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(if (isTablet) 24.dp else 16.dp)) {
            listOf("PT" to "pt", "EN" to "en").forEach { (lang, code) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedLanguage == lang,
                        onClick = {
                            val newContext = LocaleHelper.setLocale(context, code)
                            LanguagePreferences.saveLanguage(context, code)
                            onLanguageChange(lang, newContext)
                        }
                    )
                    Text(
                        text = lang,
                        fontSize = if (isTablet) 16.sp else 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UserInfoContent(
    isTablet: Boolean,
    user: User?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
    ) {
        if (user != null) {
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UserInfoRow(Icons.Outlined.Person, user.name, isTablet)
                        UserInfoRow(Icons.Outlined.Home, user.address, isTablet)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UserInfoRow(Icons.Outlined.Phone, user.phone, isTablet)
                        UserInfoRow(Icons.Outlined.Email, user.email, isTablet)
                    }
                }
            } else {
                UserInfoRow(Icons.Outlined.Person, user.name, isTablet)
                UserInfoRow(Icons.Outlined.Home, user.address, isTablet)
                UserInfoRow(Icons.Outlined.Phone, user.phone, isTablet)
                UserInfoRow(Icons.Outlined.Email, user.email, isTablet)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTablet) 150.dp else 100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 4.dp))

        OutlinedButton(
            onClick = { /* TODO: editar dados */ },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isTablet) Modifier.height(48.dp) else Modifier),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                stringResource(R.string.edit_profile_label),
                fontSize = if (isTablet) 16.sp else 14.sp
            )
        }
    }
}

@Composable
private fun LanguageChangedDialog(
    dialogContext: android.content.Context,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogContext.getString(R.string.language_changed_title)) },
        text = { Text(dialogContext.getString(R.string.language_changed_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(dialogContext.getString(R.string.go_back_home))
            }
        }
    )
}

@Composable
fun UserInfoRow(icon: ImageVector, text: String, isTablet: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2C3E50),
            modifier = Modifier.size(if (isTablet) 26.dp else 24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = if (isTablet) 16.sp else 14.sp,
            color = Color(0xFF555555)
        )
    }
}

@Preview(name = "Phone", widthDp = 360, heightDp = 640)
@Composable
private fun PreferenceScreenPhonePreview() {
    MaterialTheme {
        PreferencesScreen(userId = "preview_id", windowSize = WindowWidthSizeClass.Compact)
    }
}

@Preview(name = "Tablet", widthDp = 800, heightDp = 1280)
@Composable
private fun PreferenceScreenTabletPreview() {
    MaterialTheme {
        PreferencesScreen(userId = "preview_id", windowSize = WindowWidthSizeClass.Expanded)
    }
}
