package app.nakanishi.daiki.magic

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var searchResults by remember { mutableStateOf<List<User>?>(null) }
    var noResults by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentUserId = PreferenceManager.getDocumentPath(context)
    var targetUserId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SearchUser") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("screen4") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ユーザーIDまたは名前で検索") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End
            ){
                Button(
                    onClick = {
                        val query = searchQuery.text.trim().toLowerCase()
                        if (query.isEmpty()) {
                            showError = true
                            return@Button
                        }

                        val db = Firebase.firestore
                        val usersRef = db.collection("users")

                        usersRef.whereGreaterThanOrEqualTo(FieldPath.documentId(), query)
                            .whereLessThanOrEqualTo(FieldPath.documentId(), query + "\uf8ff")
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val results = querySnapshot.documents.mapNotNull { document ->
                                    document.toObject(User::class.java)?.apply { id = document.id }
                                }.toMutableList()

                                usersRef.whereGreaterThanOrEqualTo("name", query)
                                    .whereLessThanOrEqualTo("name", query + "\uf8ff")
                                    .get()
                                    .addOnSuccessListener { nameQuerySnapshot ->
                                        val nameResults =
                                            nameQuerySnapshot.documents.mapNotNull { document ->
                                                document.toObject(User::class.java)?.apply { id = document.id }
                                            }
                                        results.addAll(nameResults)
                                        searchResults = results.distinctBy { it.id } // ユーザーIDで重複を削除
                                        if (searchResults!!.isEmpty()) {
                                            noResults = true
                                            showError = false
                                        } else {
                                            noResults = false
                                            showError = false
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        searchResults = emptyList()
                                        showError = true
                                        Log.e("UserSearchScreen", "Name search failed", exception)
                                    }
                            }
                            .addOnFailureListener { exception ->
                                searchResults = emptyList()
                                showError = true
                                Log.e("UserSearchScreen", "ID search failed", exception)
                            }
                    },
                ) {
                    Icon(
                        Icons.Default.Search,contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("検索する")
                }
            }
            if (showError) {
                Text("入力内容がありません")
            }

            if (searchResults != null) {
                if (searchResults!!.isEmpty()) {
                    showError = false
                    if (noResults) {
                        Text("該当するユーザーはいません")
                    }
                } else {
                    Column {
                        showError = false
                        Text("検索結果:")
                        searchResults!!.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${user.name} (${user.id})", modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                Button(
                                    onClick = {
                                        targetUserId = user.id // Set targetUserId
                                        println("Button clicked for user: ${user.id}")
                                        println("targetUserId set to: $targetUserId")
                                        if (currentUserId != null) {
                                            sendFriendRequest(currentUserId, targetUserId, context)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Text("SendRequest")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class User(
    var id: String = "",
    val name: String = ""
)

fun sendFriendRequest(currentUserId: String, targetUserId: String, context: Context) {
    println("currentUserId: $currentUserId")
    println("targetUserId: $targetUserId")
    if (targetUserId.isEmpty()) {
        Log.e("sendFriendRequest", "targetUserId is empty")
        Toast.makeText(context, "Target user ID is empty", Toast.LENGTH_SHORT).show()
        return
    }

    val db = Firebase.firestore
    val currentUserRef = db.collection("users").document(currentUserId)
    val targetUserRef = db.collection("users").document(targetUserId)
    db.runTransaction { transaction ->
        // Read current user's document
        val currentUserSnapshot = transaction.get(currentUserRef)
        val currentUserRequestingList = currentUserSnapshot.get("requesting") as? List<String> ?: emptyList()

        // Read target user's document
        val targetUserSnapshot = transaction.get(targetUserRef)
        val targetUserRequestedByList = targetUserSnapshot.get("requestedBy") as? List<String> ?: emptyList()

        // Update current user's document
        val updatedRequestingList = currentUserRequestingList + targetUserId
        transaction.update(currentUserRef, "requesting", updatedRequestingList)

        // Update target user's document
        val updatedRequestedByList = targetUserRequestedByList + currentUserId
        transaction.update(targetUserRef, "requestedBy", updatedRequestedByList)

        // Return a success signal
        null
    }.addOnSuccessListener {
        Log.d("sendFriendRequest", "フレンドリクエストを送信しました")
        Toast.makeText(context, "フレンドリクエストを送信しました", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener { exception ->
        Log.e("sendFriendRequest", "リクエスト送信に失敗しました", exception)
        Toast.makeText(context, "リクエスト送信に失敗しました", Toast.LENGTH_SHORT).show()
    }
}

