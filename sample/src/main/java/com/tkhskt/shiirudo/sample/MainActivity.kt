package com.tkhskt.shiirudo.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tkhskt.shiirudo.sample.ui.screen.MainScreen
import com.tkhskt.shiirudo.sample.ui.theme.ShiirudoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleEvent()
        setContent {
            ShiirudoTheme {
                MainScreen()
            }
        }
    }

    private fun handleEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is MainViewModel.Event.ShowDialog -> {

                        }
                        is MainViewModel.Event.CloseDialog -> {

                        }
                        is MainViewModel.Event.ShowSnackBar -> {

                        }
                    }
                }
            }
        }
    }
}
