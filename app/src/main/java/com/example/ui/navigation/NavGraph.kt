package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.UserProfileStore
import com.example.data.repository.AiRepository
import com.example.ui.animation.Animation2D3DScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.history.ChatHistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.image.ImageGenScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.video.AiVideoStudioScreen
import com.example.util.VoiceInputManager
import com.example.util.VoiceReplyManager

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Chat : Screen("chat?sessionId={sessionId}", "Chat", Icons.Default.Chat)
    object ImageGen : Screen("image_gen", "Image", Icons.Default.Image)
    object VideoStudio : Screen("video_studio", "Video", Icons.Default.VideoLibrary)
    object Animation : Screen("animation", "3D / 2D", Icons.Default.ViewInAr)
    object History : Screen("history", "History")
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@Composable
fun AppNavigation(
    repository: AiRepository,
    profileStore: UserProfileStore,
    voiceInputManager: VoiceInputManager,
    voiceReplyManager: VoiceReplyManager,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Chat,
        Screen.ImageGen,
        Screen.VideoStudio,
        Screen.Animation,
        Screen.Profile
    )

    // Show bottom bar on primary tabs
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.ImageGen.route,
        Screen.VideoStudio.route,
        Screen.Animation.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_nav"),
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute?.startsWith(screen.route.substringBefore("?")) == true
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (screen == Screen.Chat) {
                                    navController.navigate("chat?sessionId=-1")
                                } else if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { screen.icon?.let { Icon(imageVector = it, contentDescription = screen.title) } },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                                indicatorColor = CyberCyan,
                                selectedTextColor = PrimaryVioletLight
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.title.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    profileStore = profileStore,
                    onNavigateToChat = { sessionId ->
                        val dest = if (sessionId != null && sessionId > 0L) "chat?sessionId=$sessionId" else "chat?sessionId=-1"
                        navController.navigate(dest)
                    },
                    onNavigateToImageGen = { navController.navigate(Screen.ImageGen.route) },
                    onNavigateToVideoStudio = { navController.navigate(Screen.VideoStudio.route) },
                    onNavigateToAnimation = { navController.navigate(Screen.Animation.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToVoice = { navController.navigate("chat?sessionId=-1") }
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("sessionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
                ChatScreen(
                    sessionId = if (sessionId > 0) sessionId else null,
                    repository = repository,
                    profileStore = profileStore,
                    voiceInputManager = voiceInputManager,
                    voiceReplyManager = voiceReplyManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ImageGen.route) {
                ImageGenScreen(
                    repository = repository,
                    profileStore = profileStore,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.VideoStudio.route) {
                AiVideoStudioScreen(
                    repository = repository,
                    profileStore = profileStore,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Animation.route) {
                Animation2D3DScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                ChatHistoryScreen(
                    repository = repository,
                    onSelectSession = { sessionId ->
                        navController.navigate("chat?sessionId=$sessionId")
                    },
                    onNewChat = {
                        navController.navigate("chat?sessionId=-1")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    profileStore = profileStore,
                    repository = repository,
                    voiceReplyManager = voiceReplyManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
