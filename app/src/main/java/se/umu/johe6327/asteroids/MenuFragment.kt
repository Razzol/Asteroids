package se.umu.johe6327.asteroids

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import se.umu.johe6327.asteroids.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.topAppBar.inflateMenu(R.menu.top_app_bar)

        stars()
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.bakgroundsound)
        mediaPlayer.isLooping = true // Set to loop the sound
        mediaPlayer.start()
        binding.startButton.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_gameFragment)
        }

        binding.ScoreButton.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_endFragment)
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
            context?.let {
                MaterialAlertDialogBuilder(it)
                    .setTitle("Quit Game")
                    .setMessage("Are you sure you want to quit?")
                    .setPositiveButton("Yes") { _, _ -> requireActivity().finish() }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
        return view
    }

    private fun stars() {
        val starAnimation = Stars(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5,
            binding.star6, binding.star7, binding.star8, binding.star9, binding.star10)
        starAnimation.start()
    }

    // information
    private fun information(){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(R.string.info_start).setCancelable(true)
        val alert = builder.create()
        alert.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}