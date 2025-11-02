package com.zulian.ayudafinanzas

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullScreenImageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageView: ImageView = findViewById(R.id.fullScreenImageView)
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
        }
    }
}
