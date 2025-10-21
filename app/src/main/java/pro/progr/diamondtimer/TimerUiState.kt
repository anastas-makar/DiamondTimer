package pro.progr.diamondtimer

data class TimerUiState(
    val totalMs: Long = 25.minutesMs,        // выбранная длительность
    val remainingMs: Long = 25.minutesMs,    // сколько осталось
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val selectedReward: Int = 10,            // выбранная награда
    val canClaim: Boolean = false,           // можно ли забирать награду (после финиша и не забирали)
    val isClaiming: Boolean = false,         // идёт запрос add()
    val lastError: String? = null
)

internal val Int.minutesMs: Long get() = this * 60_000L
internal val Long.coerceAtLeastZero: Long get() = if (this < 0L) 0L else this
