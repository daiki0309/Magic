package app.nakanishi.daiki.magic

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import app.nakanishi.daiki.magic.ui.theme.AppTheme
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class Post(
    var id: String = "",
    var content: String = "",
    var videoUrl: String? = null,
    var favoriteCount: Int = 0
)
@Composable
fun UploadVideoScreen(onVideoUploaded: (Uri?) -> Unit, videoUri: Uri?, setLoading: (Boolean) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            onVideoUploaded(it)
        }
    }
    var isLoading by remember { mutableStateOf(false) }
    var showController by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("video/*") }) {
            Text("動画を選択")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (videoUri != null) {
            val exoPlayer = remember { ExoPlayer.Builder(context).build() }

            LaunchedEffect(videoUri) {
                exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
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

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

fun loadImageFromFirebase(uri: String, imageView: ImageView) {
    Glide.with(imageView.context)
        .load(uri)
        .apply(RequestOptions().placeholder(R.drawable.baseline_image_24)) // プレースホルダー画像
        .into(imageView)
}
fun displayPost(userIconUri: String, imageView: ImageView) {
    // Firebase Storageからユーザーアイコンを取得して表示する
    loadImageFromFirebase(userIconUri, imageView)
}

fun uploadVideoToFirebase(context: Context, uri: Uri, onComplete: (String) -> Unit, setLoading: (Boolean) -> Unit) {
    val storage = Firebase.storage
    val storageRef = storage.reference
    val videoRef = storageRef.child("videos/${UUID.randomUUID()}.mp4")
    val uploadTask = videoRef.putFile(uri)

    setLoading(true)

    uploadTask.addOnSuccessListener {
        videoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            setLoading(false)
            onComplete(downloadUrl.toString())
        }
    }.addOnFailureListener {
        setLoading(false)
        Toast.makeText(context, "動画のアップロードに失敗しました", Toast.LENGTH_SHORT).show()
    }
}



fun fetchVideoUrls(onVideosFetched: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("videos").orderBy("timestamp", Query.Direction.DESCENDING).get()
        .addOnSuccessListener { result ->
            val videoUrls = result.documents.mapNotNull { document ->
                document.getString("videoUrl")
            }
            onVideosFetched(videoUrls)
        }
}
@Composable
fun PostItem(
    postId: String,
    content: String,
    videoUrl: String?,
    initialFavoriteCount: Int,
    userName: String,
    userId: String,
    modifier: Modifier = Modifier
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var favorite by rememberSaveable { mutableStateOf(false) }
    var favoriteCount by remember { mutableStateOf(initialFavoriteCount) }
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var userImageUrl by rememberSaveable(postId) { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var showController by remember { mutableStateOf(false) } // コントローラーの表示状態を管理
    val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()
    FirebaseFirestore.getInstance().firestoreSettings = settings
    var showReplyForm by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf(TextFieldValue("")) }
    var replies by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showReply by remember{ mutableStateOf(false) }
    var replyCount by remember{ mutableStateOf("") }
    fun updateFavoriteCount(postId: String, increment: Boolean) {
        val postRef = db.collection("posts").document(postId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentFavoriteCount = snapshot.getLong("favorite") ?: 0
            val newFavoriteCount = if (increment) {
                currentFavoriteCount + 1
            } else {
                currentFavoriteCount - 1
            }
            transaction.update(postRef, "favorite", newFavoriteCount)
        }
    }

    fun updateUserFavoritePost(postId: String, add: Boolean) {
        val documentPath = PreferenceManager.getDocumentPath(context)
        if (documentPath != null) {
            val userRef = db.collection("users").document(documentPath)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentFavoritePosts = snapshot.get("favoritePost") as? List<String> ?: emptyList()
                val updatedFavoritePosts = if (add) {
                    currentFavoritePosts + postId
                } else {
                    currentFavoritePosts - postId
                }
                transaction.update(userRef, "favoritePost", updatedFavoritePosts)
            }
        }
    }
    // ユーザーのimage URLを取得する
    fun fetchUserImage(userId: String) {
        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val imagePath = document.getString("image")
                // FirebaseStorage 参照の作成
                if (imagePath != null) {
                    userImageUrl = imagePath
                }
            }
        }
    }
    fun fetchReplies() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId).collection("replies")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val loadedReplies = result.documents.mapNotNull { document ->
                    document.data?.toMutableMap()?.apply {
                        this["id"] = document.id
                    }
                }
                replies = loadedReplies
            }
            .addOnFailureListener { exception ->
                Log.e("fetchReplies", "Error getting replies", exception)
            }
    }

    LaunchedEffect(postId) {
        fetchReplies()
    }
    fun checkIfFavorite(postId: String, callback: (Boolean) -> Unit) {
        val documentPath = PreferenceManager.getDocumentPath(context)
        if (documentPath != null) {
            val userRef = db.collection("users").document(documentPath)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val favoritePosts = document.get("favoritePost") as? List<String> ?: emptyList()
                    callback(favoritePosts.contains(postId))
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        fetchUserImage(userId)
        checkIfFavorite(postId) { isFavorite ->
            favorite = isFavorite
        }
    }
    // Firestore から URL を取得
    LaunchedEffect(userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    imageUrl = document.getString("image")
                } else {
                    Log.d("UserProfileImage", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("UserProfileImage", "Get failed with ", exception)
            }
    }
    LaunchedEffect(postId) {
        val repliesRef = db.collection("posts").document(postId).collection("replies")
        repliesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                replyCount = snapshot.size().toString()
            }
        }
    }
    LaunchedEffect(userId) {
        if (userImageUrl == null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    userImageUrl = document.getString("image")
                }
                .addOnFailureListener { exception ->
                    Log.e("PostItem", "Error getting user image", exception)
                }
        }
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(4.dp))  // 角を丸くする
            .animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(4.dp)  // 角を丸くする
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imagePainter = rememberImagePainter(
                    data = userImageUrl,
                    builder = {
                        crossfade(true) // クロスフェード効果
                        placeholder(R.drawable.baseline_account_circle_24) // プレースホルダー画像
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
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "id: $userId",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Column {
                var textHeight by remember { mutableStateOf(0) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textHeight = coordinates.size.height
                        }
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = content,
                        fontSize = 16.sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        modifier = Modifier.animateContentSize()
                    )
                }

                if (textHeight > 3 * LocalDensity.current.run { 16.sp.toPx() }) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (expanded) "閉じる" else "もっと見る")
                    }
                }
            }

            if (videoUrl != null) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .height(202.5.dp)
                            .clickable {
                                showController = !showController
                            }
                    ) {
                        AndroidView(
                            factory = { context ->
                                VideoView(context).apply {
                                    setVideoURI(Uri.parse(videoUrl))
                                    val mediaController = MediaController(context).apply {
                                        setAnchorView(this@apply) // VideoViewをアンカービューとして設定
                                    }
                                    setMediaController(mediaController)
                                    requestFocus()

                                    // 再生を停止して初期表示を静止状態にする
                                    setOnPreparedListener {
                                        pause() // 再生を停止して静止状態で表示
                                        mediaController.show(if (showController) 0 else 1)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f)
                                .height(202.5.dp)
                                .zIndex(1f) // これでコントローラーの表示順序を他のUIより上にする
                        )
                    }
                }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    showReplyForm = !showReplyForm
                    showReply = !showReply
                }) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = replyCount,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(
                    onClick = {
                        favorite = !favorite
                        favoriteCount = if (favorite) favoriteCount + 1 else favoriteCount - 1
                        coroutineScope.launch {
                            updateFavoriteCount(postId, favorite)
                            updateUserFavoritePost(postId, favorite)
                        }
                    }
                ) {
                    if (favorite) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "favorite",
                            tint = Color(0xFFEC407A)
                        )
                    } else {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$favoriteCount",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        if (showReplyForm) {
            Column(modifier = Modifier.padding(16.dp)) {
            if (replies.isNotEmpty() ) {
                    replies.forEach { reply ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = reply["userName"] as String,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp),
                                fontSize = 16.sp
                            )
                            Text(
                                text = reply["userId"] as String,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text(text = reply["replyText"] as String)
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("返信") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            showReplyForm = false
                        }
                    ) {
                        Text("キャンセル")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            // Firestoreに返信を保存
                            addReply(context,postId, replyText.text)
                            replyText = TextFieldValue("")
                            showReplyForm = false
                        }
                    ) {
                        Text("送信")
                    }
                }
            }
        }
        }
    }
