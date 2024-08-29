package com.ahmedapps.geminichatbot.all

import android.graphics.Bitmap

/**
 * @author Ahmed Guedmioui
 */
data class ChatState (
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
    val bitmap: Bitmap? = null
)