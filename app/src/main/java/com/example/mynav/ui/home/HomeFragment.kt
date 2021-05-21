package com.example.mynav.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.mynav.ObjectDetection
import com.example.mynav.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val button1: Button = root.findViewById(R.id.button1)
        val fab: FloatingActionButton = root.findViewById(R.id.camera)
        fab.setOnClickListener {
            val intent = Intent(activity, ObjectDetection::class.java)
            startActivity(intent)
        }
        return root
    }
}


