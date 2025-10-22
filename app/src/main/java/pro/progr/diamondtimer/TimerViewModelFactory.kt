package pro.progr.diamondtimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pro.progr.diamondapi.AccumulateInterface

class TimerViewModelFactory(
    private val api: AccumulateInterface,
    private val minutes: Int,
    private val reward: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TimerViewModel::class.java))
        return TimerViewModel(api, minutes, reward) as T
    }
}