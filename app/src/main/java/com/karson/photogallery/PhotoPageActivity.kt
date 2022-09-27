package com.karson.photogallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.karson.photogallery.databinding.ActivityPhotoPageBinding

class PhotoPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fm = supportFragmentManager
        val currentFragment = fm.findFragmentById(R.id.container)
        if (currentFragment == null) {
            val fragment = PhotoPageFragment.newInstance(intent.data ?: Uri.EMPTY)
            fm.beginTransaction().add(R.id.container, fragment)
                .commit()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {

        fun newIntent(context: Context, photoPageUri: Uri): Intent {
            return Intent(context, PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }
}