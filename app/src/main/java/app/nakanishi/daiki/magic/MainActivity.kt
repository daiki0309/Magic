package app.nakanishi.daiki.magic

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.nakanishi.daiki.magic.ui.theme.AppTheme
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MagicScreen()
                }
            }
        }
    }
}

// どこのページへ移動するかnavControllerに定義する

data class Technique(
    val name: String,
    val requiredSkills: List<String>,
    val nextSteps: List<String>,
    val sampleVideoUri: Uri?,
    val userVideoUri: Uri?
)
@Composable
fun TechniqueDetailScreen(navController: NavController, techniqueName: String) {
    val context = LocalContext.current
    var savedVideoUri by remember { mutableStateOf(getSavedVideoUri(context, techniqueName)) }
    var openDialog by remember { mutableStateOf(false) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var openDeleteDialog by remember { mutableStateOf(false) }

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

    val technique = techniques.find { it.name == techniqueName }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedVideoUri = it
            openDialog = true
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        technique?.let {
            Text(
                text = it.name,
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 36.sp,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "必要な技術",
                modifier = Modifier.padding(vertical = 12.dp),
                fontSize = 20.sp
            )

            it.requiredSkills.forEach { skill ->
                Card(
                    modifier = Modifier
                        .animateContentSize()
                        .height(80.dp)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                ) {
                    Row {
                        Icon(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(alignment = Alignment.CenterVertically),
                            imageVector = Icons.Rounded.Done,
                            contentDescription = "Done",
                        )
                        LazyRow(
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(it.requiredSkills){
                                ElevatedCard(
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    ),
                                    modifier = Modifier
                                        .padding(2.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(4.dp),
                                        text = skill,
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "${it.name}の次は...？",
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 20.sp,
            )

            Card(
                modifier = Modifier
                    .animateContentSize()
                    .height(80.dp)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row{
                    Icon(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(alignment = Alignment.CenterVertically),
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Star",
                    )
                    LazyRow(
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(it.nextSteps) { nextStep ->
                            ElevatedCard(
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                ),
                                modifier = Modifier
                                    .padding(2.dp)
                            ) {
                                Text(
                                    modifier = Modifier.padding(4.dp),
                                    text = nextStep,
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp,
                                )
                            }
                        }
                    }
                }
            }
            VideoSection(savedVideoUri = savedVideoUri, launcher = launcher)

            if (savedVideoUri != null) {
                IconButton(
                    onClick = { openDeleteDialog = true },
                    modifier = Modifier.padding(vertical = 16.dp)
                        .align(alignment = Alignment.End)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        } ?: run {
            Text("技術が見つかりませんでした。")
        }

        Spacer(modifier = Modifier.height(72.dp))
    }

    if (openDialog) {
        selectedVideoUri?.let {
            AlertDialogView(
                onDismissRequest = { openDialog = false },
                onConfirmation = {
                    openDialog = false
                    saveVideoFile(context, it, techniqueName)
                    savedVideoUri = getSavedVideoUri(context, techniqueName)
                },
                uri = it
            )
        }
    }

    if (openDeleteDialog) {
        AlertDialog(
            onDismissRequest = { openDeleteDialog = false },
            title = { Text("削除の確認") },
            text = { Text("この動画を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        savedVideoUri?.let { uri ->
                            deleteVideoFile(context, techniqueName)
                            savedVideoUri = null
                        }
                        openDeleteDialog = false
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDeleteDialog = false }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }
}

fun deleteVideoFile(context: Context, techniqueName: String) {
    val file = File(context.filesDir, "$techniqueName.mp4")
    if (file.exists()) {
        file.delete()
    }
}
fun saveVideoUriToPreferences(context: Context, techniqueName: String, uri: Uri) {
    val sharedPreferences = context.getSharedPreferences("Profile", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("${techniqueName}_video_uri", uri.toString())
        apply()
    }
}
fun saveVideoFile(context: Context, videoUri: Uri, techniqueName: String) {
    try {
        val inputStream = context.contentResolver.openInputStream(videoUri)
        val file = File(context.filesDir, "$techniqueName.mp4")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        // 動画のURIをSharedPreferencesに保存
        val savedUri = Uri.fromFile(file)
        saveVideoUriToPreferences(context, techniqueName, savedUri)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
fun getSavedVideoUriFromPreferences(context: Context, techniqueName: String): Uri? {
    val sharedPreferences = context.getSharedPreferences("Profile", Context.MODE_PRIVATE)
    val uriString = sharedPreferences.getString("${techniqueName}_video_uri", null)
    Log.d("ProfileActivity", "Retrieved URI for $techniqueName: $uriString")
    return uriString?.let { Uri.parse(it) }
}
fun getSavedVideoUri(context: Context, techniqueName: String): Uri? {
    val file = File(context.filesDir, "$techniqueName.mp4")
    return if (file.exists()) Uri.fromFile(file) else null
}


@Composable
fun VideoSection(savedVideoUri: Uri?, launcher: ManagedActivityResultLauncher<String, Uri?>) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = "お手本",
            modifier = Modifier.padding(vertical = 8.dp),
            fontWeight = FontWeight.Bold
        )

        Box(modifier = Modifier
            .height(140.dp)
            .fillMaxWidth()) {
            // お手本動画のビデオプレイヤーを配置
        }

        Text(text = "あなたの動画",
            modifier = Modifier.padding(vertical = 24.dp),
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .height(202.5.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (savedVideoUri == null) {
                    Text(
                        text = "動画がありません",
                        color = MaterialTheme.colorScheme.scrim
                        )
                } else {
                    VideoPlayer(uri = savedVideoUri)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch("video/*") }) {
                    Text("Select Video")
                }
            }
        }
    }
}
fun saveVideoToInternalStorage(context: Context, uri: Uri): Uri? {
    val contentResolver = context.contentResolver
    val videoFile = File(context.filesDir, "saved_video.mp4")

    return try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(videoFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Uri.fromFile(videoFile)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}



@Composable
fun VideoPlayer(uri: Uri) {
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
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(16 / 9f)
                .height(202.5.dp)
        )
    }
}
@Composable
fun AlertDialogView(onDismissRequest: () -> Unit, onConfirmation: () -> Unit,uri: Uri){
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val savedUri = saveVideoToInternalStorage(context, uri)
                    savedUri?.let {
                        saveVideoUriToPreferences(context, "ダブルリフト", Uri.parse("file://path_to_video"))
                    }
                    onConfirmation()
                }
            ){
                Text("追加")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("キャンセル")
            }
        },
        text = {
            VideoPlayer(uri = uri)
        }
    )
}




