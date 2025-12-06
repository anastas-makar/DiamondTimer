package pro.progr.diamondtimer

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ChipDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerContent(
    state: StateFlow<TimerUiState>,
    diamondsCount: StateFlow<Int>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onRestart: () -> Unit,
    onClaim: () -> Unit,
    onClaimAndRestart: () -> Unit,
    onChangeDurationMinutes: (Int) -> Unit,
    onChangeReward: (Int) -> Unit,
    durationOptionsMinutes: List<Int> = listOf(1, 5, 10, 15, 25, 30, 45, 60),
    rewardOptions: List<Int> = listOf(1, 5, 10, 15, 25, 50, 100),
    modifier: Modifier = Modifier
) {
    val ui by state.collectAsState()
    val diamonds by diamondsCount.collectAsState()

    val context = LocalContext.current

    // Чтобы не звенеть каждый раз при любой перерисовке, а только в момент перехода в isFinished = true
    var wasFinished by remember { mutableStateOf(false) }

    LaunchedEffect(ui.isFinished) {
        if (ui.isFinished && !wasFinished) {
            wasFinished = true

            // Вибрация
            vibrateOnce(context)

            // Звук (можешь закомментировать, если не нужен)
            playNotificationSound(context)
        }
        if (!ui.isFinished) {
            // как только выходим из состояния "финиш", разрешаем следующую сработку
            wasFinished = false
        }
    }

    val progress =
        if (ui.totalMs == 0L) 0f
        else 1f - (ui.remainingMs.toFloat() / ui.totalMs.toFloat())

    val diamondInlineId = "diamond"

    val inlineContent = mapOf(
        diamondInlineId to InlineTextContent(
            Placeholder(
                width = 1.em,
                height = 1.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextBottom
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_diamond),
                contentDescription = "бриллианты"
            )
        }
    )

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Шапка: бриллианты
        Box(modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.primary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
        ) {
            Text("Бриллианты:  $diamonds", fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(5.dp))
        }

        // Выбор длительности
        Text("Длительность", fontWeight = FontWeight.SemiBold)

        FlowRow (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            durationOptionsMinutes.forEach { m ->
                FilterChip(
                    selected = ui.totalMs == m.minutesMs && !ui.isRunning && !ui.isPaused,
                    onClick = { if (!ui.isRunning && !ui.isPaused) onChangeDurationMinutes(m) },
                    content = { Text("$m мин") },
                    enabled = !ui.isRunning && !ui.isPaused,
                    colors = ChipDefaults.filterChipColors(
                        selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.25f),
                        selectedContentColor = MaterialTheme.colors.primary
                    )
                )
            }
        }

        // Выбор награды
        Text("Награда", fontWeight = FontWeight.SemiBold)

        FlowRow (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rewardOptions.forEach { r ->
                FilterChip(
                    selected = ui.selectedReward == r && !ui.isClaiming,
                    onClick = { if (!ui.isClaiming) onChangeReward(r) },
                    content = {
                        Text(
                            text = buildAnnotatedString {
                                append(r.toString())
                                append(" ")
                                appendInlineContent(diamondInlineId)
                            },
                            inlineContent = inlineContent
                        )
                    },
                    enabled = !ui.isRunning && !ui.isPaused,
                    colors = ChipDefaults.filterChipColors(
                        selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.25f),
                        selectedContentColor = MaterialTheme.colors.primary
                    )
                )
            }
        }

        // Таймер
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
                Text(
                    text = formatMsAsMMSS(ui.remainingMs),
                    textAlign = TextAlign.Center
                )

                // Кнопки управления
                if (!ui.isFinished) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when {
                            !ui.isRunning && !ui.isPaused -> {
                                Button(onClick = onStart) { Text("Старт") }
                                OutlinedButton(onClick = onReset, enabled = ui.remainingMs != ui.totalMs) { Text("Сброс") }
                            }
                            ui.isRunning -> {
                                Button(onClick = onPause) { Text("Пауза") }
                                OutlinedButton(onClick = onReset) { Text("Сброс") }
                            }
                            ui.isPaused -> {
                                Button(onClick = onResume) { Text("Продолжить") }
                                OutlinedButton(onClick = onReset) { Text("Сброс") }
                            }
                        }
                    }
                } else {
                    // Экран финиша
                    Text("Время вышло")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onClaim, enabled = ui.canClaim && !ui.isClaiming) {
                            if (ui.isClaiming) CircularProgressIndicator(Modifier.size(18.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Забрать ${ui.selectedReward} ")
                                    appendInlineContent(diamondInlineId)
                                },
                                inlineContent = inlineContent
                            )
                        }
                        Button(onClick = onClaimAndRestart, enabled = ui.canClaim && !ui.isClaiming) {
                            if (ui.isClaiming) CircularProgressIndicator(Modifier.size(18.dp))
                            Text("Забрать и перезапустить")
                        }
                    }
                    OutlinedButton(onClick = onReset, enabled = !ui.isClaiming) { Text("Не забирать и сбросить таймер") }
                }

                ui.lastError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}

private fun vibrateOnce(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                /* milliseconds = */ 400L,
                /* amplitude   = */ VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(400L)
    }
}

private fun playNotificationSound(context: Context) {
    try {
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, notificationUri)
        ringtone?.play()
    } catch (_: Throwable) {
        // тихо игнорируем, если что-то пошло не так
    }
}
