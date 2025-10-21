package pro.progr.diamondtimer

import java.util.concurrent.TimeUnit

fun formatMsAsMMSS(ms: Long): String {
    val totalSec = TimeUnit.MILLISECONDS.toSeconds(ms)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}
