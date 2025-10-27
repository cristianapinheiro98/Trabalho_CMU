package pt.ipp.estg.trabalho_cmu.ui.components.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Vaccines
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.AdminHomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.PedidosAdocao
import pt.ipp.estg.trabalho_cmu.ui.screens.StartScreen.HomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.StartScreen.LoginScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.StartScreen.RegisterScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.StartScreen.RegistoAnimal

// ✅ 1. Corrigido: organização + tipo e imports limpos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerScaffold() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val drawerItems = prepareDrawerItems()
    var selectedItem by remember { mutableStateOf(drawerItems[0]) }
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val gesturesEnabled = currentRoute !in listOf("Start", "LoginScreen", "RegisterScreen")

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                    if (currentRoute != "Start" && currentRoute != "LoginScreen" && currentRoute != "RegisterScreen")
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
    NavHost(navController = navController, startDestination = "Start") {
        composable("Start") {
            HomeScreen(
                onLoginClick = { navController.navigate("LoginScreen") },
                onRegisterClick = { navController.navigate("RegisterScreen") })
        }
        composable("AdminHome") {
            AdminHomeScreen(
                onRegisterClick = { navController.navigate("RegistoAnimal") },
                onRequestsClick = { navController.navigate("PedidosAdocao") }
            )
        }
        composable("RegistoAnimal") {
            RegistoAnimal(
                onGuardar = { animalForm ->
                    println("A guardar o animal: ${animalForm.nome}")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("LoginScreen") {
            LoginScreen(onLoginSuccess = {
                // ✅ navegar para o ecrã principal
                navController.navigate("AdminHome") {
                    popUpTo("Start") { inclusive = true } // limpa o ecrã inicial
                    launchSingleTop = true
                }
            }

            )
        }
        composable("RegisterScreen") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },// Botão "Voltar"
                onRegisterSuccess = {
                    // Após criar conta → vai para o Login
                    navController.navigate("LoginScreen") {
                        // Remove "RegisterScreen" da back stack para não voltar para aqui ao pressionar Back
                        popUpTo("RegisterScreen") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("PedidosAdocao") { PedidosAdocao() }

        // ⚠️ As rotas seguintes eram chamadas no Drawer mas não existiam — adicionei-as
        composable("Veterinários") { Text("Ecrã de Veterinários") }
        composable("Perfil") { Text("Ecrã de Perfil") }
        composable("Notificações") { Text("Ecrã de Notificações") }
        composable("Sair") { Text("Sessão terminada") }
    }
}


// ✅ Corrigido: label e ícones em lista coerente com o NavHost
private fun prepareDrawerItems(): List<DrawerData> {
    return listOf(
        DrawerData("Veterinários", Icons.Outlined.Vaccines),
        DrawerData("Perfil", Icons.Outlined.Person),
        DrawerData("Notificações", Icons.Outlined.Notifications),
        DrawerData("Sair", Icons.Outlined.ExitToApp)
    )
}

// ✅ Corrigido: removeu erro de sintaxe no fim da classe (faltava linha separadora)
data class DrawerData(
    val label: String,
    val icon: ImageVector
)
