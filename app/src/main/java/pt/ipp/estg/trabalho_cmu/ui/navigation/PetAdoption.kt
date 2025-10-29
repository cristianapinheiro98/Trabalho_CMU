package pt.ipp.estg.trabalho_cmu.ui.navigation

import AppScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun PetAdoptionApp(
    isLoggedIn: Boolean = false,
    isAdmin: Boolean = false
) {
    MaterialTheme {
        AppScaffold(isLoggedIn = isLoggedIn, isAdmin = isAdmin)
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewPublicApp() {
    PetAdoptionApp(isLoggedIn = false, isAdmin = false)
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewUserApp() {
    PetAdoptionApp(isLoggedIn = true, isAdmin = false)
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewAdminApp() {
    PetAdoptionApp(isLoggedIn = true, isAdmin = true)
}
