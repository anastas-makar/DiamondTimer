package pro.progr.diamondtimer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerScreen(
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
    durationOptionsMinutes: List<Int> = listOf(5, 10, 15, 25, 30, 45, 60),
    rewardOptions: List<Int> = listOf(1, 5, 10, 12, 15, 50, 100),
    modifier: Modifier = Modifier
) {
    val ui by state.collectAsState()
    val diamonds by diamondsCount.collectAsState()

    val progress =
        if (ui.totalMs == 0L) 0f
        else 1f - (ui.remainingMs.toFloat() / ui.totalMs.toFloat())

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Шапка: бриллианты
        Card {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Бриллианты")
                Text(diamonds.toString())
            }
        }

        // Выбор длительности
        Text("Длительность")
        FlowRowWrap(modifier = Modifier.fillMaxWidth()) {
            durationOptionsMinutes.forEach { m ->
                FilterChip(
                    selected = ui.totalMs == m.minutesMs && !ui.isRunning && !ui.isPaused,
                    onClick = { if (!ui.isRunning && !ui.isPaused) onChangeDurationMinutes(m) },
                    content = { Text("$m мин") },
                    enabled = !ui.isRunning && !ui.isPaused
                )
            }
        }

        // Выбор награды
        Text("Награда")
        FlowRowWrap(modifier = Modifier.fillMaxWidth()) {
            rewardOptions.forEach { r ->
                FilterChip(
                    selected = ui.selectedReward == r && !ui.isClaiming,
                    onClick = { if (!ui.isClaiming) onChangeReward(r) },
                    content = { Text("$r 💎") }
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
                            Text("Забрать ${ui.selectedReward} 💎")
                        }
                        Button(onClick = onClaimAndRestart, enabled = ui.canClaim && !ui.isClaiming) {
                            if (ui.isClaiming) CircularProgressIndicator(Modifier.size(18.dp))
                            Text("Забрать и перезапустить")
                        }
                        OutlinedButton(onClick = onReset, enabled = !ui.isClaiming) { Text("Сброс") }
                    }
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

/**
 * Простейшая обёртка — «переносимые» чипы в несколько строк без зависимостей.
 */
@Composable
private fun FlowRowWrap(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
