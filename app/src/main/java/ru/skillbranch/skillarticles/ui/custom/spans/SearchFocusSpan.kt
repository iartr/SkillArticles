package ru.skillbranch.skillarticles.ui.custom.spans

import android.text.TextPaint

class SearchFocusSpan(private val bgColor: Int, private val fgColor:Int) : SearchSpan(bgColor, fgColor) {

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.bgColor = bgColor
        textPaint.color = fgColor
    }
}