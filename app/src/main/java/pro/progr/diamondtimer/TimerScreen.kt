package pro.progr.diamondtimer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

@Composable
fun TimerScreen(backFun : () -> Unit,
                 vm : TimerViewModel) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .navigationBarsPadding(),
        topBar = {
            Box(modifier = Modifier.statusBarsPadding()) {
                TimerBar(
                    backFun
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxHeight()
            ) {
                TimerContent(
                    state = vm.state,
                    diamondsCount = vm.diamondsCount,
                    onStart = vm::start,
                    onPause = vm::pause,
                    onResume = vm::resume,
                    onReset = vm::reset,
                    onRestart = vm::restart,
                    onClaim = vm::claimAndFlush,
                    onClaimAndRestart = vm::claimAndRestart,
                    onChangeDurationMinutes = vm::setDurationMinutes,
                    onChangeReward = vm::setReward)
            }
        }
    )
}