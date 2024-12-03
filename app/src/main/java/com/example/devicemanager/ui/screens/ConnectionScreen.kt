package com.example.devicemanager.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.devicemanager.R
import com.example.devicemanager.viewmodels.ConnectionViewModel
import com.example.devicemanager.ui.theme.*

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = viewModel(),
    onNavigateToDeviceScreen: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = BootstrapWhite,
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BootstrapBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.device_manager),
                contentDescription = "Device Manager Icon",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Device Manager Setup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BootstrapText
            )

            OutlinedTextField(
                value = uiState.deviceId,
                onValueChange = { viewModel.updateDeviceId(it) },
                label = { Text("Enter Server IP Address", color = BootstrapText) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BootstrapPrimary,
                    unfocusedBorderColor = BootstrapBorder,
                    cursorColor = BootstrapPrimary,
                    focusedLabelColor = BootstrapPrimary
                )
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    viewModel.connect(context, onNavigateToDeviceScreen)

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BootstrapPrimary,
                    contentColor = BootstrapWhite
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = BootstrapWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Connect",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
