package com.example.opencvdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable

class ResultActivity : AppCompatActivity() {
    var imageview : ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        imageview = findViewById(R.id.result_image)
        val imageModel = intent.getSerializableExtra("imageModel") as ImageModel
        val bitmap = imageModel.bitmap
        imageview!!.setImageBitmap(bitmap)
    }
}