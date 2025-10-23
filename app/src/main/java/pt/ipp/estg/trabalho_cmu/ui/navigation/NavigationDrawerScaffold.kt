package pt.ipp.estg.trabalho_cmu.ui.components.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.AdminHomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.register.PedidosAdocao
import pt.ipp.estg.trabalho_cmu.ui.screens.register.RegistoAnimal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerScaffold() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val drawerItems = prepareDrawerItems()
    var selectedItem by remember { mutableStateOf(drawerItems[0]) }
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Divider()
                drawerItems.forEach { item ->
                    DrawerItem(
                        item = item,
                        selected = item == selectedItem,
                        onItemClick = {
                            selectedItem = item
                            scope.launch { drawerState.close() }
                            navController.navigate(item.label)
                        }
                    )
                }
            }
        },
        content = {
            TailwaggerScaffold(drawerState, navController)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TailwaggerScaffold(drawerState: DrawerState, navController: NavHostController) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_collar),
                            contentDescription = "Tailwagger logo",
                            modifier = Modifier
                                .size(42.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "Tailwagger",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir menu",
                            tint = Color(0xFF37474F)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TailwaggerNavHost(navController)
            }
        }
    )
}

@Composable
fun DrawerItem(
    item: DrawerData,
    selected: Boolean,
    onItemClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(item.label) },
        selected = selected,
        onClick = onItemClick,
        icon = { Icon(item.icon, contentDescription = item.label) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun TailwaggerNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "Home") {
        composable( "Home"){ AdminHomeScreen(onRegisterClick = {navController.navigate("RegistoAnimal")},
            onRequestsClick = {navController.navigate("PedidosAdocao")}) }
        composable("RegistoAnimal") { RegistoAnimal() }
        composable("PedidosAdocao") { PedidosAdocao() }
        composable("Perfil") { Text("Ecrã de Perfil") }
        composable("Notificações") { Text("Ecrã de Notificações") }
        composable("Sair") { Text("Sessão terminada") }
    }
}

private fun prepareDrawerItems(): List<DrawerData> {
    return listOf(
        DrawerData("Perfil", Icons.Outlined.Person),
        DrawerData("Notificações", Icons.Outlined.Notifications),
        DrawerData("Sair", Icons.Outlined.ExitToApp)
    )
}

data class DrawerData(val label: String, val icon: ImageVector)
