package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SageTeal
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate950
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * BarcodeScanner composable leveraging CameraX and Google ML Kit.
 * Scans 1D and 2D barcodes (QR code, UPC, Code 128, EAN, Data Matrix) in real-time
 * and triggers [onBarcodeScanned] when a code is identified.
 */
@Composable
fun BarcodeScanner(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    enableScanning: Boolean = true,
    lastScannedBarcode: String? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var isTorchEnabled by remember { mutableStateOf(false) }
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var lastScannedCode by remember { mutableStateOf<String?>(lastScannedBarcode) }
    var lastScannedTime by remember { mutableLongStateOf(0L) }

    // Pulsing laser animation in the viewfinder
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Slate950)
            .testTag("barcode_scanner_viewfinder"),
        contentAlignment = Alignment.Center
    ) {
        if (!hasCameraPermission) {
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = SageTeal,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Camera Access Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enable camera permission to scan warehouse item barcodes and QR codes via Google ML Kit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("grant_camera_permission_button")
                ) {
                    Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // CameraX Live Feed
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
            val barcodeScanner = remember {
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_ALL_FORMATS
                    )
                    .build()
                BarcodeScanning.getClient(options)
            }

            DisposableEffect(Unit) {
                onDispose {
                    cameraExecutor.shutdown()
                    barcodeScanner.close()
                }
            }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(
                                barcodeScanner = barcodeScanner,
                                imageProxy = imageProxy,
                                enableScanning = enableScanning,
                                onBarcodeFound = { rawBarcode ->
                                    val now = System.currentTimeMillis()
                                    // Cooldown of 1.2s to prevent multiple triggers for the same barcode
                                    if (rawBarcode != lastScannedCode || (now - lastScannedTime) > 1200) {
                                        lastScannedCode = rawBarcode
                                        lastScannedTime = now
                                        onBarcodeScanned(rawBarcode)
                                    }
                                }
                            )
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val cam = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraInstance = cam
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Viewfinder Reticle & Guidelines
            Box(
                modifier = Modifier
                    .size(240.dp, 160.dp)
                    .border(2.5.dp, SageTeal.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
            ) {
                // Animated Laser Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .padding(top = (160 * laserOffset).dp)
                        .background(SageTeal)
                )
            }

            // Top Status Bar & Torch Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Slate950.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (enableScanning) SageTeal else Color.Gray)
                    )
                    Text(
                        text = if (enableScanning) "ML Kit Scanner Active" else "Paused",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        isTorchEnabled = !isTorchEnabled
                        cameraInstance?.cameraControl?.enableTorch(isTorchEnabled)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isTorchEnabled) SageTeal else Slate800.copy(alpha = 0.8f))
                        .testTag("toggle_torch_button")
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle flashlight",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Scan Feedback
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate950.copy(alpha = 0.75f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = SageTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = lastScannedCode?.let { "Scanned: $it" } ?: "Align barcode inside reticle",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    enableScanning: Boolean,
    onBarcodeFound: (String) -> Unit
) {
    if (!enableScanning) {
        imageProxy.close()
        return
    }

    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        onBarcodeFound(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener {
                // Silently continue scanning frames
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
