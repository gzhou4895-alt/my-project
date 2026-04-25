package com.example.hello

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogViewModel : ViewModel() {

    private val _logs = MutableLiveData("")
    val logs: LiveData<String> = _logs

    // 👉 添加一行日志
    fun appendLog(msg: String) {
        val current = _logs.value ?: ""
        _logs.postValue(current + msg + "\n")
    }

    // 👉 清空日志（备用功能）
    fun clear() {
        _logs.postValue("")
    }
}
