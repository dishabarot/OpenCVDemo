package com.example.opencvdemo

import android.graphics.Bitmap
import java.io.Serializable

class ImageModel : Serializable {
    var bitmap: Bitmap
        get() {
            return bitmap
        }
        set(value) {
            bitmap = value
        }
}