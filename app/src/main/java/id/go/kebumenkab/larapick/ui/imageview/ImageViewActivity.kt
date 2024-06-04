package id.go.kebumenkab.larapick.ui.imageview

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.go.kebumenkab.larapick.R
import id.go.kebumenkab.larapick.databinding.ActivityImageViewBinding
import id.go.kebumenkab.larapick.util.loadImageWithoutCenterImg

class ImageViewActivity : AppCompatActivity() {
    private lateinit var imageUrl: String

    private lateinit var binding: ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolbar()
        setImage()
    }

    private fun setToolbar() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setImage() {
        imageUrl = intent.getStringExtra("imageUrl").toString()
        binding.apply {
            if (imageUrl == "null") {
                labelNoImage.visibility = View.VISIBLE
            } else {
                loadImageWithoutCenterImg(ivImage, imageUrl)
                labelNoImage.visibility = View.GONE
            }
        }
    }
}