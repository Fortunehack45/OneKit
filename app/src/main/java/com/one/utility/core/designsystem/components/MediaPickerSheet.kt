package com.one.utility.core.designsystem.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.one.utility.core.designsystem.AppShapes
import com.one.utility.core.designsystem.AppTheme
import com.one.utility.core.designsystem.pressFeedback
import java.io.File

/**
 * Creates a temporary file in cacheDir and returns its content Uri via FileProvider.
 */
fun createTempCameraUri(context: Context): Uri {
    val tempFile = File.createTempFile("one_camera_${System.currentTimeMillis()}", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

/**
 * Reusable Camera and Gallery Capture Controller.
 */
class MediaCaptureController(
    val launchCamera: () -> Unit,
    val launchGallery: () -> Unit
)

@Composable
fun rememberMediaCapture(
    onImageSelected: (Uri) -> Unit
): MediaCaptureController {
    val context = LocalContext.current
    var currentCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCameraUri != null) {
            onImageSelected(currentCameraUri!!)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    return remember {
        MediaCaptureController(
            launchCamera = {
                val uri = createTempCameraUri(context)
                currentCameraUri = uri
                cameraLauncher.launch(uri)
            },
            launchGallery = {
                galleryLauncher.launch("image/*")
            }
        )
    }
}

/**
 * Dual-action Picker Sheet allowing users to either Take a Live Picture or Upload from Gallery.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPickerModalSheet(
    onDismissRequest: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.colors.surfaceCard,
        shape = AppShapes.Card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Select Image Source",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppTheme.colors.textPrimary
            )
            Text(
                text = "Take a new photo with camera or select an existing image from your device gallery.",
                fontSize = 13.sp,
                color = AppTheme.colors.textSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Take Photo with Camera
                Button(
                    onClick = {
                        onDismissRequest()
                        onTakePhoto()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .pressFeedback(),
                    shape = AppShapes.SubCard,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Camera", fontWeight = FontWeight.Bold)
                }

                // Choose from Gallery
                OutlinedButton(
                    onClick = {
                        onDismissRequest()
                        onChooseGallery()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .pressFeedback(),
                    shape = AppShapes.SubCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppTheme.colors.textPrimary
                    )
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
