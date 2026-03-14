package com.peter.overtimecalculator

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.peter.overtimecalculator.data.AppContainer

val Application.appContainer: AppContainer
    get() = (this as OvertimeApplication).appContainer

inline fun <reified VM : ViewModel> appViewModelFactory(
    crossinline create: () -> VM,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(VM::class.java)) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }
            return create() as T
        }
    }
}
