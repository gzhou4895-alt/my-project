package com.example.hello

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogViewModel : ViewModel() {

    private val _logs = MutableLiveData<String>("")
    val logs: LiveData<String> = _logs

    fun appendLog(msg: String) {
        val current = _logs.value ?: ""
        _logs.postValue(current + msg + "\n")
    }

    fun clear() {
        _logs.postValue("")
    }
}
