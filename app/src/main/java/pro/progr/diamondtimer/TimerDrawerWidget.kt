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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
            contentDescription = stringResource(R.string.timer_image_description),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )

        Column(modifier = Modifier.padding(5.dp)) {
            Text(
                text = stringResource(R.string.open_timer_with_rewards),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

    }
}
