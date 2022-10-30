package com.tkhskt.shiirudo.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tkhskt.shiirudo.sample.ui.MainRoute
import com.tkhskt.shiirudo.sample.ui.theme.ShiirudoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect(::logEvent)
            }
        }
        setContent {
            ShiirudoTheme {
                MainRoute(viewModel)
            }
        }
    }

    private fun logEvent(event: MainViewModel.Event) = event.shiirudo()
        .isShowDialog {
            Log.d("ViewModel Event", "show dialog")
        }
        .isCloseDialog {
            Log.d("ViewModel Event", "close dialog")
        }
        .isShowToast {
            Log.d("ViewModel Event", "show toast")
        }
        .isElse {
            Log.d("ViewModel Event", "else")
        }.execute()
}
