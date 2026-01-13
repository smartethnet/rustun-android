package com.smartethnet.rustun.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartethnet.rustun.R
import com.smartethnet.rustun.datastore.configsDataStore
import com.smartethnet.rustun.proto.Config
import com.smartethnet.rustun.proto.ConfigList
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConfigListPage(onNewConfig: () -> Unit, onConnect: (Config) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val configList by context.configsDataStore.data.collectAsState(ConfigList.newBuilder().build())

    fun handleDelete(config: Config) {
        scope.launch {
            context.configsDataStore.updateData { oldData ->
                val list = oldData.configList.toMutableList()
                    .filterNot { it.name == config.name }
                ConfigList.newBuilder().addAllConfig(list).build()
            }
            snackbarHostState.showSnackbar("${config.name} 已删除")
        }
    }

    // UI
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(context.getString(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNewConfig) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {}
    ) { innerPadding ->
        when {
            configList.configList.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("请先添加一个配置吧!", style = MaterialTheme.typography.bodyLarge)
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .padding(
                            top = innerPadding.calculateTopPadding() + 24.dp,
                            bottom = innerPadding.calculateBottomPadding() + 24.dp,
                            start = 24.dp,
                            end = 24.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    configList.configList.forEach { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    handleDelete(item)
                                }

                                false
                            }
                        )

                        key(item.name) {
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = { DismissBackground() }
                            ) {
                                ListItem(item, onConnect)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListItem(item: Config, onClick: (Config) -> Unit = {}) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { onClick(item) }) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(item.server, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground() {
    OutlinedCard(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(24.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "delete",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}