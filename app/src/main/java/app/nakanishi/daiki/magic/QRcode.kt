package app.nakanishi.daiki.magic

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.nakanishi.daiki.magic.ui.theme.AppTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.WriterException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import android.Manifest
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun generateQRCode(content: String): Bitmap? {
    val writer = QRCodeWriter()
    return try {
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestDialog(
    recipientId: String,
    recipientName: String,
    onDismiss: () -> Unit,
    onSendRequest: (String, String) -> Unit
) {
    val context = LocalContext.current
    val userName = PreferenceManager.getName(context)
    val userId = PreferenceManager.getDocumentPath(context)
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDismiss()
            },
            title = { Text(text = "Friend Request") },
            text = { Text(text = "Send a friend request to $recipientName ($recipientId)?") },
            confirmButton = {
                TextButton(onClick = {
                    onSendRequest(userName, recipientId)
                    showDialog = false
                    onDismiss()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDismiss()
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
fun sendFriendRequestByQrCode( recipientId: String,userId: String) {
    val db = FirebaseFirestore.getInstance()

    if (userId != null) {
        val batch = db.batch()
        val recipientRef = db.collection("users").document(recipientId)
        val senderRef = db.collection("users").document(userId)

        batch.update(recipientRef, "requestedBy", FieldValue.arrayUnion(userId))
        batch.update(senderRef, "requesting", FieldValue.arrayUnion(recipientId))

        batch.commit()
            .addOnSuccessListener {
                Log.d("Firestore", "Friend request sent successfully")
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to send friend request", it)
            }
    } else {
        Log.e("Firestore", "User ID is null")
    }
}
@Composable
fun QrScanMainScreen(navController: NavController) {
    var scannedId by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var recipientName by remember { mutableStateOf<String?>(null) }
    val userId = PreferenceManager.getDocumentPath(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    // QRコードスキャン画面を表示
    ScanQRCodeScreen(
        navController = navController,
        onUserIdScanned = { id ->
            scannedId = id
            showDialog = true
        }
    )

    // ダイアログを表示してフレンドリクエストを送信
    if (showDialog && scannedId != null) {
        // 非同期にユーザー名を取得
        LaunchedEffect(scannedId) {
            recipientName = getUserName(scannedId!!)
        }

        if (recipientName != null) {
            FriendRequestDialog(
                recipientId = scannedId!!,
                recipientName = recipientName!!,
                onDismiss = {
                    scannedId = null
                    showDialog = false
                },
                onSendRequest = { senderName, recipientId ->
                    if (userId != null) {
                        sendFriendRequestByQrCode(recipientId, userId)
                    }
                }
            )
        }
    }
}
suspend fun getUserName(recipientId: String): String? {
    val db = FirebaseFirestore.getInstance()
    return try {
        val document = db.collection("users").document(recipientId).get().await()
        document.getString("name")
    } catch (e: Exception) {
        Log.e("Firestore", "Error getting user name", e)
        null
    }
}
@Composable
fun QRCodeScreen(navController: NavController, userId: String) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(userId) {
        qrBitmap = generateQRCode(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        qrBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(256.dp)
            )
        }
    }
}
@Composable
fun ScanQRCodeScreen(
    navController: NavController,
    onUserIdScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()

    // Check and request camera permission
    val cameraPermissionState = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            cameraPermissionState.value = granted
        }
    )

    DisposableEffect(Unit) {
        val permission = Manifest.permission.CAMERA
        cameraPermissionState.value = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        if (!cameraPermissionState.value) {
            launcher.launch(permission)
        }

        onDispose {
            // Clean up code if needed
        }
    }

    if (cameraPermissionState.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView?.surfaceProvider)
                            }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(ctx),
                            QRCodeAnalyzer { userId ->
                                onUserIdScanned(userId)
                            }
                        )

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

                    previewView!!
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Text(text = "Camera permission is required to scan QR codes")
    }
}

class QRCodeAnalyzer(
    private val onUserIdScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val width = image.width
        val height = image.height
        val source = PlanarYUVLuminanceSource(bytes, width, height, 0, 0, width, height, false)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = MultiFormatReader().decode(binaryBitmap)
            Log.d("QRCodeAnalyzer", "QR Code detected: ${result.text}")
            result?.let {
                onUserIdScanned(it.text)
            }
        } catch (e: NotFoundException) {
            Log.d("QRCodeAnalyzer", "QR Code not found")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }
}
@Composable
fun FriendAddScreen(navController: NavController, friendId: String) {
    val context = LocalContext.current

    LaunchedEffect(friendId) {
        addFriend(context, friendId) { success ->
            if (success) {
                navController.popBackStack()
            } else {
                // エラー処理
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ){
        Text(
            "FriendAdd"
        )
    }
}

fun addFriend(context: Context, friendId: String, onComplete: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userId = PreferenceManager.getDocumentPath(context)

    if (userId != null) {
        val batch = db.batch()
        val userRef = db.collection("users").document(userId)
        val friendRef = db.collection("users").document(friendId)

        batch.update(userRef, "friends", FieldValue.arrayUnion(friendId))
        batch.update(friendRef, "friends", FieldValue.arrayUnion(userId))

        batch.commit().addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    } else {
        onComplete(false)
    }
}
