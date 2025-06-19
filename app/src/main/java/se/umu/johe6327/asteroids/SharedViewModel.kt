package se.umu.johe6327.asteroids

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * Holds data for the different fragments and methods that save or changes the data.
 */
class SharedViewModel(
    application: Application,
    private val state: SavedStateHandle
): AndroidViewModel(application) {
    val screenHeight = Resources.getSystem().displayMetrics.heightPixels
    val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val SHIP_TRANSLATION_X_KEY = "ship_x_coordinate"
        private const val SHIP_TRANSLATION_Y_KEY = "ship_y_coordinate"
        private const val LASER_AMOUNT_KEY = "laser_amount"
        private const val SCORE_KEY = "score"
        private const val PAUSE_KEY = "pause"
        private const val GAME_STARTED_KEY = "game_started"
        private const val SHIP_LIFE_KEY = "ship_life"
        const val HIGH_SCORE_KEY = "high_scores"
        private const val HIGH_SCORES_FILENAME = "highScore.text"
        private const val LASER_KEY = "laser"
        private const val ALIEN_KEY = "alien"
        private const val ASTEROID_KEY = "asteroid"
    }

    var highScores: ArrayList<Int>
    init {
        val scoresFromState: ArrayList<Int>? = state[HIGH_SCORE_KEY]
        val scoresFromFile = loadHighScoresFromFile()
        val combinedScores = mutableSetOf<Int>()

        if (scoresFromState!=null){
            combinedScores.addAll(scoresFromState)
        }
        combinedScores.addAll(scoresFromFile)
        val sortedCombinedScores = ArrayList(combinedScores.toList())
        sortedCombinedScores.sortDescending()

        highScores = ArrayList(sortedCombinedScores.take(10))
        state[HIGH_SCORE_KEY] = highScores
    }

    var shipX = state.get<Float>(SHIP_TRANSLATION_X_KEY) ?: 0f
    var shipY = state.get<Float>(SHIP_TRANSLATION_Y_KEY) ?: 0f
    var laserAmount = state.get<Int>(LASER_AMOUNT_KEY) ?: 0
    var score = state.get<Int>(SCORE_KEY) ?: 0
    var pause = state.get<Boolean>(PAUSE_KEY) ?: false
    var gameStarted = state.get<Boolean>(GAME_STARTED_KEY) ?: false
    var shipLife = state.get<Int>(SHIP_LIFE_KEY) ?: 5
    var laser = state.get<MutableList<Laser>>(LASER_KEY) ?: mutableListOf()
    var alien = state.get<MutableList<Alien>>(ALIEN_KEY) ?: mutableListOf()
    var asteroid = state.get<MutableList<Asteroid>>(ASTEROID_KEY) ?: mutableListOf()

    private fun loadHighScoresFromFile(): ArrayList<Int> {
        val loadedScores = ArrayList<Int>()
        val context = getApplication<Application>()
        val file = File(context.filesDir, HIGH_SCORES_FILENAME)

        if (!file.exists()){
            Log.d("SharedViewModel", "High score file does no exist. Returning empty list.")
            return loadedScores
        }

        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = context.openFileInput(HIGH_SCORES_FILENAME)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                line?.toIntOrNull()?.let { scoreValue ->
                    loadedScores.add(scoreValue)
                }
            }
            Log.d("SharedViewModel", "Succesfully read ${loadedScores.size} scores from file.")
        } catch (e: Exception) {
            Log.d("SharedViewModel", "Error reading highscores file: ${e.message}", e)
        } finally {
            try {
                fileInputStream?.close()
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error closing file input stream: ${e.message}", e)
            }
        }
        return loadedScores
    }

    fun saveHighScore(newScore: Int) {
        if (!highScores.contains(newScore)) {
            highScores.add(newScore)
            Log.d("SaveScore", "Score $newScore added to list.")
        }else {
            Log.d("SaveScore", "Score $newScore already in list or not added.")
        }

        highScores.sortDescending()
        if (highScores.size > 10){
            highScores = ArrayList(highScores.subList(0, 10))
            Log.d("SaveScore", "List after trimming to top 10: $highScores")
        }

        state[HIGH_SCORE_KEY] = highScores
        Log.d("SaveScore", "Updated SavedStateHandle with: $highScores")
        saveHighScoresToFile()
    }

    fun saveHighScoresToFile() {
        val context = getApplication<Application>()
        var fileOutputStream: FileOutputStream? = null
        try {
            val content = highScores.joinToString("\n")
            fileOutputStream = context.openFileOutput(HIGH_SCORES_FILENAME, Context.MODE_PRIVATE)
            fileOutputStream.write(content.toByteArray())
            //Log.d("SharedViewModel", "High scores saved to file.")
            Log.d("SharedViewModel", "High scores saved to file: $highScores")
        } catch (e: Exception) {
            Log.d("SharedViewModel", "Error saving high scores file: ${e.message}", e)
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: Exception) {
                Log.d("SharedViewModel", "Error closing file output stream: ${e.message}", e)
            }
        }
    }

    fun resetGameSessionVariables(){
        Log.d("SharedViewModel", "--- resetGameSessionVariables() CALLED ---")
        shipX = 0f
        shipY = 0f
        laserAmount = 0
        score = 0   //current games score reset to 0
        pause = false
        gameStarted = false // a new game session hasn't started yet
        shipLife = 5
        laser.clear()
        alien.clear()
        asteroid.clear()
        handler.removeCallbacksAndMessages(null)

        state[SHIP_TRANSLATION_X_KEY] = shipX
        state[SHIP_TRANSLATION_Y_KEY] = shipY
        state[LASER_AMOUNT_KEY] = laserAmount
        state[SCORE_KEY] = score
        state[PAUSE_KEY] = pause
        state[GAME_STARTED_KEY] = gameStarted
        state[SHIP_LIFE_KEY] = shipLife
        state[LASER_KEY] = laser // Persist cleared lists if that's the intent
        state[ALIEN_KEY] = alien
        state[ASTEROID_KEY] = asteroid
        Log.d("SharedViewModel", "Game session variables reset.")
    }

    fun DANGEROUSLY_CLEAR_ALL_HIGH_SCORES_DEBUG_ONLY() {
        Log.e("SharedViewModel", "--- DANGEROUSLY_CLEAR_ALL_HIGH_SCORES_DEBUG_ONLY CALLED ---")
        highScores.clear()
        state[HIGH_SCORE_KEY] = highScores
        saveHighScoresToFile() // This will save an empty list
    }

    fun saveShipCoordinates(x: Float, y: Float) {
        shipX = x
        shipY = y
        state[SHIP_TRANSLATION_X_KEY] = shipX
        state[SHIP_TRANSLATION_Y_KEY] = shipY
    }
    fun incrementLaser(){
        laserAmount += 1
        state[LASER_AMOUNT_KEY] = laserAmount
    }
    fun decreaseLaser(){
        laserAmount -= 1
        state[LASER_AMOUNT_KEY] = laserAmount
    }
    fun incrementScore(value: Int){
        score += value
        state[SCORE_KEY] = score
    }
    fun savePause(b: Boolean){
        pause = b
        state[PAUSE_KEY] = pause
    }
    fun saveGameStarted(b: Boolean) {
        gameStarted = b
        state[GAME_STARTED_KEY] = gameStarted
    }
    fun decreaseLife(){
        shipLife --
        state[SHIP_LIFE_KEY] = shipLife
    }
    fun saveLaserObject(laserObject: Laser) {
        laser.add(laserObject)
        state[LASER_KEY] = laser
    }
    fun saveAlienObject(alienObject: Alien) {
        alien.add(alienObject)
        state[ALIEN_KEY] = alien
    }
    fun saveAsteroidObject(asteroidObject: Asteroid){
        asteroid.add(asteroidObject)
        state[ASTEROID_KEY] = asteroid
    }
}