package app.nakanishi.daiki.magic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.shape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.LocalContentAlpha
import app.nakanishi.daiki.magic.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapWithButtons(navController: NavController) {

    var text by remember { mutableStateOf("") }
    val doublelift = Color(0xffa0d8ef)
    val tilt = Color(0xff90ee90)
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = Color.Black, // テキストの色
                cursorColor = Color.Gray,// カーソルの色
                focusedBorderColor = Color.Gray
            ),
            label = { Text (
                text = "技名を検索",
                color = Color.Black
            )},
            singleLine = true,
            leadingIcon = {
                Icon(
                    Icons.Default.Search, // アイコンのリソース
                    contentDescription = "Search" // アイコンの説明
                )
            },

            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp),
                )
                .height(60.dp)
                .zIndex(1f)

        )

        MapZoomableLayout {
            Box(
                Modifier
                    .fillMaxSize()
            ) {
                // ボタン1
                Button(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = doublelift
                    ),
                    onClick = { navController.navigate("screen1") },
                    modifier = Modifier
                        .offset(100.dp, 100.dp)
                        .size(120.dp)
                        .clip(CircleShape)

                        .shadow(8.dp, CircleShape),
                ) {
                    Text(
                        modifier = Modifier.padding(0.dp),
                        text = "ダブルリフト",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }

                // ボタン2
                Button(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = tilt
                    ),
                    onClick = { /* ボタン2のクリック時の処理 */ },
                    modifier = Modifier
                        .offset(300.dp, 300.dp)
                        .size(120.dp)
                        .clip(CircleShape)
                        .shadow(8.dp, CircleShape)

                ) {
                    Text(
                        modifier = Modifier.padding(0.dp),
                        text = "ティルト",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
@Composable
fun MapZoomableLayout(content: @Composable () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offset += pan
                }
            }
                .background(color = Color.White)
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                )
        ) {
            // ボタンの再描画を制御する
            CompositionLocalProvider(LocalContentAlpha provides if (scale > 1f) ContentAlpha.high else ContentAlpha.disabled) {
                content()
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MapPreview() {
    AppTheme {

        MapWithButtons(navController = rememberNavController())
    }
}