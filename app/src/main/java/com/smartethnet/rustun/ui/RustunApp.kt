package com.smartethnet.rustun.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartethnet.rustun.datastore.configsDataStore
import com.smartethnet.rustun.proto.Config
import com.smartethnet.rustun.ui.pages.ConfigEditPage
import com.smartethnet.rustun.ui.pages.ConfigListPage
import com.smartethnet.rustun.ui.pages.ConnectPage
import com.smartethnet.rustun.ui.theme.RustunTheme
import com.smartethnet.rustun.viewmodel.ConfigViewModel
import com.smartethnet.rustun.viewmodel.ConnectViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    private val _currentConfig = MutableStateFlow<Config?>(null)
    val currentConfig = _currentConfig.asStateFlow()

    fun setCurrentConfig(config: Config) {
        _currentConfig.value = config
    }
}

@Composable
fun RustunApp() {
    val context = LocalContext.current
    val sharedViewModel: SharedViewModel = viewModel()
    val navController = rememberNavController()

    RustunTheme {
        NavHost(navController = navController, startDestination = Route.CONFIG_LIST) {
            // 配置列表页面
            composable(Route.CONFIG_LIST) {
                ConfigListPage(onNewConfig = {
                    navController.navigate(Route.CONFIG_EDIT)
                }, onConnect = { config ->
                    sharedViewModel.setCurrentConfig(config)
                    navController.navigate(Route.CONNECT)
                })
            }

            // 新增配置页面
            composable(Route.CONFIG_EDIT) {

                val viewModel: ConfigViewModel =
                    viewModel(factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return ConfigViewModel(context.configsDataStore) as T
                        }
                    })
                ConfigEditPage(
                    viewModel,
                    onBack = {
                        navController.popBackStack()
                        navController.navigate(Route.CONFIG_LIST) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }

            // 连接详情页面
            composable(Route.CONNECT) {
                val config = sharedViewModel.currentConfig.collectAsState().value!!
                val viewModel: ConnectViewModel =
                    viewModel(factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return ConnectViewModel(context) as T
                        }
                    })
                ConnectPage(config, viewModel, onBack = {
                    navController.popBackStack()
                    navController.navigate(Route.CONFIG_LIST) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                })
            }
        }
    }
}
