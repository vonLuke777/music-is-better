package pl.speakersync.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.speakersync.ui.viewmodel.MainViewModel

object Routes {
    const val HOME = "home"
    const val GROUP = "group"
    const val MIRROR = "mirror"
    const val SETTINGS = "settings"
}

@Composable
fun SpeakerSyncNavHost(
    viewModel: MainViewModel = viewModel(),
    onStartMirroring: () -> Unit,
    onStopMirroring: () -> Unit,
    onOpenNotificationAccess: () -> Unit
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                uiState = uiState,
                viewModel = viewModel,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenGroupBuilder = { navController.navigate(Routes.GROUP) },
                onOpenNotificationAccess = onOpenNotificationAccess
            )
        }
        composable(Routes.GROUP) {
            GroupBuilderScreen(
                uiState = uiState,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenMirror = { navController.navigate(Routes.MIRROR) }
            )
        }
        composable(Routes.MIRROR) {
            MirrorScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onStartMirroring = onStartMirroring,
                onStopMirroring = onStopMirroring,
                onOpenNotificationAccess = onOpenNotificationAccess
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                autoDiscovery = settings.autoDiscovery,
                scanTimeout = settings.scanTimeoutSeconds,
                themeMode = settings.themeMode,
                onAutoDiscoveryChange = { enabled ->
                    viewModel.updateSettings(enabled, settings.scanTimeoutSeconds)
                },
                onScanTimeoutChange = { timeout ->
                    viewModel.updateSettings(settings.autoDiscovery, timeout)
                },
                onThemeModeChange = viewModel::updateThemeMode,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
