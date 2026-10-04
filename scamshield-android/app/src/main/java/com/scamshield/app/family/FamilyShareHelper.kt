package com.scamshield.app.family

import android.content.Context
import android.content.Intent
import android.net.Uri

object FamilyShareHelper {
    fun shareWithFamily(context: Context, url: String, verdict: String, phone: String? = null) {
        val message = "Hi! I received this link: $url\nScamShield says it might be $verdict. Can you check if it's safe?"
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        
        val chooser = Intent.createChooser(intent, "Ask Family Member")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