fun addReply(context: Context, postId: String, replyText: String) {
    val db = FirebaseFirestore.getInstance()
    val userName = PreferenceManager.getName(context)
    val userId = PreferenceManager.getDocumentPath(context)
    val reply = hashMapOf(
        "replyText" to replyText,
        "userName" to userName,
        "userId" to userId,
    )
    db.collection("posts").document(postId).collection("replies").add(reply)
        .addOnSuccessListener {
            // 返信の成功時の処理
        }
        .addOnFailureListener {
            // エラー処理
        }
}
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var skipPartiallyExpanded by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    var openBottomSheet by remember { mutableStateOf(false) }
    var newPostContent by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current
    var allPosts by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var friendPosts by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var showBottomNav by remember { mutableStateOf(true) }
    var showFAB by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var isVideoLoading by remember { mutableStateOf(false) }
    val names = listOf("All", "Friend", "Group")
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val checkedState = remember { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val userId = PreferenceManager.getDocumentPath(context)
    val db = FirebaseFirestore.getInstance()

    fun getAllPosts(onPostsLoaded: (List<Map<String, Any>>) -> Unit) {
        db.collection("posts")
            .whereEqualTo("open", true)
            .get()
            .addOnSuccessListener { result ->
                val loadedPosts = result.documents.mapNotNull { document ->
                    document.data?.toMutableMap()?.apply {
                        this["id"] = document.id
                    }
                }
                onPostsLoaded(loadedPosts)
            }
            .addOnFailureListener { exception ->
                Log.e("getAllPosts", "Error getting posts", exception)
            }
    }

    fun getFriendPosts(onPostsLoaded: (List<Map<String, Any>>) -> Unit) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val friends = document.get("friend") as? List<String> ?: emptyList()
                    if (friends.isNotEmpty()) {
                        db.collection("posts")
                            .whereIn("userId", friends)
                            .get()
                            .addOnSuccessListener { result ->
                                val loadedPosts = result.documents.mapNotNull { document ->
                                    document.data?.toMutableMap()?.apply {
                                        this["id"] = document.id
                                    }
                                }
                                onPostsLoaded(loadedPosts)
                            }
                            .addOnFailureListener {
                                // エラー処理
                            }
                    } else {
                        onPostsLoaded(emptyList())
                    }
                }
                .addOnFailureListener {
                    // エラー処理
                }
        }
    }

    fun addPost(context: Context, content: String, videoUrl: String?, open: Boolean) {
        val userName = PreferenceManager.getName(context)
        val userId = PreferenceManager.getDocumentPath(context)
        val newPost = hashMapOf(
            "content" to content,
            "videoUrl" to videoUrl,
            "favorite" to 0,
            "userName" to userName,
            "userId" to userId,
            "open" to open,
        )
        db.collection("posts").add(newPost)
            .addOnSuccessListener {
                // Success handling
                getAllPosts { loadedPosts ->
                    allPosts = loadedPosts
                }
                getFriendPosts { loadedPosts ->
                    friendPosts = loadedPosts
                }
            }
            .addOnFailureListener {
                // Error handling
            }
    }

    val submitPost = {
        if (newPostContent.text.isNotBlank()) {
            if (videoUri != null) {
                uploadVideoToFirebase(context, videoUri!!, { url ->
                    addPost(context, newPostContent.text, url, !checkedState.value)
                    newPostContent = TextFieldValue("")
                    videoUri = null
                    openBottomSheet = false
                    Toast.makeText(context, "投稿が完了しました", Toast.LENGTH_SHORT).show()
                }, setLoading = { isLoading = it })
            } else {
                addPost(context, newPostContent.text, null, !checkedState.value)
                newPostContent = TextFieldValue("")
                openBottomSheet = false
                Toast.makeText(context, "投稿が完了しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        getAllPosts { loadedPosts ->
            allPosts = loadedPosts
        }
        getFriendPosts { loadedPosts ->
            friendPosts = loadedPosts
        }
    }

    fun refreshAllPosts() {
        isLoading = true
        getAllPosts { loadedPosts ->
            allPosts = loadedPosts
            isLoading = false
        }
    }

    fun refreshFriendPosts() {
        isLoading = true
        getFriendPosts { loadedPosts ->
            friendPosts = loadedPosts
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshAllPosts()
        refreshFriendPosts()
    }

    Scaffold(
        floatingActionButton = {
                FloatingActionButton(
                    onClick = { coroutineScope.launch { openBottomSheet = true } },
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(64.dp) ,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.PostAdd,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
        },
        topBar = {
            Column{
                TopAppBar(
                    title = { Text(text = "Chat") },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate("UserSearch") }
                        ) {
                            Icon(Icons.Default.PersonSearch, contentDescription = null)
                        }
                    },
                    scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                        rememberTopAppBarState())
                )
            }

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            // タブ
            TabRow(
                selectedTabIndex = pagerState.currentPage,
            ) {
                Tab(
                    text = { Text("All") },
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                Tab(
                    text = { Text("Friend") },
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
            }
            // スクロール可能な部分
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> SwipeRefreshExample(
                            posts = allPosts,
                            isLoading = isLoading,
                            onRefresh = { refreshAllPosts() }
                        )
                        1 -> SwipeRefreshExample(
                            posts = friendPosts,
                            isLoading = isLoading,
                            onRefresh = { refreshFriendPosts() }
                        )
                    }
                }
            // 投稿の下に追加
            if (openBottomSheet) {
                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    onDismissRequest = { openBottomSheet = false },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        TextField(
                            value = newPostContent,
                            onValueChange = { newPostContent = it },
                            label = { Text("内容を入力") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UploadVideoScreen(onVideoUploaded = { uri ->
                                                            videoUri = uri
                        }, videoUri = videoUri, setLoading = { isVideoLoading = it })
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            Modifier
                                .toggleable(
                                    value = checkedState.value,
                                    role = Role.Checkbox,
                                    onValueChange = { checkedState.value = it }
                                )
                        ) {
                            Checkbox(checked = checkedState.value, onCheckedChange = null)
                            Text("この投稿をフレンドのみに公開")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                submitPost()
                                openBottomSheet = false
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("投稿")
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SwipeRefreshExample(
    posts: List<Map<String, Any>>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = isLoading),
        onRefresh = { onRefresh() }
    ) {
        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { post ->
                    // Render each post
                    PostItem(
                        postId = post["id"] as String,
                        content = post["content"] as String,
                        videoUrl = post["videoUrl"] as? String,
                        initialFavoriteCount = (post["favorite"] as Long).toInt(),
                        userName = post["userName"] as String,
                        userId = post["userId"] as String
                    )
                }
                item{
                    Spacer(modifier = Modifier.padding(52.dp))
                }
            }
        }
    }
}
@Composable
@Preview(showBackground = true)
fun MyScreenPreview() {
    // プレビュー用のダミーデータ
    // UI表示
    ChatScreen(
       navController = rememberNavController()
    )
}