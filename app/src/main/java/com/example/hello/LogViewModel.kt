package com.example.hello

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogViewModel : ViewModel() {

    // 存储聊天记录
    private val _logs = MutableLiveData<String>("")
    val logs: LiveData<String> get() = _logs

    /**
     * 只记录重要的对话内容
     * 过滤掉所有包含“路径”、“检测”、“失败”等调试信息的日志
     */
    fun log(message: String) {
        // --- 【核心修改：拦截噪音】 ---
        val noiseKeywords = listOf("检测本地", "引擎启动", "请确认", "storage/", "等待权限")
        if (noiseKeywords.any { message.contains(it) }) {
            return // 如果包含这些关键词，直接丢弃，不更新到 UI
        }

        // 只保留真正的对话或成功的提示
        val currentLog = _logs.value ?: ""
        _logs.postValue("$currentLog\n$message")
    }

    /**
     * 提供一个清空功能，用于在引擎就绪后一键重置界面
     */
    fun clearLogs() {
        _logs.postValue("")
    }
}
