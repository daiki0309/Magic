package app.nakanishi.daiki.magic

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.nakanishi.daiki.magic.ui.theme.AppTheme
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


object PreferenceManager {
    private const val PREFS_NAME = "Profile"
    private const val KEY_NAME = "name"
    private const val KEY_DOCUMENT_PATH = "document_path"
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
@Composable
fun ProfileActivity(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var fetchedName by remember { mutableStateOf("Name") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    var magic by remember { mutableStateOf(0) }
    val sharedPreferences = context.getSharedPreferences("Profile", Context.MODE_PRIVATE)

    val doublelift = Color(0xffa0d8ef)
    val tilt = Color(0xff90ee90)

    var imageUrl by remember { mutableStateOf<String?>(null) }
    var documentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        name = PreferenceManager.getName(context)
    }
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get().addOnSuccessListener { result ->
            for (document in result) {
                imageUrl = document.getString("imageUrl") ?: ""
            }
        }
    }
    LaunchedEffect(Unit) {
        documentId = PreferenceManager.getDocumentPath(context)
        documentId?.let {
            getImageUrlFromFirestore(it) { fetchedImageUrl ->
                imageUrl = fetchedImageUrl
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.LightGray)
        ) {
            // 横長の背景画像
            Image(
                painter = rememberImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .build()
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .offset(y = -50.dp)
        ) {
            if (imageUrl != null) {
                Image(
                    painter = rememberImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }

        }
        Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
                
            ){
                Button(

                    onClick = { navController.navigate("Edit") },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )

                ) {

                    Icon(
                        Icons.Default.Edit,contentDescription = null,
                    )
                    Text(
                        text = "編集",
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            fontSize = 24.sp,
            modifier = Modifier
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp),
                )
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ),
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "習得したマジック            $magic 種類",
                color = Color.Black,
                fontSize = 20.sp
            )
            LazyRow(
                contentPadding = PaddingValues(20.dp)
            ) {
                items(1) {
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        ),
                        modifier = Modifier
                            .padding(2.dp)

                            .background(
                                color = doublelift,
                                shape = RoundedCornerShape(20.dp)
                            )
                    )
                    {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = ("ダブルリフト"),
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.padding(20.dp))
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        ),
                        modifier = Modifier
                            .padding(2.dp)

                            .background(
                                color = tilt,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = "ティルト",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("あなたの動画", fontSize = 20.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray)
            ) {
                // Placeholder for video
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

    }
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
        .addOnFailureListener {
            onComplete(false)
        }
}
fun saveOrUpdateImageInFirestore(context: Context, Image: String, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val documentPath = PreferenceManager.getDocumentPath(context)

    if (documentPath != null) {
        // ドキュメントパスが存在する場合は更新
        println("aaaa")
        println(Image)
        val documentReference = db.collection("users").document(documentPath)
        documentReference.update("imageUrl", Image)
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { e ->
                // エラー処理
                println("era----")
                println(e)
                onComplete()
            }
    } else {
        // ドキュメントパスが存在しない場合は新しいドキュメントを作成
        println("bbbb")
        val user = hashMapOf("imageUrl" to Image)
        val documentId = UUID.randomUUID().toString()
        db.collection("users").document(documentId)
            .set(user)
            .addOnSuccessListener { documentReference ->
                // 新しいドキュメントパスをSharedPreferencesに保存
                PreferenceManager.saveDocumentPath(context, documentId)
                onComplete()
            }
            .addOnFailureListener { e ->
                // エラー処理
                onComplete()
            }
    }
}
fun getImageUrlFromFirestore(documentId: String, onComplete: (String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(documentId)
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val imageUrl = documentSnapshot.getString("imageUrl")
                onComplete(imageUrl)
            } else {
                onComplete(null)
            }
        }
        .addOnFailureListener { e ->
            // エラー処理
            onComplete(null)
        }
}
@Composable
fun EditProfileActivity(navController: NavController ) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }
    val userId = "unique_user_id"
    var isEditing by remember { mutableStateOf(false) }
    var savedName by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var name by remember { mutableStateOf("") }
    var savedData by remember { mutableStateOf("") }

    var documentId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var imageDocumentId by remember {
        mutableStateOf(
            PreferenceManager.getDocumentPath(context).orEmpty()
        )
    }

    LaunchedEffect(Unit) {
        name = PreferenceManager.getName(context)
    }
    LaunchedEffect(Unit) {
        if (documentId.isNotEmpty()) {
            getImageUrlFromFirestore(documentId) { fetchedImageUrl ->
                imageUrl = fetchedImageUrl ?: ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.LightGray)
        ) {
            Image(
                painter = rememberImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .build()
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .offset(y = -50.dp)
        ) {

            if (imageUri != null) {
                Image(
                    painter = rememberImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            } else {
                if (imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }

            }


            IconButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = { imagePickerLauncher.launch("image/*") },
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
            }
        }
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                label = {
                    Text(
                        text = "名前",
                        color = Color.Black
                    )
                },
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp),
                modifier = Modifier
                    .background(Color.White, shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                PreferenceManager.saveName(context, name)
                // 保存処理
                isLoading = true
                saveOrUpdateNameInFirestore(context, name) {
                    savedName = name
                    isLoading = false
                }
                isEditing = false
                navController.navigateUp()

                imageUri?.let { uri ->
                    uploadImageToFirebaseStorage(uri,
                        onSuccess = { uploadedImageUrl ->
                            if (documentId.isEmpty()) {
                                documentId = FirebaseFirestore.getInstance().collection("users").document().id
                                PreferenceManager.saveDocumentPath(context, documentId)
                            }
                            saveOrUpdateImageInFirestore(context, uploadedImageUrl)  {
                                // 保存成功

                                imageUrl = uploadedImageUrl
                                navController.navigateUp()
                            }
                        },
                        onFailure = { exception ->
                            // エラー処理
                        }
                    )
                }
            }) {
                Text("保存")
            }

    }
}

