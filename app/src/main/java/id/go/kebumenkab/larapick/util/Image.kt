package id.go.kebumenkab.larapick.util

import android.content.Context
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import id.go.kebumenkab.larapick.R

fun loadImage(context: Context, imageView: ImageView, url: String?, name: String?) {
    val colorInt = ContextCompat.getColor(context, R.color.md_theme_primary)
    val colorHex = String.format("%06X", 0xFFFFFF and colorInt)

    val finalUrl = if (url.isNullOrBlank() || url == "null") {
        val baseUrl = "https://ui-avatars.com/api/"
        val lengthParam = "length=1"
        val colorParam = "color=EEEEEE"
        val backgroundParam = "background=$colorHex"
        val sizeParam = "size=128"
        val nameParam = if (!name.isNullOrBlank()) "name=${name.replace(" ", "+")}" else "name=User"
        "$baseUrl?$lengthParam&$colorParam&$backgroundParam&$sizeParam&$nameParam"
    } else {
        url
    }

    Glide.with(context)
        .load(finalUrl)
        .centerCrop()
        .into(imageView)
}

fun loadImageWithoutCenterImg(imageView: ImageView, url: String) {
    Glide.with(imageView)
        .load(url)
        .into(imageView)
}