package org.oxycblt.auxio.songs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.FragmentSongsBinding
import org.oxycblt.auxio.recycler.applyDivider

class SongsFragment : Fragment() {
    private val songsModel: SongsViewModel by lazy {
        ViewModelProvider(this).get(SongsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSongsBinding>(
            inflater, R.layout.fragment_songs, container, false
        )

        val adapter = SongDataAdapter()
        binding.songRecycler.adapter = adapter
        binding.songRecycler.applyDivider()
        binding.songRecycler.setHasFixedSize(true)

        songsModel.songs.observe(
            viewLifecycleOwner,
            Observer {
                adapter.data = it
            }
        )

        Log.d(this::class.simpleName, "Fragment created.")

        return binding.root
    }
}
