package pro.progr.diamondtimer

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp

@Composable
fun TimerBar(backFun : () -> Unit) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.timer_title))
        },
        navigationIcon = {
            IconButton(onClick = { backFun() }) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.back))
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}
