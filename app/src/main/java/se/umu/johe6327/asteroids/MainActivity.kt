package se.umu.johe6327.asteroids

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * Starts up gameFragment as it's content to show and read-, writes to internal file for high score.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel:SharedViewModel by viewModels()
    private val filePath = "highScore.text"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun readFromFile() {
        val file = File(filesDir, filePath)

        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: Exception) {
                Log.e("MyClass", "Error creating file: ${e.message}", e)
                return
            }
        }

        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = openFileInput(filePath)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                line?.toIntOrNull()?.let { viewModel.saveHighScore(it) }
            }
        } catch (e: Exception) {
            Log.e("MyClass", "Error reading file: ${e.message}", e)
        } finally {
            fileInputStream?.close()
        }
    }

    fun saveToFile(unSortedArray: ArrayList<Int>) {
        var fileOutputStream: FileOutputStream? = null
        try {
            val content = unSortedArray.joinToString("\n")
            fileOutputStream = openFileOutput(filePath, Context.MODE_PRIVATE)
            fileOutputStream.write(content.toByteArray())
        } catch (e: Exception) {
            Log.e("MyClass", "Error saving file: ${e.message}", e)
        } finally {
            fileOutputStream?.close()
        }
    }
}