package com.debduttapanda.coroutinecancellable

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class MainViewModel: ViewModel() {

    val buttonText = mutableStateOf("Run")
    val number = mutableStateOf(-1)
    var job: Job? = null
    fun onButtonClick() {
        if(buttonText.value=="Run"){
            runProcess()
        }
        else{
            job?.cancel()
            buttonText.value = "Run"
        }
    }

    private fun runProcess() {
        buttonText.value = "Stop"
        /*job = viewModelScope.launch {
            generateNumbers()
                .onEach {
                    number.value = it
                }
                .collect()
        }*/
        ///////////////////////
        job = viewModelScope.launchCancelable {cancel->
            generateNumbers()
                .onEach {
                    number.value = it
                    if(it>5){
                        buttonText.value = "Run"
                        cancel()
                    }
                }
                .collect()
        }
    }

    private fun generateNumbers(): Flow<Int> = flow{
        var i = 0
        while(true){
            delay(2000)
            emit(++i)
        }
    }
}

public fun CoroutineScope.launchCancelable(//our very own cancellable coroutine
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.(()->Unit) -> Unit
): Job{
    var job: Job? = null
    val cancel = {
        job?.cancel()
    }
    job = launch(
        context = context,
        start = start
    ){
        block(cancel)
    }
    return job
}