@Composable
fun ProfileScreen(navController: NavController){
    val profileNavController = rememberNavController()
    NavHost(
        navController = profileNavController,
        startDestination = "profile"
    ){
        composable("profile"){ ProfileActivity(navController = profileNavController)}
        composable("Edit"){
                backStackEntry ->
            val currentName = backStackEntry.arguments?.getString("currentName") ?: ""
            EditProfileActivity(navController )
        }
    }
}
object PreferenceManagerPath {
    private const val PREFS_NAME = "Profile"
    private const val KEY_DOCUMENT_PATH = "document_path"

    fun saveDocumentPath(context: Context, documentPath: String) {
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
fun saveOrUpdateNameInFirestore(context: Context, name: String, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val sharedPreferences = context.getSharedPreferences("Profile", Context.MODE_PRIVATE)
    val documentPath = PreferenceManager.getDocumentPath(context)

    if (documentPath != null) {
        // ドキュメントパスが存在する場合は更新
        val documentReference = db.collection("users").document(documentPath)
        documentReference.update("name", name)
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { e ->
                // エラー処理
                onComplete()
            }
    } else {
        // ドキュメントパスが存在しない場合は新しいドキュメントを作成
        val user = hashMapOf("name" to name)
        val documentId = UUID.randomUUID().toString()
        db.collection("users").document(documentId)
            .set(user)
            .addOnSuccessListener { documentReference ->
                // 新しいドキュメントパスをSharedPreferencesに保存
                PreferenceManager.saveDocumentPath(context, documentId)
                onComplete()
            }
            .addOnFailureListener { e ->
                // エラー処理
                onComplete()
            }
    }
}
fun getDataFromFirestore(documentId: String, onComplete: (String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(documentId)
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val data = documentSnapshot.getString("data")
                onComplete(data)
            } else {
                onComplete(null)
            }
        }
        .addOnFailureListener { e ->
            // Handle the error
            onComplete(null)
        }
}
fun getUserDataFromFirestore(documentId: String, onComplete: (String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(documentId)
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val name = documentSnapshot.getString("name")
                onComplete(name)
            } else {
                onComplete(null)
            }
        }
        .addOnFailureListener { e ->
            // エラー処理
            onComplete(null)
        }
}
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
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