@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MagicScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var currentId = PreferenceManager.getDocumentPath(context)



    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavigationAppTheme(navController)
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = "mapWithButtons",
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(
                "mapWithButtons"
            ) {
                MapWithButtons(navController)
            }
            composable(
                "techniqueDetail/{techniqueName}",
                arguments = listOf(navArgument("techniqueName") { type = NavType.StringType })
            ) { backStackEntry ->
                val techniqueName = backStackEntry.arguments?.getString("techniqueName") ?: return@composable
                TechniqueDetailScreen(navController, techniqueName = techniqueName)
            }
            composable("screen3") { ProfileScreen(navController) }
            composable("screen4") { ChatScreen(navController) }

            composable("UserSearch") { UserSearchScreen(navController) }
            composable("Group") {
                if (currentId != null) {
                    GroupTabContent(navController,currentId)
                }
            }
            composable("groupChat/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                GroupChatView(navController = navController, groupId = groupId)
            }
            composable("groupSetting/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                GroupSetting(navController,groupId)
            }
        }
    }
}


@Composable
fun BottomNavigationAppTheme (navController: NavController){
    // navigationを追加
    var selectedTab by remember { mutableIntStateOf(0) }
    NavigationBar(
        modifier = Modifier
    ){
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = {
                selectedTab = 0
                navController.navigate("mapWithButtons")
            },
            icon = { Icon(
                Icons.Default.Home, contentDescription = "Home",
            ) },
            label = { Text("Home")}
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = {
                selectedTab = 1
                navController.navigate("screen4")
            },
            icon = { Icon(
                Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat",
            ) },
            label = { Text("Chat")}
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = {
                selectedTab = 2
                navController.navigate("Group")
                      },
            icon = { Icon(
                 Icons.Default.Group, contentDescription = "Group")},
            label = {Text("Group")})
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = {
                selectedTab = 3
                navController.navigate("screen3")
            },
            icon = { Icon(
                Icons.Default.Person, contentDescription = "Person",
            ) },
            label = { Text("Profile")}
        )
    }
    }





@Preview
@Composable
fun BottomnavigationPreview(){
    AppTheme {
        BottomNavigationAppTheme(navController = rememberNavController())
    }
}

