package pro.progr.diamondtimer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun TimerDrawerWidget(diamondsTotal: State<Int>,
                       navFun : () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
        .clickable {
            navFun()
        }) {

        Image(
            painter = painterResource(id = R.drawable.timer),
            contentDescription = "Сундук",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )

        Column(modifier = Modifier.align(Alignment.CenterHorizontally))  {
            Text(text = "Открыть таймер", modifier = Modifier.align(Alignment.CenterHorizontally))
            Row(modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally)) {
                Text(text = "с ${diamondsTotal.value}")
            }
        }

    }
}