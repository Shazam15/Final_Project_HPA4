package com.utp.finalproject.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.utp.finalproject.R
import com.utp.finalproject.data.local.entity.TaskEntity

object MapIntentHelper {
    fun open(context: Context, task: TaskEntity) {
        val latitude = task.latitude ?: return
        val longitude = task.longitude ?: return
        val label = Uri.encode(task.locationName ?: context.getString(R.string.location_selected))
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        val genericIntent = Intent(Intent.ACTION_VIEW, uri)
        val intent = if (mapsIntent.resolveActivity(context.packageManager) != null) {
            mapsIntent
        } else {
            genericIntent
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.no_map_application, Toast.LENGTH_SHORT).show()
        }
    }
}
