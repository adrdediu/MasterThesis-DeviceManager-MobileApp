package com.example.devicemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.devicemanager.viewmodels.ConnectionViewModel
import com.example.devicemanager.viewmodels.ConnectionState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import WebViewScreen

@Composable
fun ConnectionScreen() {
    val viewModel: ConnectionViewModel = viewModel()
    var ipAddress by remember { mutableStateOf("") }
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    
    when (connectionState) {
        is ConnectionState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ConnectionState.Connected -> {
            WebViewScreen((connectionState as ConnectionState.Connected).serverIp)
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(400.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Connect to Server",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = ipAddress,
                            onValueChange = { ipAddress = it },
                            label = { Text("Server IP Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.connectToServer(ipAddress) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Connect")
                        }
                        
                        if (connectionState is ConnectionState.Error) {
                            Text(
                                text = (connectionState as ConnectionState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

