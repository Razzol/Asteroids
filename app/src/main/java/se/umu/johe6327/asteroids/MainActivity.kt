package se.umu.johe6327.asteroids

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity


/**
 * Starts up gameFragment as it's content to show and read-, writes to internal file for high score.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel:SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveHighScoresToFile()
    }
}