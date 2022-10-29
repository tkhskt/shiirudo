package com.tkhskt.shiirudo.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkhskt.shiirudo.annotation.Shiirudo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    fun dispatch(event: Event) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    @Shiirudo
    sealed interface Event {

        interface DialogEvent {
            object ShowDialog : Event

            object CloseDialog : Event
        }

        object ShowSnackBar : Event

        object Foop : Event
    }
}
