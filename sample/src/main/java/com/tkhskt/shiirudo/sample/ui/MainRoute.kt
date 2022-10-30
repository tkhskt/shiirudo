package com.tkhskt.shiirudo.sample.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.tkhskt.shiirudo.sample.MainViewModel
import com.tkhskt.shiirudo.sample.MainViewModelEventShiirudoBuilder
import com.tkhskt.shiirudo.sample.shiirudo
import kotlinx.coroutines.flow.Flow

@Composable
fun MainRoute(viewModel: MainViewModel) {
    var showDialog by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    LaunchedEffect(viewModel.event) {
        viewModel.event.collectEvent {
            isDialogShow {
                showDialog = true
            }
            isDialogClose {
                showDialog = false
            }
            isShowToast {
                Toast.makeText(context, "Toast!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    MainScreen(
        showDialog = showDialog,
        onClickShowDialogButton = { viewModel.dispatch(MainViewModel.Event.Dialog.Show) },
        onClickCloseDialogButton = { viewModel.dispatch(MainViewModel.Event.Dialog.Close) },
        onClickShowToastBarButton = { viewModel.dispatch(MainViewModel.Event.ShowToast) },
    )
}

private suspend fun Flow<MainViewModel.Event>.collectEvent(
    handler: MainViewModelEventShiirudoBuilder.() -> Unit,
) {
    collect {
        it.shiirudo(handler)
    }
}
