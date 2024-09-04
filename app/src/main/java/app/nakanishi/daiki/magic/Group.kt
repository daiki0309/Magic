package app.nakanishi.daiki.magic

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun GroupTabContent(navController: NavController, currentUserId: String) {
    var groups by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var selectedGroup by remember { mutableStateOf<Map<String, Any>?>(null) }
    var userGroups by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Load groups from Firestore
    LaunchedEffect(Unit) {
        loadGroups { loadedGroups ->
            groups = loadedGroups
            userGroups = loadedGroups.filter { group ->
                val members = group["members"] as? List<*>
                members?.contains(currentUserId) == true
            }
            selectedGroup = userGroups.firstOrNull() // Automatically select the first group the user is part of
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        if (selectedGroup == null) {
            // User is not part of any group, show group creation and request screen
            GroupCreationAndRequestScreen(
                groups = groups,
                currentUserId = currentUserId,
                onGroupCreated = { newGroup ->
                    groups = groups + newGroup
                    selectedGroup = newGroup
                },
                onRequestSent = { updatedGroups ->
                    groups = updatedGroups
                }
            )
        } else {
            // User is part of a group, show the group chat screen
            GroupChatView(navController = navController, groupId = selectedGroup!!["id"] as String)
        }
    }
}

@Composable
fun GroupCreationAndRequestScreen(
    groups: List<Map<String, Any>>,
    currentUserId: String,
    onGroupCreated: (Map<String, Any>) -> Unit,
    onRequestSent: (List<Map<String, Any>>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Group creation form
        Text("グループ作成")
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("グループ名") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = groupDescription,
            onValueChange = { groupDescription = it },
            label = { Text("説明") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                createGroup(groupName, groupDescription, currentUserId) { newGroup ->
                    onGroupCreated(newGroup)
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text("作成")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Group request form
        Text("Join a Group")
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Adjust the weight to fill the available space
        ) {
            items(groups) { group ->
                val groupId = group["id"] as String
                val groupName = group["name"] as String
                val groupDescription = group["description"] as String
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(groupName)
                        Text(groupDescription)
                        Button(
                            onClick = {
                                requestToJoinGroup(groupId, currentUserId) {
                                    onRequestSent(groups)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        ) {
                            Text("参加リクエスト")
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatView(navController: NavController, groupId: String) {
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var groupName by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedVideoUri = uri
    }
    val db = FirebaseFirestore.getInstance()
    fun sendMessage(groupId: String, content: String, videoUrl: String?, onMessageSent: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userName = PreferenceManager.getName(context)
        val userId = PreferenceManager.getDocumentPath(context) // ユーザーIDを取得

        val newMessage = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "content" to content,
            "timestamp" to System.currentTimeMillis(),
            "videoUrl" to videoUrl
        )
        db.collection("groupChats").document(groupId).collection("messages").add(newMessage)
            .addOnSuccessListener {
                onMessageSent()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to send message", it)
            }
    }
    // メッセージと動画の送信処理
    fun sendMessageWithVideo(groupId: String, content: String, videoUri: Uri?, onMessageSent: () -> Unit) {
        if (videoUri != null) {
            // 動画をFirebase Storageにアップロード
            uploadVideoToFirebase(videoUri, groupId) { downloadUrl ->
                // 動画のアップロードが成功したらメッセージを送信
                sendMessage(groupId, content, downloadUrl, onMessageSent)
            }
        } else {
            // 動画がない場合はそのままメッセージを送信
            sendMessage(groupId, content, null, onMessageSent)
        }
    }

    LaunchedEffect(groupId) {
        loadGroupMessages(groupId) { loadedMessages ->
            messages = loadedMessages
        }
    }
    LaunchedEffect(Unit){
        if (groupId != null) {
            val userRef = db.collection("groups").document(groupId)
            if (userRef != null) {
                userRef.get().addOnSuccessListener { document ->
                    if (document != null) {
                         groupName = document.get("name").toString()
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = groupName) },
                actions = {
                    IconButton(onClick = { navController.navigate("groupSetting/$groupId") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                modifier = Modifier.height(40.dp)
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(message)
                }
            }

            selectedVideoUri?.let { uri ->
                VideoPreview(uri)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("メッセージ") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { launcher.launch("video/*") }) {
                    Icon(Icons.Default.Photo, contentDescription = "Select Video")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    // メッセージと動画を同時に送信
                    sendMessageWithVideo(groupId, message, selectedVideoUri) {
                        loadGroupMessages(groupId) { loadedMessages ->
                            messages = loadedMessages
                        }
                    }
                    message = ""
                    selectedVideoUri = null
                }) {
                    Text("送信")
                }
            }
            Spacer(modifier = Modifier.padding(32.dp))
        }
    }
}
@Composable
fun ChatMessageItem(message: Map<String, Any>) {
    val userId = message["userId"] as String
    val userName = message["userName"] as String
    val content = message["content"] as String
    val videoUrl = message["videoUrl"] as? String

    val context = LocalContext.current
    var userIconUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val iconUrl = document.getString("image")
                    userIconUrl = iconUrl
                } else {
                    Log.e("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting document", exception)
            }
    }
    Column(){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // ユーザーのアイコン画像を表示
            if (userIconUrl != null) {
                AsyncImage(
                    model = userIconUrl,
                    contentDescription = "User Icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default User Icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = userName,
                )
        }
        Row(){
            Spacer(modifier = Modifier.padding(horizontal = 24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                if (content.isNotEmpty()) {
                    Text(content)
                }
                videoUrl?.let {
                    VideoPlayer2(it)
                }
            }
        }
    }
}

@Composable
fun VideoPreview(uri: Uri) {
    val context = LocalContext.current
    val player = remember { SimpleExoPlayer.Builder(context).build() }
    var showController by remember { mutableStateOf(false) } // コントローラーの表示状態を管理

    DisposableEffect(uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        onDispose {
            player.release()
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
    ){
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
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
fun getCurrentUserId(context: Context): String? {
    return PreferenceManager.getDocumentPath(context)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSetting(navController: NavController, groupId: String) {
    val context = LocalContext.current
    var groupRequests by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var members by remember { mutableStateOf<List<String>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()
    var userImages by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var imageUrls by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
    var groupName by remember { mutableStateOf("") }

    // グループのリクエストとメンバーを取得
    LaunchedEffect(groupId) {
        fetchGroupRequests(listOf(groupId)) { requests ->
            groupRequests = requests
        }
        fetchGroupMembers(groupId) { memberList ->
            members = memberList

            // すべてのリクエストが完了したら userImages を更新
        }
    }
    LaunchedEffect(groupId) {
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    members = document.get("members") as List<String>
                    groupName = document.get("name").toString()
                } else {
                    Log.d("GroupSetting", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("GroupSetting", "Get failed with ", exception)
            }
    }

    // 各メンバーのプロフィール画像URLを取得
    LaunchedEffect(members) {
        members.forEach { memberId ->
            db.collection("users").document(memberId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        imageUrls = imageUrls + (memberId to document.getString("image"))
                    } else {
                        Log.d("UserProfileImage", "No such document for user $memberId")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("UserProfileImage", "Get failed for user $memberId", exception)
                }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setting") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("groupChat/$groupId") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = groupName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            if (groupRequests.isNotEmpty()) {
                Text("参加リクエスト", style = MaterialTheme.typography.headlineMedium)
                groupRequests.forEach { (groupId, requestedBy) ->
                    Text("Group ID: $groupId", style = MaterialTheme.typography.bodyMedium)
                    requestedBy.forEach { requestUserId ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val imageUrl = userImages[requestUserId]
                            val imagePainter = rememberImagePainter(
                                data = imageUrl,
                                builder = {
                                    crossfade(true)
                                }
                            )
                            Image(
                                painter = imagePainter,
                                contentDescription = "User Icon",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Text(
                                text = requestUserId,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(onClick = {
                                acceptRequest(groupId, requestUserId, context) {
                                    groupRequests = groupRequests.toMutableMap().apply {
                                        val updatedList = this[groupId]?.minus(requestUserId)
                                        if (updatedList.isNullOrEmpty()) {
                                            remove(groupId)
                                        } else {
                                            put(groupId, updatedList)
                                        }
                                    }
                                }
                            }) {
                                Text("承認")
                            }
                        }
                    }
                }
            } else {
                Text("リクエストなし", modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("メンバー", style = MaterialTheme.typography.headlineMedium)
            LazyColumn {
                items(members) { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imagePainter = rememberImagePainter(
                            data = imageUrls[member],
                            builder = {
                                crossfade(true)
                            }
                        )
                        Image(
                            painter = imagePainter,
                            contentDescription = "User Icon",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = member,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

fun fetchGroupMembers(groupId: String, onMembersFetched: (List<String>) -> Unit) {
    // Implement the logic to fetch group members from Firestore
    val db = FirebaseFirestore.getInstance()
    db.collection("groups").document(groupId).get().addOnSuccessListener { document ->
        val members = document.get("members") as? List<String> ?: emptyList()
        onMembersFetched(members)
    }
}

// グループIDごとにリクエストを取得する関数
fun fetchGroupRequests(groupIds: List<String>, callback: (Map<String, List<String>>) -> Unit) {
    val db = Firebase.firestore
    val requestsMap = mutableMapOf<String, List<String>>()

    // 非同期で複数のグループIDに対してリクエストを取得
    val tasks = groupIds.map { groupId ->
        db.collection("groups").document(groupId).get().addOnSuccessListener { document ->
            val requestedList = document.get("requested") as? List<String> ?: emptyList()
            requestsMap[groupId] = requestedList
        }
    }

    // すべてのタスクが完了するのを待つ
    Tasks.whenAllSuccess<DocumentSnapshot>(*tasks.toTypedArray()).addOnCompleteListener {
        callback(requestsMap)
    }
}
fun getUserGroups(userId: String, callback: (List<String>) -> Unit) {
    val db = Firebase.firestore

    // groupsコレクションをクエリ
    db.collection("groups")
        .whereArrayContains("members", userId)
        .get()
        .addOnSuccessListener { result ->
            val groupIds = result.documents.map { it.id }
            callback(groupIds)
        }
        .addOnFailureListener { exception ->
            Log.w("Firestore", "Error getting documents.", exception)
            callback(emptyList())
        }
}
fun acceptRequest(groupId: String, targetUserId: String, context: Context, onRequestAccepted: () -> Unit) {
    val db = Firebase.firestore
    val groupRef = db.collection("groups").document(groupId)

    db.runTransaction { transaction ->
        // グループの現在のデータを取得
        val groupSnapshot = transaction.get(groupRef)
        val requestedList = groupSnapshot.get("requested") as? List<String> ?: emptyList()
        val membersList = groupSnapshot.get("members") as? List<String> ?: emptyList()

        // リクエストを承認し、リストを更新
        if (requestedList.contains(targetUserId)) {
            val updatedRequestedList = requestedList - targetUserId
            val updatedMembersList = membersList + targetUserId
            transaction.update(groupRef, "requested", updatedRequestedList)
            transaction.update(groupRef, "members", updatedMembersList)
        }
    }.addOnSuccessListener {
        Log.d("acceptRequest", "ユーザーをメンバーリストに追加しました")
        Toast.makeText(context, "ユーザーをメンバーリストに追加しました", Toast.LENGTH_SHORT).show()
        onRequestAccepted()
    }.addOnFailureListener { exception ->
        Log.e("acceptRequest", "ユーザーの追加に失敗しました", exception)
        Toast.makeText(context, "ユーザーの追加に失敗しました", Toast.LENGTH_SHORT).show()
    }
}
@Composable
fun VideoPlayer2(videoUrl: String) {
    var isFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // ExoPlayerの初期化
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 通常表示
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (isLandscape) 16 / 9f else 9 / 16f)
            .clickable { isFullScreen = true }
            .height(if (isLandscape) 202.5.dp else 360.dp)
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            },
            modifier = Modifier
                .fillMaxSize()
        )

        IconButton(
            onClick = { isFullScreen = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Fullscreen")
        }
    }

    // 全画面表示
    if (isFullScreen) {
        FullScreenDialog(
            videoUrl = videoUrl,
            onDismissRequest = { isFullScreen = false },
            exoPlayer = exoPlayer,
            isLandscape = isLandscape
        )
    }
}

@Composable
fun FullScreenDialog(
    videoUrl: String,
    onDismissRequest: () -> Unit,
    exoPlayer: ExoPlayer,
    isLandscape: Boolean
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismissRequest() }
        ) {
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                update = { playerView ->
                    playerView.player = exoPlayer
                },
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(if (isLandscape) 16 / 9f else 9 / 16f)
            )

            IconButton(
                onClick = { onDismissRequest() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Fullscreen")
            }
        }
    }
}
// コンテキストからActivityを取得する拡張関数
fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}


fun createGroup(name: String, description: String, currentUserId: String, onGroupCreated: (Map<String, Any>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val newGroup = hashMapOf(
        "name" to name,
        "description" to description,
        "members" to listOf(currentUserId),
        "requested" to listOf<String>() // Initialize as an empty list
    )
    db.collection("groups").add(newGroup)
        .addOnSuccessListener { documentReference ->
            val createdGroup = newGroup.apply { put("id", documentReference.id) }
            onGroupCreated(createdGroup)
        }
        .addOnFailureListener {
            // Handle error
        }
}

fun requestToJoinGroup(groupId: String, userId: String, onRequestCompleted: (List<Map<String, Any>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val groupRef = db.collection("groups").document(groupId)

    db.runTransaction { transaction ->
        val groupSnapshot = transaction.get(groupRef)
        val requestedList = groupSnapshot.get("requested") as? List<String> ?: emptyList()

        // Add the user to the requested list if they aren't already on it
        if (!requestedList.contains(userId)) {
            val updatedRequestedList = requestedList + userId
            transaction.update(groupRef, "requested", updatedRequestedList)
        }
    }.addOnSuccessListener {
        // Retrieve updated groups after the request is sent
        loadGroups { updatedGroups ->
            onRequestCompleted(updatedGroups)
        }
    }.addOnFailureListener {
        // Handle failure
        Log.e("Firestore", "Failed to send join request", it)
    }
}
fun loadGroups(onGroupsLoaded: (List<Map<String, Any>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("groups").get()
        .addOnSuccessListener { result ->
            val loadedGroups = result.documents.mapNotNull { document ->
                document.data?.toMutableMap()?.apply {
                    this["id"] = document.id
                }
            }
            onGroupsLoaded(loadedGroups)
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Error loading groups", exception)
            onGroupsLoaded(emptyList()) // Return an empty list on failure
        }
}

fun loadGroupMessages(groupId: String, onMessagesLoaded: (List<Map<String, Any>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("groupChats").document(groupId).collection("messages")
        .orderBy("timestamp")
        .get()
        .addOnSuccessListener { result ->
            val loadedMessages = result.documents.mapNotNull { document ->
                document.data?.toMutableMap()?.apply {
                    this["id"] = document.id
                }
            }
            onMessagesLoaded(loadedMessages)
        }
        .addOnFailureListener {
            // Handle error
        }
}

// Function to upload video to Firebase Storage
fun uploadVideoToFirebase(videoUri: Uri, groupId: String, onUploadComplete: (String?) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val videoRef = storageRef.child("groupChats/$groupId/videos/${UUID.randomUUID()}.mp4")

    videoRef.putFile(videoUri)
        .addOnSuccessListener {
            videoRef.downloadUrl.addOnSuccessListener { uri ->
                onUploadComplete(uri.toString())
            }.addOnFailureListener {
                Log.e("Firebase", "Failed to get download URL", it)
                onUploadComplete(null)
            }
        }
        .addOnFailureListener {
            Log.e("Firebase", "Failed to upload video", it)
            onUploadComplete(null)
        }
}