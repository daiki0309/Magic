package app.nakanishi.daiki.magic

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.nakanishi.daiki.magic.ui.theme.AppTheme


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


@Composable
fun Greeting(navController: NavController) {

    val Coral = Color(0xFF76F2F7)
    val LightYellow = Color(0xFFFFFFFF)
    val gradient = Brush.verticalGradient(
        colors = listOf(LightYellow, Coral),
    )
    Box(
        modifier = Modifier
            .background(gradient)
            .fillMaxWidth()
    ){
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "ダブルリフト",
                fontSize = 36.sp,
                modifier = Modifier.padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.scrim

            )
            Text(
                text = "必要な技術",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.scrim
            )
            Card(
                modifier = Modifier
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
                        modifier = Modifier.padding(16.dp),
                        imageVector = Icons.Rounded.Done,
                        contentDescription = "Done"
                    )
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = " なし",
                        fontSize = 24.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Text(
                text = "ダブルリフトの次は...？",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.scrim,
                modifier = Modifier.padding(vertical = 8.dp)

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
                LazyRow(
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(1) {
                        Icon(
                            modifier = Modifier.padding(12.dp),
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Star",


                            )
                        ElevatedCard(
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            ),
                            modifier = Modifier
                                .padding(2.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        )
                        {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = ("アンビシャス・カード"),
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        ElevatedCard(
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            ),
                            modifier = Modifier
                                .padding(2.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = "ピンキーカウント",
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        ElevatedCard(
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            ),
                            modifier = Modifier
                                .padding(2.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = "フォールス・シャッフル",
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }


                }
            }

            Text(
                text = "お手本",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.scrim
            )

            Box(
                modifier = Modifier
                    .height(140.dp)
                    .width(400.dp)

            )

            Text(
                text = "あなたの動画",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp),
                color = MaterialTheme.colorScheme.scrim

            )
            Box(
                modifier = Modifier
                    .height(225.dp)
                    .width(400.dp)
                    .fillMaxSize()
                    .clip(RectangleShape)
                    .background(LightGray),
                        contentAlignment = Alignment.Center
            ){
                Column {
                    Text(
                        text = "動画がありません",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                    Button(
                        onClick = {/* */ },

                    ){
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            imageVector = Icons.Rounded.FileUpload,
                            contentDescription = "File",
                        )
                        Text(
                            text = "動画を挿入",
                            color = Color.Black
                        )
                    }
                }

            }
        }
    }

}






@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MagicScreen() {
    val navController = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize()
    ){
        Scaffold(

            floatingActionButton = {
                FloatingActionButton(onClick = { /* フローティングアクションボタンのクリック処理 */ }) {
                    MyFloatingActionButton()
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                NavigationBar {
                    BottomNavigationAppTheme(navController)
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "screen2",
                Modifier.padding(innerPadding)
            ) {
                composable("screen1") { Greeting(navController) }
                composable("screen2") { MapWithButtons(navController) }
                composable("screen3"){ ProfileScreen(navController)}
                composable("screen4"){ ChatActivity(navController)}
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
            .fillMaxWidth()
            .height(56.dp) // 例えば、bottom navigationの高さ
            .zIndex(1f) // 影の重なりを設定
            .shadow(4.dp) // 影の設定
    ){
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = {
                selectedTab = 0
                navController.navigate("screen2")
            },
            icon = { Icon(
                Icons.Default.Home, contentDescription = "Home",
                tint = Color.Black
            ) },
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = {
                selectedTab = 1
                navController.navigate("screen4")
            },
            icon = { Icon(
                Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat",
                tint = Color.Black
            ) },
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = {
                selectedTab = 2
                navController.navigate("screen3")
            },
            icon = { Icon(
                Icons.Default.Person, contentDescription = "Person",
                tint = Color.Black
            ) },
        )
    }
    }


@Composable
fun MyFloatingActionButton() {
    FloatingActionButton(
        onClick = { /*TODO*/ },
        contentColor = MaterialTheme.colorScheme.tertiary,
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
        modifier = Modifier
            .size(100.dp)
    ) {
        Icon(
            modifier = Modifier
                .padding(8.dp)
                .size(40.dp),
            imageVector = Icons.Rounded.Videocam,
            contentDescription = "Video",
        )
    }

}




@Preview(showBackground = true)
@Composable
fun MagicThemePreview() {
    AppTheme {
        val navController = rememberNavController()
        MagicScreen()
    }
}
