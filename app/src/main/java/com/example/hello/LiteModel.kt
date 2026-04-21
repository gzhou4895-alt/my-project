package com.example.hello

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class LiteModel(context: Context) {

    private val interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile(context, "model.tflite"))
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)

        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun run(input: FloatArray): FloatArray {
        val output = FloatArray(1)
        interpreter.run(input, output)
        return output
    }
}
