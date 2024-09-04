package app.nakanishi.daiki.magic

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.nakanishi.daiki.magic.ui.theme.AppTheme
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID

fun clearPreferences(context: Context) {
    val sharedPreferences = context.getSharedPreferences("Profile", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        clear()  // 全ての値を削除
        apply()
    }
}
object PreferenceManager {
    private const val PREFS_NAME = "Profile"
    private const val KEY_NAME = "name"
    private const val KEY_DOCUMENT_PATH = "document_path"
    private const val KEY_IMAGE_ICON = "image_icon"

    fun saveName(context: Context, name: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_NAME, name)
            apply()
        }
    }

    fun getName(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_NAME, "") ?: ""
    }

    fun saveDocumentPath(context: Context, documentPath: String) {
        // ここでは、URIではなくFirestoreのドキュメントIDを保存するようにします。
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_DOCUMENT_PATH, documentPath)
            apply()
        }
    }

    fun getDocumentPath(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_DOCUMENT_PATH, null)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileActivity(navController: NavController) {
    var name by remember { mutableStateOf("NoName") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var userId by remember { mutableStateOf("") }
    var magic by remember { mutableStateOf(0) }
    val sharedPreferences = context.getSharedPreferences("Profile", Context.MODE_PRIVATE)
    var friendId by remember { mutableStateOf<List<String>>(emptyList()) }

    val doublelift = Color(0xffa0d8ef)
    val tilt = Color(0xff90ee90)
    val documentId2 = PreferenceManager.getDocumentPath(context)

    var imageUrl by remember { mutableStateOf<String?>(null) }
    var documentId by remember { mutableStateOf<String?>(null) }
    val userIdQr = "unique_user_id"

    var friendList by remember { mutableStateOf<List<String>>(emptyList()) }
    var requesting by remember { mutableStateOf<List<String>>(emptyList()) }
    var requestedBy by remember { mutableStateOf<List<String>>(emptyList()) }
    var favoritePost by remember { mutableStateOf<List<String>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()

    val techniques = listOf(
        Technique(
            name = "ダブルリフト",
            requiredSkills = listOf("なし"),
            nextSteps = listOf("アンビシャス・カード", "ピンキーカウント", "フォールス・シャッフル"),
            sampleVideoUri = Uri.parse("sample_video_uri_for_doublelift"),
            userVideoUri = getSavedVideoUri(context, "ダブルリフト")
        ),
        Technique(
            name = "ティルト",
            requiredSkills = listOf("ダブルリフト"),
            nextSteps = listOf("リバース・カード", "ミスディレクション"),
            sampleVideoUri = Uri.parse("sample_video_uri_for_tilt"),
            userVideoUri = getSavedVideoUri(context, "ティルト")
        )
        // 他の技を追加
    )
    val learnedTechniques = techniques.filter { technique ->
        technique.userVideoUri != null && technique.userVideoUri.toString().isNotEmpty()
    }

    // 保存された動画がある技術をカウント
    magic = learnedTechniques.size
    LaunchedEffect(Unit) {
        if (documentId2 != null) {
            val userRef = db.collection("users").document(documentId2)
            if (userRef != null) {
                userRef.get().addOnSuccessListener { document ->
                    if (document != null) {
                        friendId = document.get("friend") as? List<String> ?: emptyList()
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        documentId = PreferenceManager.getDocumentPath(context)
        name = PreferenceManager.getName(context) // 保存された名前を取得
        userId = PreferenceManager.getDocumentPath(context).toString()
        documentId?.let {
            getUserDataFromFirestore(it) { fetchedName, fetchedImageUrl, fetchedFriendList, fetchedRequesting, fetchedRequestedBy, fetchedFavoritePost ->
                name = fetchedName ?: name // Firestoreから取得した名前がある場合は上書き
                imageUrl = fetchedImageUrl
                friendList = fetchedFriendList ?: emptyList()
                requesting = fetchedRequesting ?: emptyList()
                requestedBy = fetchedRequestedBy ?: emptyList()
                favoritePost = fetchedFavoritePost ?: emptyList() // 新しいフィールドの初期化
            }
        }
    }
    Log.d("ProfileActivity", "Learned Techniques: $learnedTechniques, Magic Count: $magic")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                actions = {
                    IconButton(onClick = { navController.navigate("Notification") }) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(4.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(124.dp)
                            .height(100.dp)
                            .padding(16.dp)
                    ) {
                        if (imageUrl != null) {
                            Image(
                                painter = rememberImagePainter(imageUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(124.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(124.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            IconButton(
                                onClick = { navController.navigate("Qrcode/$userId") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.QrCode2,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { navController.navigate("scanQr") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Text(
                            text = "id: $userId",
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { navController.navigate("Edit") }
                ) {
                    Text(
                        text = "フレンド",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${friendId.size} 人",
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Text(
                        text = name,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                    )
                    Button(
                        onClick = { navController.navigate("Edit") },
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit, contentDescription = null,
                        )
                        Text(
                            text = "編集",
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Spacer(modifier = Modifier.height(16.dp))

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    if (learnedTechniques.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "習得したマジック  ${learnedTechniques.size} 種類",
                        fontSize = 20.sp
                    )
                        LazyRow {
                            items(learnedTechniques) { technique ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clip(shape = RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.elevatedCardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = technique.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                }else{
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "習得したマジック 0 種類",
                            fontSize = 20.sp
                        )
                    }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "あなたの動画",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
            if (learnedTechniques.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(learnedTechniques) { technique ->
                            technique.userVideoUri?.let { uri ->
                                Column {
                                    Text(
                                        text = technique.name,
                                        textAlign = TextAlign.Start
                                    )
                                    VideoPlayerSaved(uri = uri)
                                }
                            } ?: run {
                                // 動画がない場合は何も表示しない
                                // もし「動画がありません」と表示したい場合は、ここでそのテキストを配置できます。
                                Text(
                                    text = "動画がありません",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
            }else{
                Text(
                    text = "保存された動画がありません",
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 60.dp))
        }
    }
}


@Composable
fun VideoPlayerSaved(uri: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var showController by remember { mutableStateOf(false) } // コントローラーの表示状態を管理

    LaunchedEffect(uri) {
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = false
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clickable {
                showController = !showController
            }
            .height(202.5.dp)
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    controllerShowTimeoutMs = if (showController) Int.MAX_VALUE else 0
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .height(202.5.dp)
        )
    }
}
fun getSavedVideos(context: Context): Map<String, Uri> {
    val sharedPreferences = context.getSharedPreferences("Profile", Context.MODE_PRIVATE)
    val savedVideos = mutableMapOf<String, Uri>()

    sharedPreferences.all.forEach { (techniqueName, uriString) ->
        (uriString as? String)?.let {
            val uri = Uri.parse(it)
            savedVideos[techniqueName] = uri
        }
    }

    return savedVideos
}


fun uploadImageToFirebaseStorage(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("images/${uri.lastPathSegment}")
    val uploadTask = imageRef.putFile(uri)

    uploadTask.addOnSuccessListener {
        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            onSuccess(downloadUri.toString())
        }
    }.addOnFailureListener { exception ->
        onFailure(exception)
    }
}

fun deleteImageFromFirebaseStorage(imageUrl: String, onComplete: (Boolean) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
    storageRef.delete()
        .addOnSuccessListener {
            onComplete(true)
        }
        .addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Error deleting image: ", exception)
            onComplete(false)
        }
}

fun saveOrUpdateNameAndImageInFirestore(
    context: Context,
    name: String,
    imageUrl: String,
    friendList: List<String>, // Listに変更
    requesting: List<String>, // Listに変更
    requestedBy: List<String>, // Listに変更
    favoritePost: List<String>, // 新しいフィールド
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val documentPath = PreferenceManager.getDocumentPath(context) ?: UUID.randomUUID().toString()

    val user = hashMapOf(
        "name" to name,
        "image" to imageUrl,
        "friend" to friendList,
        "requesting" to requesting,
        "requestedBy" to requestedBy,
        "favoritePost" to favoritePost // 新しいフィールド
    )

    db.collection("users").document(documentPath)
        .set(user)
        .addOnSuccessListener {
            PreferenceManager.saveDocumentPath(context, documentPath)
            onComplete()
        }
        .addOnFailureListener { e ->
            // エラー処理
            onComplete()
        }
}

fun getUserDataFromFirestore(
    documentId: String,
    onComplete: (String?, String?, List<String>?, List<String>?, List<String>?, List<String>?) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users").document(documentId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val name = document.getString("name")
                val imageUrl = document.getString("image") // imageUriからimageUrlへ変更
                val friendList = document.get("friend") as? List<String>
                val requesting = document.get("requesting") as? List<String>
                val requestedBy = document.get("requestedBy") as? List<String>
                val favoritePost = document.get("favoritePost") as? List<String>

                onComplete(name, imageUrl, friendList, requesting, requestedBy, favoritePost)
            } else {
                onComplete(null, null, null, null, null, null)
            }
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Error getting documents: ", exception)
            onComplete(null, null, null, null, null, null)
        }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileActivity(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val documentId = PreferenceManager.getDocumentPath(context)
    var friendId by remember { mutableStateOf<List<String>>(emptyList()) }
    var friendName by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // 名前をMapで保持
    var friendList by remember { mutableStateOf<List<String>>(emptyList()) }
    var requesting by remember { mutableStateOf<List<String>>(emptyList()) }
    var requestedBy by remember { mutableStateOf<List<String>>(emptyList()) }
    var favoritePost by remember { mutableStateOf<List<String>>(emptyList()) } // 新しいフィールド
    var isLoading by remember { mutableStateOf(false) }
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    LaunchedEffect(Unit) {
        if (documentId != null) {
            val userRef = db.collection("users").document(documentId)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    friendId = document.get("friend") as? List<String> ?: emptyList()

                    // フレンドIDに基づいてフレンドの名前を取得
                    val friendsCollection = db.collection("users")
                    friendId.forEach { id ->
                        friendsCollection.document(id).get().addOnSuccessListener { friendDoc ->
                            val name = friendDoc.getString("name") ?: ""
                            friendName = friendName + (id to name) // Mapに追加
                        }
                    }
                }
            }
            getUserDataFromFirestore(documentId) { fetchedName, fetchedImageUrl, fetchedFriendList, fetchedRequesting, fetchedRequestedBy, fetchedFavoritePost ->
                name = fetchedName ?: ""
                imageUrl = fetchedImageUrl
                friendList = fetchedFriendList ?: emptyList()
                requesting = fetchedRequesting ?: emptyList()
                requestedBy = fetchedRequestedBy ?: emptyList()
                favoritePost = fetchedFavoritePost ?: emptyList()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Edit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 50.dp, y = -50.dp)
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
                else {
                    if (imageUrl?.isNotEmpty() == true) {
                        Image(
                            painter = rememberImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    } else {
                        Box(modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
                IconButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = CircleShape
                        ),
                    onClick = { imagePickerLauncher.launch("image/*") },
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null,)
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            text = "名前",
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 24.sp),
                )
                Spacer(modifier = Modifier.width(8.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(onClick = {
                        // ローディング状態をtrueに設定
                        isLoading = true

                        // 名前と画像の保存
                        val saveData: (String) -> Unit = { uploadedImageUrl ->
                            saveOrUpdateNameAndImageInFirestore(
                                context,
                                name,
                                uploadedImageUrl,
                                friendList,
                                requesting,
                                requestedBy,
                                favoritePost
                            ) {
                                imageUrl = uploadedImageUrl
                                isLoading = false  // ローディング状態をfalseに設定
                                PreferenceManager.saveName(context, name) // 名前を端末に保存
                                navController.navigateUp()
                            }
                        }

                        if (imageUri != null) {
                            uploadImageToFirebaseStorage(
                                uri = imageUri!!,
                                onSuccess = { uploadedImageUrl ->
                                    if (imageUrl?.isNotEmpty() == true) {
                                        deleteImageFromFirebaseStorage(imageUrl!!) {
                                            saveData(uploadedImageUrl)
                                        }
                                    } else {
                                        saveData(uploadedImageUrl)
                                    }
                                },
                                onFailure = { exception ->
                                    // エラー処理
                                    isLoading = false  // エラー時にもローディング状態をリセット
                                    Log.e("EditProfile", "Image upload failed: ${exception.message}")
                                }
                            )
                        } else {
                            imageUrl?.let { saveData(it) } ?: run {
                                // 画像が選択されていない場合もローディング状態をリセット
                                isLoading = false
                                Log.e("EditProfile", "No image URI or URL provided")
                            }
                        }
                    }) {
                        Text("保存")
                    }
                }
            }
            LazyColumn {
                item {
                    Column {
                        friendName.forEach { (id, name) ->
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                text = "名前: $name",
                                    )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "ID: $id",
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ProfileScreen(mainNavController: NavController) {
    val profileNavController = rememberNavController()
    NavHost(
        navController = profileNavController,
        startDestination = "profile"
    ) {
        composable("profile") { ProfileActivity(navController = profileNavController) }
        composable("Edit") { backStackEntry ->
            val currentName = backStackEntry.arguments?.getString("currentName") ?: ""
            EditProfileActivity(navController = profileNavController)
        }
        composable("Qrcode/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            QRCodeScreen(navController = profileNavController, userId = userId)
        }
        composable("scanQr") { QrScanMainScreen(navController = profileNavController) }
        composable("friendAdd/{friendId}") { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            FriendAddScreen(navController = profileNavController, friendId = friendId)
        }
        composable("Notification"){ NotificationScreen(profileNavController)}
    }
}



@Preview
@Composable
fun ProfilePreview(){
    AppTheme{
        val navController = rememberNavController()
        ProfileActivity(navController)
    }
}
@Preview
@Composable
fun EditProfileActivityPreview(){
    AppTheme{
        val navController = rememberNavController()
        EditProfileActivity(navController)
    }
}

