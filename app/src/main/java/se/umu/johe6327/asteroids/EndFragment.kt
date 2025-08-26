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
    private val viewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentEndBinding? = null
    private val binding get() = _binding!!
    private var displayedScores = arrayListOf<Int>()
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEndBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.topAppBar.inflateMenu(R.menu.top_app_bar)
        populateAndDisplayHighScoresFromViewModel()
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
                    showInformationDialog()
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

    private fun populateAndDisplayHighScoresFromViewModel() {
        displayedScores.clear()
        displayedScores.addAll(viewModel.highScores)
        uppdateHighScoreTextViews()
    }

    private fun uppdateHighScoreTextViews() {
        val scoreTextViews = listOf(
            binding.score1, binding.score2, binding.score3, binding.score4, binding.score5,
            binding.score6, binding.score7, binding.score8, binding.score9, binding.score10
        )
        scoreTextViews.forEach { it.text = "" }
        for (i in 0 until minOf(displayedScores.size, scoreTextViews.size)) {
            scoreTextViews[i].text = "${i + 1}. ${displayedScores[i]}"
        }
    }

    private fun showInformationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.howToRestart).setCancelable(true)
        val alert = builder.create()
        alert.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        _binding = null
    }
}