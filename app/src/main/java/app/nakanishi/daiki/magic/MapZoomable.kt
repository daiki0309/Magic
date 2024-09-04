package app.nakanishi.daiki.magic

import android.net.Uri
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.LocalContentAlpha
import app.nakanishi.daiki.magic.ui.theme.AppTheme
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.max
import kotlin.math.min

data class CustomButton(
    val label: String,
    val position: Offset,
    val color: ButtonColors,
    val onClick: () -> Unit
)

@Composable
fun MapWithButtons(navController: NavController) {
    val context = LocalContext.current

    // 技術リスト
    val techniques = listOf(
        Technique(
            name = "ダブルリフト",
            requiredSkills = listOf("なし"),
            nextSteps = listOf("アンビシャス・カード", "ピンキーカウント", "フォールス・シャッフル"),
            sampleVideoUri = Uri.parse("sample_video_uri_for_doublelift"),
            userVideoUri = getSavedVideoUriFromPreferences(context,"ダブルリフト")
        ),
        Technique(
            name = "ティルト",
            requiredSkills = listOf("ダブルリフト"),
            nextSteps = listOf("リバース・カード", "ミスディレクション"),
            sampleVideoUri = Uri.parse("sample_video_uri_for_tilt"),
            userVideoUri = getSavedVideoUriFromPreferences(context,"ティルト")
        )
        // 他の技を追加
    )
    val baseX = 100f
    val baseY = 100f
    val xSpacing = 200f
    val ySpacing = 400f
    val yOffset = 100f
    val numRows = 3

    // ボタンのリストを生成
    val buttons = techniques.mapIndexed { index, technique ->
        val x = baseX + (index % numRows) * xSpacing
        val y = baseY + (index / numRows) * ySpacing + (index % numRows) * yOffset
        CustomButton(
            label = technique.name,
            position = Offset(x, y),
            color = ButtonDefaults.textButtonColors(
                containerColor = Color.Gray
            )
        ) {
            navController.navigate("techniqueDetail/${technique.name}")
        }
    }


    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedButtonOffset by remember { mutableStateOf<Offset?>(null) }
    var scale by remember { mutableStateOf(1f) } // scale の定義を追加

    val suggestions = buttons.map { it.label }.filter { it.contains(searchQuery, ignoreCase = true) }
    val animatedOffset by animateOffsetAsState(targetValue = offset)

    Column {
        Box(
            modifier = Modifier
                .zIndex(1f)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { newText ->
                    searchQuery = newText
                    expanded = true
                },
                label = { Text(text = "技名を検索") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .height(60.dp)
                    .zIndex(2f),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        val button = buttons.find { it.label.contains(searchQuery, true) }
                        selectedButtonOffset = button?.position
                        selectedButtonOffset?.let {
                            offset = Offset(
                                (350f - it.x) / scale,
                                (600f - it.y) / scale
                            )
                        }
                    }
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f)
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        onClick = {
                            searchQuery = suggestion
                            expanded = false
                            val button = buttons.find { it.label == suggestion }
                            selectedButtonOffset = button?.position
                            selectedButtonOffset?.let {
                                offset = Offset(
                                    (350f - it.x) / scale,
                                    (600f - it.y) / scale
                                )
                            }
                        },
                        text = { Text(text = suggestion) }
                    )
                }
            }
        }

        // MapZoomableLayout に scale と offset を渡す
        MapZoomableLayout(
            scale = scale,
            offset = offset,
            onScaleChange = { newScale -> scale = newScale },
            onOffsetChange = { newOffset -> offset = newOffset }
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = max(1f, min(scale * zoom, 5f))
                            offset = offset + pan
                        }
                    }
            ) {
                buttons.forEachIndexed { index, button ->
                    Button(
                        onClick = button.onClick,
                        colors = button.color,
                        modifier = Modifier
                            .offset {
                                val diagonalOffset = 50f // 斜めのオフセット
                                IntOffset(
                                    ((button.position.x + animatedOffset.x) * scale + diagonalOffset * (index % 3)).toInt(),
                                    ((button.position.y + animatedOffset.y) * scale + diagonalOffset * (index / 3)).toInt()
                                )
                            }
                            .scale(scale)
                            .size(128.dp)
                            .clip(shape = CircleShape)
                    ) {
                        Text(
                            text = button.label,
                            fontSize = 12.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MapZoomableLayout(
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onScaleChange(max(1f, min(scale * zoom, 5f)))
                    onOffsetChange(offset + pan)
                }
            }
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
            content()
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