package com.kshitiz.crosswords

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class HomePageActivity : AppCompatActivity() {

    private lateinit var btnFeatured: Button
    private lateinit var btnStatistics: Button
    private lateinit var btnPacks: Button

    private lateinit var imgbtnLeftPane: ImageButton
    private lateinit var imgbtnDailyCW: ImageButton
    private lateinit var imgbtnFreePack1: ImageButton
    private lateinit var imgbtnFreePack2: ImageButton
    private lateinit var imgbtnFreePack3: ImageButton
    private lateinit var imgbtnFreePack4: ImageButton

    private lateinit var conLayoutFeatured: ConstraintLayout
    private lateinit var conLayoutStatistics: ConstraintLayout
    private lateinit var conLayoutPacks: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        try {
            declareResources()
        }
        catch(ex: Exception) {
            Log.e("homepage",  "error declaring resources")
        }
    }

    /*
        declare the buttons for attaching listeners
        declare layout for changing visibility
     */
    private fun declareResources() {
        btnFeatured = findViewById(R.id.btnHomepageFeatured)
        btnStatistics = findViewById(R.id.btnHomepageStatistics)
        btnPacks = findViewById(R.id.btnHomepagePacks)

        imgbtnLeftPane = findViewById(R.id.imgbtnHomepageLeftPane)
        imgbtnDailyCW = findViewById(R.id.imgbtnDailyCW)
        imgbtnFreePack1 = findViewById(R.id.imgbtnFreePack1)
        imgbtnFreePack2 = findViewById(R.id.imgbtnFreePack2)
        imgbtnFreePack3 = findViewById(R.id.imgbtnFreePack3)
        imgbtnFreePack4 = findViewById(R.id.imgbtnFreePack4)

        conLayoutFeatured = findViewById(R.id.constraintLayoutHomepage1)
        conLayoutPacks = findViewById(R.id.constraintLayoutHomepage2)
        conLayoutStatistics = findViewById(R.id.constraintLayoutHomepage3)

        setOnClickListeners()
    }

    /*
        set on click listeners on the buttons
     */
    private fun setOnClickListeners() {
        btnFeatured.setOnClickListener {changeVisibility("featured")}
        btnStatistics.setOnClickListener {changeVisibility("statistics")}
        btnPacks.setOnClickListener{changeVisibility("packs")}

        imgbtnLeftPane.setOnClickListener{Toast.makeText(applicationContext, "Left Pane does not exists", Toast.LENGTH_LONG).show()}

        imgbtnDailyCW.setOnClickListener{changeActivity("daily")}
        imgbtnFreePack1.setOnClickListener{changeActivity("series")}
        imgbtnFreePack2.setOnClickListener{changeActivity("series")}
        imgbtnFreePack3.setOnClickListener{changeActivity("series")}
        imgbtnFreePack4.setOnClickListener{changeActivity("series")}
    }

    /*
        change visibility of layouts based on the clicked button
     */
    private fun changeVisibility(tab: String) {
        when(tab) {
            "featured"-> {conLayoutFeatured.visibility = View.VISIBLE
                conLayoutStatistics.visibility = View.INVISIBLE
                conLayoutPacks.visibility = View.INVISIBLE
            }
            "packs"-> {conLayoutFeatured.visibility = View.INVISIBLE
                conLayoutStatistics.visibility = View.INVISIBLE
                conLayoutPacks.visibility = View.VISIBLE
            }
            "statistics"-> {conLayoutFeatured.visibility = View.INVISIBLE
                conLayoutStatistics.visibility = View.VISIBLE
                conLayoutPacks.visibility = View.INVISIBLE
            }
            else->Toast.makeText(applicationContext, "Cannot change visibility", Toast.LENGTH_LONG).show()
        }
    }

    /*
        change to another activity on button click
     */
    private fun changeActivity(targetActivity: String) {
        when(targetActivity) {
            "daily" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("pack", "daily")
                startActivity(intent)
            }
            "series" -> {
                val intent = Intent(this, CrossWordSeriesActivity::class.java)
                startActivity(intent)
            }
            else-> Toast.makeText(applicationContext, "Target activity does not exists", Toast.LENGTH_LONG).show()
        }
    }

}