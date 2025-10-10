package com.kazox.posedetection

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun providePermissions(): Permissions {
    val context = LocalContext.current

    return remember {
        object : Permissions {
            override fun hasCameraPermission(): Boolean {
                return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }

            @Composable
            override fun RequestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (isGranted) {
                            onGranted()
                        } else {
                            onDenied()
                        }
                    }
                )

                val permissionStatus = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                )

                when (permissionStatus) {
                    PackageManager.PERMISSION_GRANTED -> onGranted()
                    PackageManager.PERMISSION_DENIED -> {

                        LaunchedEffect(Unit) {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            }
        }
    }
}