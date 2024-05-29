package app.nakanishi.daiki.magic

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ChatActivity(navController: NavController){
    Text(
        fontSize = 40.sp,
        text = "Chat"
    )
}