package pt.ipp.estg.trabalho_cmu.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class DrawerOption(val label: String, val icon: ImageVector, val route: String)

fun getUserDrawerOptions() = listOf(
    DrawerOption("Menu Principal", Icons.Outlined.Home, "UserHome"),
    DrawerOption("Perfil", Icons.Outlined.Person, "UserProfile"),
    DrawerOption("Catálogo de Animais", Icons.Outlined.Pets, "AnimalsCatalogue"),
    DrawerOption("Animais Favoritos", Icons.Outlined.FavoriteBorder, "Favourites"),
    DrawerOption("Comunidade SocialTails", Icons.Outlined.Groups, "Community"),
    DrawerOption("Lista de Veterinários", Icons.Outlined.Vaccines, "Veterinarians")
)

@Composable
fun DrawerUser(
    items: List<DrawerOption>,
    selected: DrawerOption?,
    onSelect: (DrawerOption) -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseDrawer) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Fechar menu"
                )
            }
        }

        Divider()
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = item == selected,
                onClick = { onSelect(item) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}