package com.peter.overtimecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peter.overtimecalculator.ui.OvertimeCalculatorApp
import com.peter.overtimecalculator.ui.OvertimeViewModel
import com.peter.overtimecalculator.ui.theme.OvertimeCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OvertimeCalculatorTheme {
                val viewModel: OvertimeViewModel = viewModel(
                    factory = OvertimeViewModel.provideFactory(application),
                )
                OvertimeCalculatorApp(viewModel)
            }
        }
    }
}
