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
import android.util.Log
import kotlin.random.Random

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
    private val objectData = FlyingObjectData()
    private val game = Collision()
    private lateinit var soundPool: SoundPool
    private var explosionSoundId by Delegates.notNull<Int>()
    private lateinit var mediaPlayer: MediaPlayer

    // Define the move runnable
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

            // Ship collisions with asteroids
            val asteroidsToRemoveAfterShipCollision = mutableListOf<ImageView>()
            for (asteroidView in asteroids) {
                if (game.collidesWithShip(asteroidView, binding.shipImageView)) {
                    shipHit()
                    asteroidsToRemoveAfterShipCollision.add(asteroidView)
                    val asteroidId = asteroidView.tag as? String
                    if (asteroidId != null) {
                        viewModel.removeAsteroidById(asteroidId)
                    }
                    break
                }
            }
            asteroidsToRemoveAfterShipCollision.forEach { removeAsteroid(it) }

            // Ship collision with aliens
            val aliensToRemoveAfterShipCollision = mutableListOf<ImageView>()
            for (alienView in aliens) {
                if (game.collidesWithShip(alienView, binding.shipImageView)) {
                    shipHit()
                    aliensToRemoveAfterShipCollision.add(alienView)
                    val alienId = alienView.tag as? String
                    if (alienId != null) {
                        viewModel.removeAlienById(alienId)
                    }
                    break
                }
            }
            aliensToRemoveAfterShipCollision.forEach { removeAlienView(it) }

            // Laser collision
            val lasersCopyForCollision = lasers.toList()

            outerLaserLoop@ for (laserView in lasersCopyForCollision) {
                val laserId = laserView.tag as? String
                if (laserId == null || !lasers.contains(laserView)) {
                    Log.d("CollisionSkip", "Skipping laser in collision check. Tag: ${laserView.tag}. ID: $laserId. Not in GameFragment.lasers anymore.")
                    continue@outerLaserLoop
                }

                // Check against asteroids
                for (asteroidView in asteroids.toList()) {
                    val asteroidId = asteroidView.tag as? String ?: continue

                    if (game.laserCheck(laserView, asteroidView)) {
                        viewModel.removeLaserById(laserId)
                        removeLaser(laserView)

                        asteroidView.setImageResource(R.drawable.meteorex)
                        scoreCount(objectData.scoreAsteroid)
                        viewModel.handler.postDelayed({
                            removeAsteroid(asteroidView)
                            viewModel.removeAsteroidById(asteroidId)
                        }, 40)

                        continue@outerLaserLoop
                    }
                }
                for (alienView in aliens.toList()) {
                    val alienId = alienView.tag as? String ?: continue

                    if (game.laserCheck(laserView, alienView)) {
                        viewModel.removeLaserById(laserId)
                        removeLaser(laserView)

                        alienView.setImageResource(R.drawable.alienex_2)
                        scoreCount(objectData.scoreAlien)
                        viewModel.handler.postDelayed({
                            removeAlienView(alienView)
                            viewModel.removeAlienById(alienId) }, 40)
                        continue@outerLaserLoop
                    }
                }
            }
        }

        // Movement of all views
        private fun movement() {

            // --- Asteroid movement ---
            val asteroidImageViewsToRemove = mutableListOf<ImageView>()
            for (asteroidView in asteroids) {
                val asteroidId = asteroidView.tag as? String
                if (asteroidId == null) {
                    asteroidImageViewsToRemove.add(asteroidView)
                    continue
                }

                val asteroidData = viewModel.asteroid.find { it.id == asteroidId }
                if (asteroidData == null) {
                    asteroidImageViewsToRemove.add(asteroidView)
                    continue
                }

                asteroidData.y += asteroidData.speed
                asteroidView.translationY = asteroidData.y

                viewModel.updateAsteroidPosition(asteroidData.id, asteroidData.x, asteroidData.y)

                if (asteroidView.translationY > viewModel.screenHeight) {
                    asteroidImageViewsToRemove.add(asteroidView)
                    viewModel.removeAsteroidById(asteroidId)
                }
            }
            asteroidImageViewsToRemove.forEach { viewToRemove ->
                removeAsteroid(viewToRemove)
            }

            // --- Laser movement ---
            val laserImageViewsToRemove = mutableListOf<ImageView>()
            for (laserView in lasers) {
                val laserId = laserView.tag as? String
                if (laserId == null) {
                    Log.w("LaserMovement", "Laser ImageView missing ID tag. Skipping.")
                    continue
                }

                val laserData = viewModel.laser.find { it.id == laserId }
                if (laserData == null) {
                    Log.w("LaserMovement", "No Laser data for ID: $laserId. Marking view for removal.")
                    laserImageViewsToRemove.add(laserView)
                    continue
                }

                laserData.y -= laserData.speed
                laserView.translationY = laserData.y

                viewModel.updateLaserPosition(laserData.id, laserData.x, laserData.y)

                if ((laserView.translationY + laserView.height) < 0) { // Or laserView.translationY + laserView.height < 0 if bottom isn't reliable
                    laserImageViewsToRemove.add(laserView)
                    viewModel.removeLaserById(laserId)
                }
            }
            laserImageViewsToRemove.forEach { viewToRemove ->
                removeLaser(viewToRemove)
            }

            // --- Alien movement ---
            val alienImageViewsToRemove = mutableListOf<ImageView>()
            for (alienView in aliens) {
                val alienId = alienView.tag as? String
                if (alienId == null) {
                    continue
                }
                val alienData = viewModel.alien.find { it.id == alienId }
                if (alienData == null) {
                    alienImageViewsToRemove.add(alienView)
                    continue
                }

                alienData.y += alienData.deltaY
                alienView.translationY = alienData.y

                if (alienData.movingRight) {
                    alienData.x += alienData.deltaX
                    if (alienData.x >= alienData.rightBorder) {
                        alienData.x = alienData.rightBorder
                        alienData.movingRight = false
                    }
                } else {
                    alienData.x -= alienData.deltaX
                    if (alienData.x <= alienData.leftBorder) {
                        alienData.x = alienData.leftBorder
                        alienData.movingRight = true
                    }
                }
                alienView.translationX = alienData.x

                viewModel.updateAlienPositionAndState(
                    alienData.id,
                    alienData.x,
                    alienData.y,
                    alienData.movingRight
                )

                if (alienView.translationY > viewModel.screenHeight) {
                    alienImageViewsToRemove.add(alienView)
                    viewModel.removeAlienById(alienId)
                }
            }
            alienImageViewsToRemove.forEach { viewToRemove ->
                removeAlienView(viewToRemove)
            }
        }
    }

    // Define the asteroid spawning runnable
    private val asteroidSpawnRunnable = object : Runnable {
        override fun run() {
            if (!checkForPause() && !checkForLose()){
                spawnAsteroidWithDelay()
            }
        }
    }

    // Define the alien spawning runnable
    private val alienSpawnRunnable = object : Runnable {
        override fun run() {
            if (!checkForPause() && !checkForLose()){
                spawnAlienWithDelay()
            }
        }
    }

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
            if (!viewModel.pause && viewModel.gameStarted) {
                viewModel.savePause(true)
                viewModel.handler.removeCallbacksAndMessages(null)
                binding.startText.text = getString(R.string.paus)
                binding.startText.visibility = View.VISIBLE
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            }

            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure you want to go to home screen?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    objectPos("unCreate")
                    resetAttributes()
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    }
                    view.findNavController().navigate(R.id.action_gameFragment_to_menuFragment)
                }
                .setNegativeButton("No") { dialog, _ ->
                    if (viewModel.pause && viewModel.gameStarted && !checkForLose()) {
                        viewModel.savePause(false)
                        binding.startText.visibility = View.INVISIBLE
                        startLoop()
                        if (!mediaPlayer.isPlaying && viewModel.gameStarted) {
                            mediaPlayer.start()
                        }
                    }
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
        binding.topAppBar!!.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.info -> {
                    if (!viewModel.pause && viewModel.gameStarted) {
                        viewModel.savePause(true)
                        viewModel.handler.removeCallbacksAndMessages(null)
                        binding.startText.text = getString(R.string.paus)
                        binding.startText.visibility = View.VISIBLE
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.pause()
                        }
                    }
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
                        if(!checkForPause() && (viewModel.laser.size < 4)){
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
        builder.setMessage(R.string.howToPlay)
            .setCancelable(true)
            .setOnDismissListener {
                if (viewModel.pause && viewModel.gameStarted && !checkForLose()) {
                    viewModel.savePause(false)
                    binding.startText.visibility = View.INVISIBLE
                    startLoop()
                    if (!mediaPlayer.isPlaying && viewModel.gameStarted) {
                        mediaPlayer.start()
                    }
                }
            }
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
    private fun removeAlienView(alienImageView: ImageView) {
        binding.gameFragment.removeView(alienImageView)
        aliens.remove(alienImageView)
    }

    // Removes asteroidView if it collides with ship or bottom of screen
    private fun removeAsteroid(asteroidImageView: ImageView) {
        binding.gameFragment.removeView(asteroidImageView)
        asteroids.remove(asteroidImageView)
    }

    // Removes laserView if it passes screenHeight
    private fun removeLaser(laserImageView: ImageView) {
        binding.gameFragment.removeView(laserImageView)
        lasers.remove(laserImageView)
    }

    // Keeps the game score
    private fun scoreCount(score: Int) {
        viewModel.incrementScore(score)
        binding.hitsText.text = String.format(getString(R.string.score) + " %d", viewModel.score)
    }
    // start game loop
    private fun startLoop() {
        if (!checkForPause() && !checkForLose()) {
            viewModel.handler.removeCallbacks(moveRunnable)
            viewModel.handler.removeCallbacks(asteroidSpawnRunnable)
            viewModel.handler.removeCallbacks(alienSpawnRunnable)

            viewModel.handler.post(moveRunnable)
            viewModel.handler.post(asteroidSpawnRunnable)
            viewModel.handler.post(alienSpawnRunnable)
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

    private fun shipHit(){
        binding.shipImageView.setImageResource(R.drawable.ship_2) // Damage flash
        viewModel.handler.postDelayed({
            binding.shipImageView.setImageResource(R.drawable.ship)
        }, 60)
        viewModel.decreaseLife()    // Decrease life data in ViewModel
        loseLife("Playing")         // Update life UI and play sound
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
        viewModel.resetGameSessionVariables()
        binding.gameFragment.invalidate()
    }

    // Load objects positions
    private fun objectPos(status: String) {

        fun reCreateAllViews(){
            binding.shipImageView.y = viewModel.shipY
            binding.shipImageView.x = viewModel.shipX

            lasers.forEach { binding.gameFragment.removeView(it)}
            lasers.clear()
            aliens.forEach { binding.gameFragment.removeView(it) }
            aliens.clear()
            asteroids.forEach { binding.gameFragment.removeView(it) }
            asteroids.clear()

            reCreateAllLasers()
            reCreateAllAliens()
            reCreateAllAsteroids()
        }

        fun unCreateAllViews(){
            lasers.forEach { binding.gameFragment.removeView(it)}
            lasers.clear()
            aliens.forEach { binding.gameFragment.removeView(it) }
            aliens.clear()
            asteroids.forEach { binding.gameFragment.removeView(it) }
            asteroids.clear()
        }

        when (status) {
            "reCreate" -> {reCreateAllViews()}
            "unCreate" -> {unCreateAllViews()}
        }
    }

    private fun reCreateAllAliens() {
        for (alienData in viewModel.alien) {
            val alienImageView = ImageView(context)
            alienImageView.setImageResource(R.drawable.alien_2)
            val layoutParams = ConstraintLayout.LayoutParams(objectData.alienWidth, objectData.alienHeight)
            alienImageView.layoutParams = layoutParams

            alienImageView.tag = alienData.id

            aliens.add(alienImageView)
            binding.gameFragment.addView(alienImageView)

            alienImageView.translationX = alienData.x
            alienImageView.translationY = alienData.y
        }
    }

    private fun reCreateAllAsteroids() {
        for (asteroidData in viewModel.asteroid) {
            val asteroidImageView = ImageView(context)
            asteroidImageView.setImageResource(R.drawable.meteor)

            val layoutParams = ConstraintLayout.LayoutParams(
                asteroidData.width,
                asteroidData.height
            )
            asteroidImageView.layoutParams = layoutParams
            asteroidImageView.tag = asteroidData.id

            asteroids.add(asteroidImageView)
            binding.gameFragment.addView(asteroidImageView)

            asteroidImageView.translationX = asteroidData.x
            asteroidImageView.translationY = asteroidData.y
        }
    }

    private fun reCreateAllLasers() {
        for (laserData in viewModel.laser) {
            val laserView = ImageView(context)
            laserView.setImageResource(R.drawable.laser)
            val layoutParams = ConstraintLayout.LayoutParams(objectData.laserWidth, objectData.laserHeight)
            laserView.layoutParams = layoutParams

            laserView.tag = laserData.id
            lasers.add(laserView)
            binding.gameFragment.addView(laserView)

            laserView.translationX = laserData.x
            laserView.translationY = laserData.y
        }
    }

    // Creates and add alienView to parent layout
    private fun createAlien() {
        val alienImageView = ImageView(context)
        alienImageView.setImageResource(R.drawable.alien_2)

        val layoutParams = ConstraintLayout.LayoutParams(
            objectData.alienWidth,
            objectData.alienHeight
        )
        alienImageView.layoutParams = layoutParams

        val newAlienData = Alien(
            x = objectData.alienX,
            y = objectData.alienY,
            deltaX = objectData.alienSpeedX,
            deltaY = objectData.alienSpeedY,
            rightBorder = objectData.rightBorder,
            leftBorder = objectData.leftBorder,
            movingRight = Random.nextBoolean()
        )

        alienImageView.tag = newAlienData.id
        aliens.add(alienImageView)
        val parentLayout = binding.gameFragment
        parentLayout.addView(alienImageView)

        alienImageView.translationX = newAlienData.x
        alienImageView.translationY = newAlienData.y

        viewModel.addAlienObject(newAlienData)
    }

    private fun spawnAlienWithDelay() {
        createAlien()

        val delay = when {
            viewModel.score < 3000 -> 8000L
            viewModel.score in 3001..5999 -> 7000L
            viewModel.score in 6000..9000 -> 6000L
            viewModel.score in 9001..12000 -> 5000L
            viewModel.score in 12001..15000 -> 4000L
            viewModel.score > 15000 -> 2500L
            else -> 8000L
        }
        if (!checkForPause() && !checkForLose()) {
            viewModel.handler.postDelayed(alienSpawnRunnable, delay)
        }
    }

    // Creates and add laserView to parent layout
    private fun createShipLaser() {
        val laserView = ImageView(context)
        laserView.setImageResource(R.drawable.laser)

        val layoutParams = ConstraintLayout.LayoutParams(
            objectData.laserWidth,
            objectData.laserHeight
        )
        laserView.layoutParams = layoutParams

        val initialLaserX = binding.shipImageView.x + (binding.shipImageView.width/2) - (objectData.laserWidth / 2)
        val initialLaserY = binding.shipImageView.y

        val newLaserData = Laser(
            x = initialLaserX,
            y = initialLaserY,
            speed = objectData.laserSpeed
        )
        laserView.tag = newLaserData.id

        lasers.add(laserView)
        val parentLayout = binding.gameFragment
        parentLayout.addView(laserView)

        laserView.translationX = newLaserData.x
        laserView.translationY = newLaserData.y

        viewModel.addLaserObject(newLaserData)
    }

    // Creates and add asteroidView to parent layout
    private fun createAsteroid(){
        val asteroidImageView = ImageView(context)
        asteroidImageView.setImageResource(R.drawable.meteor)
        val flyingObjectParams = FlyingObjectData()

        val newAsteroidData = Asteroid(
            width = flyingObjectParams.asteroidWidth,
            height = flyingObjectParams.asteroidsHeight,
            x = flyingObjectParams.asteroidX,
            y = flyingObjectParams.asteroidY,
            speed = flyingObjectParams.deltaY
        )
        asteroidImageView.tag = newAsteroidData.id

        val layoutParams = ConstraintLayout.LayoutParams(
            newAsteroidData.width,
            newAsteroidData.height
        )
        asteroidImageView.layoutParams = layoutParams

        asteroids.add(asteroidImageView)
        val parentLayout = binding.gameFragment
        parentLayout.addView(asteroidImageView)
        asteroidImageView.translationX = newAsteroidData.x
        asteroidImageView.translationY = newAsteroidData.y

        viewModel.addAsteroidObject(newAsteroidData)
    }

    private fun spawnAsteroidWithDelay() {
        createAsteroid()
        val delay = when {
            viewModel.score < 2000 -> 1500L
            viewModel.score in 2001..4999 -> 1000L
            viewModel.score in 5000..10000 -> 700L
            viewModel.score in 10001..15000 -> 500L
            viewModel.score in 15001..20000 -> 400L
            viewModel.score > 20000 -> 300L
            else -> 1500L
        }
        if (!checkForPause() && !checkForLose()) {
            viewModel.handler.postDelayed(asteroidSpawnRunnable, delay)
        }
    }
}