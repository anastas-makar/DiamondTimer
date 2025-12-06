package pro.progr.diamondtimer

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pro.progr.diamondapi.AccumulateInterface
import androidx.lifecycle.viewModelScope

class TimerViewModel(
    private val api: AccumulateInterface,
    initialMinutes: Int = 25,
    initialReward: Int = 10,
    private val tickMs: Long = 100L // частота обновления
) : ViewModel() {

    private val _state = MutableStateFlow(
        TimerUiState(
            totalMs = initialMinutes.minutesMs,
            remainingMs = initialMinutes.minutesMs,
            selectedReward = initialReward
        )
    )
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    // Отображение бриллиантов из внешнего API
    val diamondsCount: StateFlow<Int> =
        api.getDiamondsCount()
            .catch { emit(0) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private var tickerJob: Job? = null
    private var endRealtimeMs: Long? = null // монотонические часы конца интервала

    fun setDurationMinutes(minutes: Int) {
        // менять длительность можно только когда таймер не бежит
        val s = _state.value
        if (s.isRunning) return
        val newMs = minutes.minutesMs
        _state.update {
            it.copy(
                totalMs = newMs,
                remainingMs = newMs,
                isFinished = false,
                canClaim = false,
                lastError = null
            )
        }
    }

    fun setReward(reward: Int) {
        _state.update { it.copy(selectedReward = reward, lastError = null) }
    }

    fun start() {
        val s = _state.value
        if (s.isRunning) return
        if (s.remainingMs <= 0L) {
            // если по нулям — сначала сбросим
            reset()
        }
        launchTicker(resume = false)
    }

    fun pause() {
        val s = _state.value
        if (!s.isRunning || s.isPaused) return
        // Остановить тики, зафиксировать остаток
        tickerJob?.cancel()
        tickerJob = null
        endRealtimeMs = null
        _state.update { it.copy(isRunning = false, isPaused = true, lastError = null) }
    }

    fun resume() {
        val s = _state.value
        if (s.isRunning || !s.isPaused) return
        launchTicker(resume = true)
    }

    fun reset() {
        tickerJob?.cancel()
        tickerJob = null
        endRealtimeMs = null
        _state.update {
            it.copy(
                remainingMs = it.totalMs,
                isRunning = false,
                isPaused = false,
                isFinished = false,
                canClaim = false,
                isClaiming = false,
                lastError = null
            )
        }
    }

    fun restart() {
        // сброс + старт
        _state.update {
            it.copy(
                remainingMs = it.totalMs,
                isRunning = false,
                isPaused = false,
                isFinished = false,
                canClaim = false,
                isClaiming = false,
                lastError = null
            )
        }
        start()
    }

    fun flush() {
        // сброс
        _state.update {
            it.copy(
                remainingMs = it.totalMs,
                isRunning = false,
                isPaused = false,
                isFinished = false,
                canClaim = true,
                isClaiming = false,
                lastError = null
            )
        }
    }

    fun claim(onSuccess: (() -> Unit)? = null) {
        val s = _state.value
        if (!s.canClaim || s.isClaiming) return
        _state.update { it.copy(isClaiming = true, lastError = null) }

        viewModelScope.launch {
            val reward = s.selectedReward
            val result = runCatching { api.add(reward) }.getOrElse { Result.failure(it) }
            // api.add возвращает Result<Boolean>
            val ok = result.isSuccess && (result.getOrNull() == true)

            if (ok) {
                _state.update { it.copy(canClaim = false, isClaiming = false, lastError = null) }
                onSuccess?.invoke()
            } else {
                _state.update {
                    it.copy(
                        isClaiming = false,
                        lastError = "Не удалось выдать награду"
                    )
                }
            }
        }
    }

    fun claimAndRestart() {
        claim { restart() }
    }

    fun claimAndFlush() {
        claim { flush() }
    }

    private fun launchTicker(resume: Boolean) {
        val now = SystemClock.elapsedRealtime()
        val startRemaining = _state.value.remainingMs
        if (!resume) {
            // старт с текущего остатка
            endRealtimeMs = now + startRemaining
        } else {
            // продолжение: тоже от текущего остатка
            endRealtimeMs = now + startRemaining
        }

        _state.update { it.copy(isRunning = true, isPaused = false, isFinished = false, lastError = null) }

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val end = endRealtimeMs ?: break
                val left = (end - SystemClock.elapsedRealtime()).coerceAtLeastZero
                if (left <= 0L) {
                    // финиш
                    endRealtimeMs = null
                    _state.update {
                        it.copy(
                            remainingMs = 0L,
                            isRunning = false,
                            isPaused = false,
                            isFinished = true,
                            canClaim = true
                        )
                    }
                    break
                } else {
                    _state.update { it.copy(remainingMs = left) }
                }
                delay(tickMs)
            }
        }
    }
}
