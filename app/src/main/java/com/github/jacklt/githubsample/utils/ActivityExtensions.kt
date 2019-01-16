package com.github.jacklt.githubsample.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

fun Context.openUrlInCustomTab(url: String) {
    try {
        CustomTabsIntent.Builder()
            .addDefaultShareMenuItem()
            .setShowTitle(true)
            .setInstantAppsEnabled(true)
            .build()
            .launchUrl(this, Uri.parse(url))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "Cannot open: $url", Toast.LENGTH_SHORT).show()
    }
}
