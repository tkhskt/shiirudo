package com.tkhskt.shiirudo.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tkhskt.shiirudo.sample.ui.theme.ShiirudoTheme

@Composable
fun MainScreen(
    showDialog: Boolean,
    onClickShowDialogButton: () -> Unit,
    onClickCloseDialogButton: () -> Unit,
    onClickShowToastBarButton: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { onClickShowDialogButton.invoke() }) {
                    Text(text = "Show Dialog")
                }
                Spacer(modifier = Modifier.size(32.dp))
                Button(onClick = { onClickShowToastBarButton.invoke() }) {
                    Text(text = "Show Toast")
                }
            }
            if (showDialog) {
                MainDialog(onClickCloseButton = onClickCloseDialogButton)
            }
        }
    }
}

@Composable
fun MainDialog(
    onClickCloseButton: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 48.dp),
        onDismissRequest = {},
        text = {
            Text(text = "Dialog!!")
        },
        buttons = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = { onClickCloseButton.invoke() }
                ) {
                    Text(text = "Close")
                }
            }

        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ShiirudoTheme {
        MainScreen(
            showDialog = false,
            onClickShowDialogButton = {},
            onClickCloseDialogButton = {},
            onClickShowToastBarButton = {}
        )
    }
}
