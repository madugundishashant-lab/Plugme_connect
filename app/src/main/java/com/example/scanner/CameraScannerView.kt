package com.example.scanner

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.PlugmeCoral
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.SyneFontFamily
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

@Composable
fun CameraScannerView(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // To prevent processing multiple callbacks after a successful scan
    val hasDetected = remember { AtomicBoolean(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner: BarcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                barcodeScanner.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("camera_scanner_view")) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy, barcodeScanner, hasDetected) { rawValue ->
                            onQrDetected(rawValue)
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Holographic Laser Scanner Overlay with Cutout Scrim, Corner Reticles & Animated Beam
        ScanningLaserOverlay(
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Custom Canvas-based visual scanning overlay:
 * 1. Semi-transparent dark scrim masking everything outside the viewfinder
 * 2. High-precision illuminated L-corner brackets with breathing pulse
 * 3. Animated vertical laser beam sweep with trailing holographic aura gradient
 * 4. Alignment crosshairs and live scanning feedback badge
 */
@Composable
private fun ScanningLaserOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser_transition")

    // Vertical sweep animation for the laser line
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_progress"
    )

    // Pulse animation for corner reticles and badge dot
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Subtle breathing scale for laser glow width
    val glowSpread by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_spread"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate viewfinder bounds (centered, slightly elevated to account for bottom sheet)
            val boxDimension = min(width * 0.74f, 260.dp.toPx())
            val left = (width - boxDimension) / 2f
            val top = (height - boxDimension) / 2f - 24.dp.toPx()
            val right = left + boxDimension
            val bottom = top + boxDimension
            val cornerRadiusPx = 24.dp.toPx()

            // 1. Dark Scrim with rounded-corner viewfinder cutout
            val scrimPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, width, height))
                addRoundRect(
                    RoundRect(
                        rect = Rect(left, top, right, bottom),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )
                )
            }
            drawPath(path = scrimPath, color = Color.Black.copy(alpha = 0.52f))

            // 2. Viewfinder subtle outer frame
            drawRoundRect(
                color = PlugmeCyan.copy(alpha = 0.22f),
                topLeft = Offset(left, top),
                size = Size(boxDimension, boxDimension),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 3. Four L-shaped illuminated Corner Reticles
            val cornerLength = 32.dp.toPx()
            val cornerStrokeWidth = 4.5.dp.toPx()
            val bracketColor = PlugmeCyan.copy(alpha = pulseAlpha)

            // Top-Left corner
            drawLine(
                color = bracketColor,
                start = Offset(left - 1.dp.toPx(), top + cornerRadiusPx),
                end = Offset(left - 1.dp.toPx(), top + cornerLength),
                strokeWidth = cornerStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(left + cornerRadiusPx, top - 1.dp.toPx()),
                end = Offset(left + cornerLength, top - 1.dp.toPx()),
                strokeWidth = cornerStrokeWidth,
                cap = StrokeCap.Round
            )
            drawArc(
                color = bracketColor,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(left - 1.dp.toPx(), top - 1.dp.toPx()),
                size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
                style = Stroke(width = cornerStrokeWidth)
            )

            // Top-Right corner
            drawLine(
                color = bracketColor,
                start = Offset(right + 1.dp.toPx(), top + cornerRadiusPx),
                end = Offset(right + 1.dp.toPx(), top + cornerLength),
                strokeWidth = cornerStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(right - cornerLength, top - 1.dp.toPx()),
                end = Offset(right - cornerRadiusPx, top - 1.dp.toPx()),
                strokeWidth = cornerStrokeWidth,
                cap = StrokeCap.Round
            )
            drawArc(
                color = bracketColor,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(right - cornerRadiusPx * 2 + 1.dp.toPx(), top - 1.dp.toPx()),
                size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
                style = Stroke(width = cornerStrokeWidth)
            )

            // Bottom-Left corner
            drawLine(
                color = bracketColor,
                start = Offset(left - 1.dp.toPx(), bottom - cornerLength),
                end = Offset(left - 1.dp.toPx(), bottom - cornerRadiusPx),
                strokeWidth = cornerStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(left + cornerRadiusPx, bottom + 1.dp.toPx()),
                end = Offset(left + cornerLength, bottom + 1.dp.toPx()),
                strokeWidth = cornerStrokeWidth,
                cap = StrokeCap.Round
            )
            drawArc(
                color = bracketColor,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(left - 1.dp.toPx(), bottom - cornerRadiusPx * 2 + 1.dp.toPx()),
                size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
                style = Stroke(width = cornerStrokeWidth)
            )

            // Bottom-Right corner
            drawLine(
                color = bracketColor,
                start = Offset(right + 1.dp.toPx(), bottom - cornerLength),
                end = Offset(right + 1.dp.toPx(), bottom - cornerRadiusPx),
                strokeWidth = cornerStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(right - cornerLength, bottom + 1.dp.toPx()),
                end = Offset(right - cornerRadiusPx, bottom + 1.dp.toPx()),
                strokeWidth = cornerStrokeWidth,
                cap = StrokeCap.Round
            )
            drawArc(
                color = bracketColor,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(right - cornerRadiusPx * 2 + 1.dp.toPx(), bottom - cornerRadiusPx * 2 + 1.dp.toPx()),
                size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
                style = Stroke(width = cornerStrokeWidth)
            )

            // 4. Subtle central targeting crosshair marks
            val centerX = left + boxDimension / 2f
            val centerY = top + boxDimension / 2f
            val tickSize = 10.dp.toPx()
            val tickColor = Color.White.copy(alpha = 0.25f)
            drawLine(color = tickColor, start = Offset(centerX - tickSize, centerY), end = Offset(centerX + tickSize, centerY), strokeWidth = 1.dp.toPx())
            drawLine(color = tickColor, start = Offset(centerX, centerY - tickSize), end = Offset(centerX, centerY + tickSize), strokeWidth = 1.dp.toPx())

            // 5. Animated Laser Scanning Beam & Glow Aura
            val laserY = top + (boxDimension * laserProgress)
            val auraHeightPx = glowSpread.dp.toPx()
            val laserInset = 14.dp.toPx()
            val laserStartX = left + laserInset
            val laserEndX = right - laserInset

            // Trailing holographic glow gradient
            val auraBrush = Brush.verticalGradient(
                colors = listOf(
                    PlugmeCyan.copy(alpha = 0.0f),
                    PlugmeCyan.copy(alpha = 0.12f),
                    PlugmeCyan.copy(alpha = 0.38f),
                    PlugmeCoral.copy(alpha = 0.22f),
                    Color.Transparent
                ),
                startY = laserY - auraHeightPx,
                endY = laserY + 6.dp.toPx()
            )
            drawRect(
                brush = auraBrush,
                topLeft = Offset(laserStartX, laserY - auraHeightPx),
                size = Size(laserEndX - laserStartX, auraHeightPx + 6.dp.toPx())
            )

            // Secondary soft bottom aura
            val bottomGlowBrush = Brush.verticalGradient(
                colors = listOf(
                    PlugmeCyan.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                startY = laserY,
                endY = laserY + 12.dp.toPx()
            )
            drawRect(
                brush = bottomGlowBrush,
                topLeft = Offset(laserStartX, laserY),
                size = Size(laserEndX - laserStartX, 12.dp.toPx())
            )

            // Core high-intensity laser line with gradient
            val laserLineBrush = Brush.horizontalGradient(
                colors = listOf(
                    PlugmeCyan.copy(alpha = 0.2f),
                    PlugmeCyan,
                    Color.White,
                    PlugmeCoral,
                    PlugmeCyan.copy(alpha = 0.2f)
                ),
                startX = laserStartX,
                endX = laserEndX
            )
            drawLine(
                brush = laserLineBrush,
                start = Offset(laserStartX, laserY),
                end = Offset(laserEndX, laserY),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Glowing end caps / spark dots
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(laserStartX + 2.dp.toPx(), laserY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(laserEndX - 2.dp.toPx(), laserY)
            )
        }

        // Live Scanning Status Pill centered above the reticle
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 290.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PlugmeCyan.copy(alpha = pulseAlpha))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SCANNING ACTIVE",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxy(
    imageProxy: ImageProxy,
    barcodeScanner: BarcodeScanner,
    hasDetected: AtomicBoolean,
    onSuccess: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null && !hasDetected.get()) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val raw = barcode.rawValue
                    if (!raw.isNullOrBlank() && hasDetected.compareAndSet(false, true)) {
                        onSuccess(raw)
                        break
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
