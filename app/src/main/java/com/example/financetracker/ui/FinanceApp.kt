package com.example.financetracker.ui

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.financetracker.broadcast.RecordBroadcast
import com.example.financetracker.broadcast.RecordChangeReceiver
import com.example.financetracker.ui.navigation.FinanceNavHost

@Composable
fun FinanceApp(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current

    DisposableEffect(context) {
        val receiver = RecordChangeReceiver { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(RecordBroadcast.ACTION_RECORD_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    NotificationPermissionRequest()

    FinanceNavHost(navController = navController)
}

@Composable
private fun NotificationPermissionRequest() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    var permissionAsked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted && !permissionAsked) {
            permissionAsked = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
