package se.umu.johe6327.asteroids

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Resources
import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import se.umu.johe6327.asteroids.databinding.FragmentGameBinding
import kotlin.properties.Delegates
import android.media.MediaPlayer

/**
 * Fragment that holds most of the game logic about collision, removal, creation and movement
 * of different image views.
 */
class GameFragment : Fragment() {
    private val viewModel:SharedViewModel by activityViewModels()
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private lateinit var asteroids: MutableList<ImageView>
    private lateinit var lasers: MutableList<ImageView>
    private lateinit var aliens: MutableList<ImageView>
    private lateinit var alienObject: Alien
    private lateinit var laserObject: Laser
    private lateinit var asteroidObject: Asteroid
    private val objectData = FlyingObjectData()
    private val game = Game()
    private lateinit var soundPool: SoundPool
    private var explosionSoundId by Delegates.notNull<Int>()
    private lateinit var mediaPlayer: MediaPlayer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.topAppBar!!.inflateMenu(R.menu.top_app_bar)
        asteroids = listOf<ImageView>().toMutableList()
        lasers = listOf<ImageView>().toMutableList()
        aliens = listOf<ImageView>().toMutableList()

        binding.topAppBar!!.setNavigationOnClickListener{
            objectPos("unCreate")
            redoGUI()
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure you want to go to home screen?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    resetAttributes()
                    view.findNavController().navigate(R.id.action_gameFragment_to_menuFragment)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
        binding.topAppBar!!.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.info -> {
                    information()
                    true
                }
                else -> false
            }
        }
        binding.gameFragment.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                redoGUI()
                stars()
                sound()
                binding.gameFragment.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        soundPool = SoundPool.Builder()
            .setMaxStreams(10) // Set the maximum number of simultaneous streams
            .build()
        val laserSoundId = soundPool.load(requireContext(), R.raw.shoot, 1)
        explosionSoundId = soundPool.load(requireContext(), R.raw.playerexplode, 1)
        binding.shipImageView.setOnTouchListener { ship, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (motionEvent.pointerCount == 2) {
                        if(!checkForPause() && (viewModel.laserAmount < 4)){
                            viewModel.incrementLaser()
                            soundPool.play(laserSoundId, 0.5f, 0.5f, 0, 0, 1f)
                            createShipLaser()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_DOWN -> {
                    if(viewModel.pause){
                        startGame() }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!checkForPause()){
                        var newX = motionEvent.rawX - ship.width / 2
                        var newY = motionEvent.rawY - ship.height / 2

                        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

                        newX = newX.coerceIn(0f, (screenWidth - ship.width).toFloat())
                        newY = newY.coerceIn(0f, (screenHeight - ship.height).toFloat())

                        ship.x = newX
                        ship.y = newY
                        viewModel.saveShipCoordinates(ship.x, ship.y)
                    }
                    true
                }
                else -> false
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_gameFragment_to_menuFragment)
        }
        return view
    }

    // Sets information for top bar
    private fun information(){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(R.string.howToPlay).setCancelable(true)
        val alert = builder.create()
        alert.show()
    }

    // starts the sound animations
    private fun sound(){
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.bakgroundsound)
        mediaPlayer.isLooping = true // Set to loop the sound
        mediaPlayer.start()
    }

    // starts star animations
    private fun stars() {
        val starAnimation = Stars(binding.star1!!, binding.star2!!, binding.star3!!, binding.star4!!, binding.star5!!,
            binding.star6!!, binding.star7!!, binding.star8!!, binding.star9!!, binding.star10!!)
        starAnimation.start()
    }

    // Removes alienView that goes beneath screen height.
    private fun removeAlienView(alien: ImageView) {
        val parentLayout = binding.gameFragment
        parentLayout.removeView(alien)

        val copyAlien = aliens.toMutableList()
        copyAlien.remove(alien)
        aliens = copyAlien
    }

    // Removes asteroidView if it collides with ship or bottom of screen
    private fun removeAsteroid(asteroid: ImageView) {
        val parentLayout = binding.gameFragment
        parentLayout.removeView(asteroid)

        val copyAsteroids = asteroids.toMutableList()
        copyAsteroids.remove(asteroid)
        asteroids = copyAsteroids
    }

    // Removes laserView if it passes screenHeight
    private fun removeLaser(laser: ImageView) {
        val parentLayout = binding.gameFragment
        parentLayout.removeView(laser)

        val copyLaser = lasers.toMutableList()
        copyLaser.remove(laser)
        lasers = copyLaser
        viewModel.decreaseLaser()
    }

    // Keeps the game score
    private fun scoreCount(score: Int) {
        viewModel.incrementScore(score)
        binding.hitsText.text = String.format(getString(R.string.score) + " %d", viewModel.score)
    }

    // Removes lifeViews from player-life
    private fun loseLife(status: String) {
        fun playing(){
            when (viewModel.shipLife) {
                4 -> {binding.life1.visibility = View.INVISIBLE }
                3 -> {binding.life2.visibility = View.INVISIBLE }
                2 -> {binding.life3.visibility = View.INVISIBLE }
                1 -> {binding.life4.visibility = View.INVISIBLE }
                0 -> {binding.life5.visibility = View.INVISIBLE }
            }
            soundPool.play(explosionSoundId, 1f, 1f, 0, 0, 1f)
        }
        fun resume(){
            when (viewModel.shipLife) {
                4 -> {binding.life1.visibility = View.INVISIBLE }
                3 -> {
                    binding.life1.visibility = View.INVISIBLE
                    binding.life2.visibility = View.INVISIBLE }
                2 -> {
                    binding.life1.visibility = View.INVISIBLE
                    binding.life2.visibility = View.INVISIBLE
                    binding.life3.visibility = View.INVISIBLE }
                1 -> {
                    binding.life1.visibility = View.INVISIBLE
                    binding.life2.visibility = View.INVISIBLE
                    binding.life3.visibility = View.INVISIBLE
                    binding.life4.visibility = View.INVISIBLE }
            }
        }
        when (status) {
            "Resume" -> {resume()}
            "Playing" -> {playing()}
        }
    }

    // redo views after closing the game.
    private fun redoGUI() {
        // If game is already started else starting screen
        if (viewModel.gameStarted){
            binding.hitsText.text = String.format(getString(R.string.score) + " %d", viewModel.score)
            viewModel.savePause(true)
            binding.startText.text = getString(R.string.paus)
            binding.startText.visibility = View.VISIBLE
            viewModel.handler.removeCallbacksAndMessages(null)
            loseLife("Resume")
            objectPos("reCreate")
        }
        else{
            resetAttributes()
            viewModel.savePause(true)
            binding.startText.text = getString(R.string.start_text)
            binding.startText.visibility = View.VISIBLE
        }
    }

    // reset game
    private fun resetAttributes() {
        viewModel.resetGame()
        binding.gameFragment.invalidate()
    }

    // Load objects positions
    private fun objectPos(status: String) {

        fun reCreate(){
            binding.shipImageView.y = viewModel.shipY
            binding.shipImageView.x = viewModel.shipX

            for (index in 0..viewModel.laser.size){
                if (index < viewModel.laser.size) {
                    reCreateLasers(index)
                }
            }
            for (index in 0..viewModel.alien.size){
                if (index < viewModel.alien.size) {
                    reCreateAliens(index)
                }
            }
            for (index in 0..viewModel.asteroid.size){
                if (index < viewModel.asteroid.size) {
                    reCreateAsteroid(index)
                }
            }
        }

        fun unCreate(){
            for (laser in lasers){
                removeLaser(laser)
            }
            for (asteroid in asteroids){
                removeAsteroid(asteroid)
            }
            for(alien in aliens){
                removeAlienView(alien)
            }
        }
        when (status) {
            "reCreate" -> {reCreate()}
            "unCreate" -> {unCreate()}
        }
    }

    // Runnable i main thread
    private val moveRunnable = object : Runnable {
        override fun run() {
            if (!checkForLose()){
                movement()
                collision()
                if(!checkForPause()){
                    viewModel.handler.postDelayed(this, 1000 / 60) // 60 frames per second
                    viewModel.incrementScore(1)
                    binding.hitsText.text = String.format(getString(R.string.score) + " %d", viewModel.score)
                }
            }
            else{
                viewModel.handler.removeCallbacksAndMessages(null)
                viewModel.saveHighScore(viewModel.score)
                findNavController().navigate(R.id.action_gameFragment_to_endFragment)
            }
        }
        // All view collisions
        private fun collision() {
            fun shipHit(){
                binding.shipImageView.setImageResource(R.drawable.ship_2)
                viewModel.handler.postDelayed({
                    binding.shipImageView.setImageResource(R.drawable.ship) }, 60)
                viewModel.decreaseLife()
                loseLife("Playing")
            }
            for((index, asteroid) in asteroids.withIndex()) {
                if (game.collidesWithShip(asteroid, binding.shipImageView)) {
                    shipHit()
                    removeAsteroid(asteroid)
                    val currentAsteroid = viewModel.asteroid
                    if (index >= 0 && index < currentAsteroid.size) {
                        currentAsteroid.remove(currentAsteroid[index])
                    }
                    break
                }
            }
            for((index, alien) in aliens.withIndex()) {
                if (game.collidesWithShip(alien, binding.shipImageView)) {
                    shipHit()
                    removeAlienView(alien)
                    val currentAliens = viewModel.alien
                    if (index >= 0 && index < currentAliens.size) {
                        currentAliens.remove(currentAliens[index])
                    }
                    break
                }
            }
            for (asteroid in asteroids){

                collidesWithLaser(asteroid, "asteroid")
            }
            for (alien in aliens){
                collidesWithLaser(alien, "alien")
            }
        }

        // Movement of all views
        private fun movement() {
            val currentAsteroids = viewModel.asteroid
            for((index, asteroid) in asteroids.withIndex()){
                if (index >= 0 && index < currentAsteroids.size) {
                    asteroid.translationY += asteroidObject.speed
                    currentAsteroids[index].y += asteroidObject.speed
                    if (asteroid.translationY > viewModel.screenHeight){
                        removeAsteroid(asteroid)
                        currentAsteroids.remove(currentAsteroids[index])
                    }
                }
            }
            val currentLasers = viewModel.laser
            for ((index, laser) in lasers.withIndex()){
                if (index >= 0 && index < currentLasers.size) {
                    laser.translationY -= laserObject.speed
                    currentLasers[index].y -= laserObject.speed
                    if(laser.translationY < 160f){
                        removeLaser(laser)
                        currentLasers.remove(currentLasers[index])
                    }
                }
            }
            val currentAliens = viewModel.alien
            for((index, alien) in aliens.withIndex()){
                alienObject.movement(alien, currentAliens, index)
                if (alien.translationY > viewModel.screenHeight){
                    removeAlienView(alien)
                    if (index >= 0 && index < currentAliens.size) {
                        currentAliens.remove(currentAliens[index])
                    }
                }
            }
        }
    }

    // start game loop
    private fun startLoop() {
        if (!checkForPause() && !checkForLose()){
            viewModel.handler.post(moveRunnable)
            viewModel.handler.post(object : Runnable {
                override fun run() {
                    if (!checkForPause() && !checkForLose()){
                        asteroidSpawn()
                    }
                }
                private fun asteroidSpawn() {
                    if (viewModel.score < 2000){
                        createAsteroid()
                        viewModel.handler.postDelayed(this, 1500)
                    }
                    else if (viewModel.score in 2001..4999){
                        createAsteroid()
                        viewModel.handler.postDelayed(this, 1000)
                    }
                    else if (viewModel.score in 5000..10000){
                        createAsteroid()
                        viewModel.handler.postDelayed(this, 700)
                    }
                    else if (viewModel.score in 10001..15000){
                        createAsteroid()
                        viewModel.handler.postDelayed(this, 500)
                    }
                    else if (viewModel.score in 15001..20000){
                        createAsteroid()
                        viewModel.handler.postDelayed(this, 400)
                    }
                    else if (viewModel.score > 20001){
                        createAsteroid()
                        viewModel.handler.postDelayed(this, 300)
                    }
                }
            })
            viewModel.handler.post(object : Runnable {
                override fun run() {
                    if (!checkForPause() && !checkForLose()){
                        alienSpawn()
                    }
                }
                private fun alienSpawn() {
                    if (viewModel.score < 3000){
                        createAlien()
                        viewModel.handler.postDelayed(this, 8000)
                    }
                    else if (viewModel.score in 3001..5999){
                        createAlien()
                        viewModel.handler.postDelayed(this, 7000)
                    }
                    else if (viewModel.score in 6000..9000){
                        createAlien()
                        viewModel.handler.postDelayed(this, 6000)
                    }
                    else if (viewModel.score in 9001..12000){
                        createAlien()
                        viewModel.handler.postDelayed(this, 5000)
                    }
                    else if (viewModel.score in 12001..15000){
                        createAlien()
                        viewModel.handler.postDelayed(this, 4000)
                    }
                    else if (viewModel.score in 12001..15000){
                        createAlien()
                        viewModel.handler.postDelayed(this, 3000)
                    }
                    else if (viewModel.score > 15001){
                        createAlien()
                        viewModel.handler.postDelayed(this, 2500)
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.savePause(true)
        soundPool.release()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    // Starts the game
    private fun startGame() {
        viewModel.saveGameStarted(true)
        viewModel.savePause(false)
        binding.startText.visibility = View.INVISIBLE
        startLoop()
    }

    // Check for loose
    private fun checkForLose() : Boolean{
         return binding.life5.visibility == View.INVISIBLE
    }

    // Check for pause
    private fun checkForPause() : Boolean{
        return viewModel.pause
    }

    // Checks collision between laser and flying object
    private fun collidesWithLaser(flyingObject: ImageView, objectId: String) {
        for((index, laser) in lasers.withIndex()){
            val intersection = game.laserCheck(laser, flyingObject)
            val currentLasers = viewModel.laser
            val currentAliens = viewModel.alien

            if (intersection){
                removeLaser(laser)
                if (index >= 0 && index < currentLasers.size) {
                    currentLasers.remove(currentLasers[index])
                }
                if (objectId == "asteroid"){
                    flyingObject.setImageResource(R.drawable.meteorex)
                    scoreCount(objectData.scoreAsteroid)
                    viewModel.handler.postDelayed({
                        removeAsteroid(flyingObject) }, 40)
                }
                else{
                    flyingObject.setImageResource(R.drawable.alienex_2)
                    scoreCount(objectData.scoreAlien)
                    viewModel.handler.postDelayed({
                        removeAlienView(flyingObject)
                        if (index >= 0 && index < currentAliens.size) {
                            currentAliens.remove(currentAliens[index])
                        }}, 40)
                }
            }
        }
    }

    // Creates and add alienView to parent layout
    private fun createAlien() {
        val alienImageView = ImageView(context)
        alienImageView.setImageResource(R.drawable.alien_2)
        val layoutParams = ConstraintLayout.LayoutParams(objectData.alienWidth, objectData.alienHeight)
        alienImageView.layoutParams = layoutParams

        aliens.add(alienImageView)
        val parentLayout = binding.gameFragment

        parentLayout.addView(alienImageView)
        alienImageView.translationX = objectData.alienX
        alienImageView.translationY = objectData.alienY

        alienObject = Alien(objectData.alienX, objectData.alienY, objectData.alienSpeedX,
            objectData.alienSpeedY, objectData.rightBorder, objectData.leftBorder, objectData.wallR, objectData.wallL)
        viewModel.saveAlienObject(alienObject)
    }

    // recreates alien views after process death
    private fun reCreateAliens(index: Int){
        val alienImageView = ImageView(context)
        alienImageView.setImageResource(R.drawable.alien_2)
        val layoutParams = ConstraintLayout.LayoutParams(objectData.alienWidth, objectData.alienHeight)
        alienImageView.layoutParams = layoutParams

        aliens.add(alienImageView)
        val parentLayout = binding.gameFragment

        parentLayout.addView(alienImageView)
        val savedAliens = viewModel.alien

        alienImageView.translationX = savedAliens[index].x
        alienImageView.translationY = savedAliens[index].y
        alienObject = Alien(savedAliens[index].x, savedAliens[index].y, savedAliens[index].deltaX,
            savedAliens[index].deltaY, savedAliens[index].rightBorder, savedAliens[index].leftBorder,
            savedAliens[index].wallR, savedAliens[index].wallL)
    }

    // Creates and add laserView to parent layout
    private fun createShipLaser() {
        val laserView = ImageView(context)
        laserView.setImageResource(R.drawable.laser)
        val layoutParams = ConstraintLayout.LayoutParams(objectData.laserWidth, objectData.laserHeight)
        laserView.layoutParams = layoutParams

        lasers.add(laserView)
        val parentLayout = binding.gameFragment

        parentLayout.addView(laserView)
        laserView.translationX = binding.shipImageView.translationX + objectData.laserX
        laserView.translationY = binding.shipImageView.translationY + objectData.laserY

        laserObject = Laser(binding.shipImageView.translationX + objectData.laserX,
            binding.shipImageView.translationY + objectData.laserY, objectData.laserSpeed)
        viewModel.saveLaserObject(laserObject)
    }

    // recreates laser views after process death
    private fun reCreateLasers(index: Int){
        val laserView = ImageView(context)
        laserView.setImageResource(R.drawable.laser)

        val layoutParams = ConstraintLayout.LayoutParams(objectData.laserWidth, objectData.laserHeight)
        laserView.layoutParams = layoutParams

        lasers.add(laserView)
        val parentLayout = binding.gameFragment

        parentLayout.addView(laserView)
        val savedLasers = viewModel.laser
        laserView.translationX = savedLasers[index].x
        laserView.translationY = savedLasers[index].y
        laserObject = Laser(savedLasers[index].x, savedLasers[index].y, savedLasers[index].speed)
    }

    // Creates and add asteroidView to parent layout
    private fun createAsteroid(){
        val asteroidImageView = ImageView(context)
        asteroidImageView.setImageResource(R.drawable.meteor)
        val flyingObject = FlyingObjectData()
        val layoutParams = ConstraintLayout.LayoutParams(flyingObject.asteroidWidth, flyingObject.asteroidsHeight)
        asteroidImageView.layoutParams = layoutParams

        asteroids.add(asteroidImageView)
        val parentLayout = binding.gameFragment

        parentLayout.addView(asteroidImageView)
        asteroidImageView.translationX = flyingObject.asteroidX
        asteroidImageView.translationY = flyingObject.asteroidY

        asteroidObject = Asteroid(flyingObject.asteroidWidth, flyingObject.asteroidsHeight, flyingObject.asteroidX,
            flyingObject.asteroidY, flyingObject.deltaY)
        viewModel.saveAsteroidObject(asteroidObject)
    }

    // recreates asteroid views after process death
    private fun reCreateAsteroid(index: Int){
        val savesAsteroid = viewModel.asteroid
        val asteroidImageView = ImageView(context)
        asteroidImageView.setImageResource(R.drawable.meteor)
        val layoutParams = ConstraintLayout.LayoutParams(savesAsteroid[index].width, savesAsteroid[index].height)
        asteroidImageView.layoutParams = layoutParams

        asteroids.add(asteroidImageView)
        val parentLayout = binding.gameFragment

        parentLayout.addView(asteroidImageView)
        asteroidImageView.translationX = savesAsteroid[index].x
        asteroidImageView.translationY = savesAsteroid[index].y

        asteroidObject = Asteroid(savesAsteroid[index].width, savesAsteroid[index].height, savesAsteroid[index].x,
            savesAsteroid[index].y, savesAsteroid[index].speed)
    }
}