package se.umu.johe6327.asteroids

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import se.umu.johe6327.asteroids.databinding.FragmentEndBinding

/**
 * Shows highScores for the current game and others played withing this playing session.
 */
class EndFragment : Fragment() {
    private val viewModel:SharedViewModel by activityViewModels()
    private var _binding: FragmentEndBinding? = null
    private val binding get() = _binding!!
    private var unSortedArray = arrayListOf<Int>()
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEndBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.topAppBar.inflateMenu(R.menu.top_app_bar)
        (activity as MainActivity?)!!.readFromFile()
        copyHighScore()
        loadHighScores()
        (activity as MainActivity?)!!.saveToFile(unSortedArray)
        stars()
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.bakgroundsound)
        mediaPlayer.isLooping = true // Set to loop the sound
        mediaPlayer.start()

        binding.topAppBar.setNavigationOnClickListener{
            // go back to start.
            viewModel.saveGameStarted(false)
            findNavController().navigate(R.id.action_endFragment_to_menuFragment)
        }
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.info -> {
                    information()
                    true
                }
                else -> false
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_endFragment_to_menuFragment)
        }
        return view
    }

    private fun stars() {
        val starAnimation = Stars(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5,
            binding.star6, binding.star7, binding.star8, binding.star9, binding.star10)
        starAnimation.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    // information
    private fun information(){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(R.string.howToRestart).setCancelable(true)
        val alert = builder.create()
        alert.show()
    }

    // Sorts top 10 scores
    private fun copyHighScore() {
        for (score in viewModel.highScores){
            unSortedArray.add(score)
        }
        unSortedArray.sortDescending()
    }

    // Load GUI with top 10 scores
    private fun loadHighScores() {
        if (unSortedArray.size == 1){
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
        }
        else if (unSortedArray.size == 2){
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
        }
        else if (unSortedArray.size == 3){
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
            binding.score3.text = String.format(getString(R.string._3) + " %d", unSortedArray[2])
        }
        else if (unSortedArray.size == 4){
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
            binding.score3.text = String.format(getString(R.string._3) + " %d", unSortedArray[2])
            binding.score4.text = String.format(getString(R.string._4) + " %d", unSortedArray[3])
        }
        else if (unSortedArray.size == 5){
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
            binding.score3.text = String.format(getString(R.string._3) + " %d", unSortedArray[2])
            binding.score4.text = String.format(getString(R.string._4) + " %d", unSortedArray[3])
            binding.score5.text = String.format(getString(R.string._5) + " %d", unSortedArray[4])
        }
        else if (unSortedArray.size == 6){
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
            binding.score3.text = String.format(getString(R.string._3) + " %d", unSortedArray[2])
            binding.score4.text = String.format(getString(R.string._4) + " %d", unSortedArray[3])
            binding.score5.text = String.format(getString(R.string._5) + " %d", unSortedArray[4])
            binding.score6.text = String.format(getString(R.string._6) + " %d", unSortedArray[5])
        }
        else if (unSortedArray.size == 7) {
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
            binding.score3.text = String.format(getString(R.string._3) + " %d", unSortedArray[2])
            binding.score4.text = String.format(getString(R.string._4) + " %d", unSortedArray[3])
            binding.score5.text = String.format(getString(R.string._5) + " %d", unSortedArray[4])
            binding.score6.text = String.format(getString(R.string._6) + " %d", unSortedArray[5])
            binding.score7.text = String.format(getString(R.string._7) + " %d", unSortedArray[6])
        }
        else if (unSortedArray.size == 8) {
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
            binding.score3.text = String.format(getString(R.string._3) + " %d", unSortedArray[2])
            binding.score4.text = String.format(getString(R.string._4) + " %d", unSortedArray[3])
            binding.score5.text = String.format(getString(R.string._5) + " %d", unSortedArray[4])
            binding.score6.text = String.format(getString(R.string._6) + " %d", unSortedArray[5])
            binding.score7.text = String.format(getString(R.string._7) + " %d", unSortedArray[6])
            binding.score8.text = String.format(getString(R.string._8) + " %d", unSortedArray[7])
        }
        else if (unSortedArray.size == 9) {
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
            binding.score3.text = String.format(getString(R.string._3) + " %d", unSortedArray[2])
            binding.score4.text = String.format(getString(R.string._4) + " %d", unSortedArray[3])
            binding.score5.text = String.format(getString(R.string._5) + " %d", unSortedArray[4])
            binding.score6.text = String.format(getString(R.string._6) + " %d", unSortedArray[5])
            binding.score7.text = String.format(getString(R.string._7) + " %d", unSortedArray[6])
            binding.score8.text = String.format(getString(R.string._8) + " %d", unSortedArray[7])
            binding.score9.text = String.format(getString(R.string._9) + " %d", unSortedArray[8])
        }
        else if (unSortedArray.size == 10 || unSortedArray.size > 10) {
            binding.score1.text = String.format(getString(R.string._1) + " %d", unSortedArray[0])
            binding.score2.text = String.format(getString(R.string._2) + " %d", unSortedArray[1])
            binding.score3.text = String.format(getString(R.string._3) + " %d", unSortedArray[2])
            binding.score4.text = String.format(getString(R.string._4) + " %d", unSortedArray[3])
            binding.score5.text = String.format(getString(R.string._5) + " %d", unSortedArray[4])
            binding.score6.text = String.format(getString(R.string._6) + " %d", unSortedArray[5])
            binding.score7.text = String.format(getString(R.string._7) + " %d", unSortedArray[6])
            binding.score8.text = String.format(getString(R.string._8) + " %d", unSortedArray[7])
            binding.score9.text = String.format(getString(R.string._9) + " %d", unSortedArray[8])
            binding.score10.text = String.format(getString(R.string._10) + " %d", unSortedArray[9])
        }
    }
}