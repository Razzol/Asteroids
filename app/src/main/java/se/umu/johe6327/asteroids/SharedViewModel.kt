package se.umu.johe6327.asteroids

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController

/**
 * Holds data for the different fragments and methods that save or changes the data.
 */
class SharedViewModel(private val state: SavedStateHandle): ViewModel() {
    val handler = Handler(Looper.getMainLooper())
    val screenHeight = Resources.getSystem().displayMetrics.heightPixels
    companion object {
        private const val SHIP_TRANSLATION_X_KEY = "ship_x_coordinate"
        private const val SHIP_TRANSLATION_Y_KEY = "ship_y_coordinate"
        private const val LASER_AMOUNT_KEY = "laser_amount"
        private const val SCORE_KEY = "score"
        private const val PAUSE_KEY = "pause"
        private const val GAME_STARTED_KEY = "game_started"
        private const val SHIP_LIFE_KEY = "ship_life"
        private const val HIGH_SCORE_KEY = "high_scores"
        private const val LASER_KEY = "laser"
        private const val ALIEN_KEY = "alien"
        private const val ASTEROID_KEY = "asteroid"
    }
    var shipX = state.get<Float>(SHIP_TRANSLATION_X_KEY) ?: 0f
    var shipY = state.get<Float>(SHIP_TRANSLATION_Y_KEY) ?: 0f
    var laserAmount = state.get<Int>(LASER_AMOUNT_KEY) ?: 0
    private var _score = state.getLiveData(SCORE_KEY, 0)
    val score: LiveData<Int> = _score
    var pause = state.get<Boolean>(PAUSE_KEY) ?: false
    var gameStarted = state.get<Boolean>(GAME_STARTED_KEY) ?: false
    private var _shipLife = state.getLiveData(SHIP_LIFE_KEY, 5)
    val shipLife: LiveData<Int> = _shipLife
    var highScores = state.get<ArrayList<Int>>(HIGH_SCORE_KEY) ?: arrayListOf()
    var laser = state.get<MutableList<Laser>>(LASER_KEY) ?: mutableListOf()
    var alien = state.get<MutableList<Alien>>(ALIEN_KEY) ?: mutableListOf()
    var asteroid = state.get<MutableList<Asteroid>>(ASTEROID_KEY) ?: mutableListOf()

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
        _score.value = (_score.value ?: 0) + value
        /*
        score += value
        state[SCORE_KEY] = score

         */
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
        _shipLife.value = (_shipLife.value ?: 5) -1
        /*
        shipLife --
        state[SHIP_LIFE_KEY] = shipLife
         */
    }
    fun saveHighScore(score: Int) {
        highScores.add(score)
        state[HIGH_SCORE_KEY] = highScores
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
    fun resetGame(){
        shipX = 0f
        shipY = 0f
        laserAmount = 0
        _score.value = 0
        pause = false
        gameStarted = false
        _shipLife.value = 5
        highScores.clear()
        laser.clear()
        alien.clear()
        handler.removeCallbacksAndMessages(null)
    }


    // Check for loose
    fun checkForLose() : Boolean{
        return shipLife.value == 0
    }

    // Check for pause
    fun checkForPause() : Boolean{
        return pause
    }

    // Runnable i main thread
    val moveRunnable = object : Runnable {
        override fun run() {
            if (!checkForLose()){
                if(!checkForPause()){
                    handler.postDelayed(this, 1000 / 60) // 60 frames per second
                    incrementScore(1)
                }
            }
            else{
                handler.removeCallbacksAndMessages(null)
                score.value?.let { saveHighScore(it) }
            }
        }
    }
}