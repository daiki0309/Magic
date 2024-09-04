package app.nakanishi.daiki.magic

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController){
    val context = LocalContext.current
    val currentUserId = PreferenceManager.getDocumentPath(context)
    var friendRequesting by remember { mutableStateOf<List<String>>(emptyList()) }
    var requestedBy by remember { mutableStateOf<List<String>>(emptyList()) }
    val db = Firebase.firestore

    // リスナーを設定して友達申請の変更を監視
    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            val userRef = db.collection("users").document(currentUserId)
            userRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("NotificationScreen", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    friendRequesting = snapshot.get("requesting") as? List<String> ?: emptyList()
                    requestedBy = snapshot.get("requestedBy") as? List<String> ?: emptyList()
                } else {
                    Log.d("NotificationScreen", "Current data: null")
                }
            }
        }
    }

    Scaffold(
        topBar =  {
            TopAppBar(
                title = { Text("Notification") },
                navigationIcon = {
                    IconButton(onClick = {navController.navigate("profile")}){
                        Icon(Icons.Default.ArrowBack,contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text("通知", modifier = Modifier.padding(16.dp))

            if (friendRequesting.isNotEmpty()) {
                Text("友達申請中:", modifier = Modifier.padding(16.dp))
                friendRequesting.forEach { userId ->
                    Text(text = "$userId さんにリクエストしました", modifier = Modifier.padding(8.dp))
                }
            } else {
                Text("友達申請中の通知はありません", modifier = Modifier.padding(8.dp))
            }

            if (requestedBy.isNotEmpty()) {
                Text("友達申請を受け取りました:", modifier = Modifier.padding(16.dp))
                requestedBy.forEach { userId ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(text = "$userId さんからのリクエスト", modifier = Modifier.weight(1f))
                        Button(onClick = {
                            acceptFriendRequest(currentUserId!!, userId, context)
                        }) {
                            Text("承認")
                        }
                    }
                }
            } else {
                Text("友達申請の通知はありません", modifier = Modifier.padding(8.dp))
            }
        }
    }
}
fun acceptFriendRequest(currentUserId: String, targetUserId: String, context: Context) {
    val db = Firebase.firestore
    val currentUserRef = db.collection("users").document(currentUserId)
    val targetUserRef = db.collection("users").document(targetUserId)

    db.runTransaction { transaction ->
        // 現在のユーザーの友達リストを取得
        val currentUserSnapshot = transaction.get(currentUserRef)
        val currentUserFriends = currentUserSnapshot.get("friend") as? List<String> ?: emptyList()
        val currentUserRequestedBy = currentUserSnapshot.get("requestedBy") as? List<String> ?: emptyList()

        // ターゲットユーザーの友達リストを取得
        val targetUserSnapshot = transaction.get(targetUserRef)
        val targetUserFriends = targetUserSnapshot.get("friend") as? List<String> ?: emptyList()
        val targetUserRequesting = targetUserSnapshot.get("requesting") as? List<String> ?: emptyList()

        // 友達リストを更新
        transaction.update(currentUserRef, "friend", currentUserFriends + targetUserId)
        transaction.update(targetUserRef, "friend", targetUserFriends + currentUserId)

        // リクエストリストを更新
        transaction.update(currentUserRef, "requestedBy", currentUserRequestedBy - targetUserId)
        transaction.update(targetUserRef, "requesting", targetUserRequesting - currentUserId)
    }.addOnSuccessListener {
        Log.d("acceptFriendRequest", "友達リクエストを承認しました")
        Toast.makeText(context, "友達リクエストを承認しました", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener { exception ->
        Log.e("acceptFriendRequest", "友達リクエストの承認に失敗しました", exception)
        Toast.makeText(context, "友達リクエストの承認に失敗しました", Toast.LENGTH_SHORT).show()
    }
}