package com.utp.finalproject

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var hiddenPanel: ViewGroup
    private lateinit var sliderButton: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var closeButton: Button

    private var isPanelShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        hiddenPanel = findViewById(R.id.hidden_panel)
        sliderButton = findViewById(R.id.button3)
        recyclerView = findViewById(R.id.recyclerView)
        closeButton = findViewById(R.id.closeButton)

        hiddenPanel.visibility = View.GONE

        // Lista de ejemplo
        val items = listOf(
            "Elemento 1",
            "Elemento 2",
            "Elemento 3",
            "Elemento 4",
            "Elemento 5",
            "Elemento 6",
            "Elemento 7",
            "Elemento 8",
            "Elemento 9",
            "Elemento 10"
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MenuAdapter(items)

        sliderButton.setOnClickListener {

            if (!isPanelShown) {

                val slideUp = AnimationUtils.loadAnimation(
                    this,
                    R.anim.slide_up
                )

                hiddenPanel.visibility = View.VISIBLE
                hiddenPanel.startAnimation(slideUp)

                isPanelShown = true

            } else {

                closePanel()
            }
        }
        closeButton.setOnClickListener {

            closePanel()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun closePanel() {

        val slideDown = AnimationUtils.loadAnimation(
            this,
            R.anim.slide_down
        )

        hiddenPanel.startAnimation(slideDown)

        slideDown.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                hiddenPanel.visibility = View.GONE
            }
        })

        isPanelShown = false
    }
}

