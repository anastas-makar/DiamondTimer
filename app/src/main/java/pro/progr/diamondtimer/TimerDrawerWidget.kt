package pro.progr.diamondtimer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun TimerDrawerWidget(navFun : () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
        .clickable {
            navFun()
        }) {

        Image(
            painter = painterResource(id = R.drawable.timer),
            contentDescription = "Таймер",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )

        Text(text = "Открыть таймер с наградами", modifier = Modifier.padding(5.dp))

    }
}