package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.extensions.LayoutContainer
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.ArticleItemData
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.format
import kotlin.math.max

@Suppress("PropertyName")
class ArticleItemView constructor(context: Context) : ViewGroup(context), LayoutContainer {
    override val containerView = this

    val tv_date: TextView
    val tv_author: TextView
    val tv_title: TextView
    val iv_poster: ImageView
    val iv_category: ImageView
    val tv_description: TextView
    val iv_likes: ImageView
    val tv_likes_count: TextView
    val iv_comments: ImageView
    val tv_comments_count: TextView
    val tv_read_duration: TextView
    val iv_bookmark: ImageView

    val padding = context.dpToIntPx(16)
    val defaultMargin = context.dpToIntPx(8)
    val posterSize = context.dpToIntPx(64)
    val categorySize = context.dpToIntPx(40)
    val iconSize = context.dpToIntPx(16)
    val grayColor = context.getColor(R.color.color_gray)
    val primaryColor = context.attrValue(R.attr.colorPrimary)

    init {
        this.setPadding(padding)

        tv_date = TextView(context).apply {
            id = R.id.tv_date
            textSize = 12f
            setTextColor(grayColor)
        }
        addView(tv_date)

        tv_author = TextView(context).apply {
            id = R.id.tv_author
            textSize = 12f
            setTextColor(primaryColor)
        }
        addView(tv_author)

        tv_title = TextView(context).apply {
            id = R.id.tv_title
            textSize = 18f
            setTextColor(primaryColor)
            setTypeface(typeface, Typeface.BOLD)
        }
        addView(tv_title)

        iv_poster = ImageView(context).apply {
            id = R.id.iv_poster
            layoutParams = LayoutParams(posterSize, posterSize)
        }
        addView(iv_poster)

        iv_category = ImageView(context).apply {
            id = R.id.iv_category
            layoutParams = LayoutParams(categorySize, categorySize)
        }
        addView(iv_category)

        tv_description = TextView(context).apply {
            id = R.id.tv_description
            textSize = 14f
            setTextColor(grayColor)
        }
        addView(tv_description)

        iv_likes = ImageView(context).apply {
            id = R.id.tv_author
            layoutParams = LayoutParams(iconSize, iconSize)
            imageTintList = ColorStateList.valueOf(grayColor)
            setImageResource(R.drawable.ic_favorite_black_24dp)
        }
        addView(iv_likes)

        tv_likes_count = TextView(context).apply {
            textSize = 12f
            setTextColor(grayColor)
        }
        addView(tv_likes_count)

        iv_comments = ImageView(context).apply {
            layoutParams = LayoutParams(iconSize, iconSize)
            imageTintList = ColorStateList.valueOf(grayColor)
            setImageResource(R.drawable.ic_insert_comment_black_24dp)
        }
        addView(iv_comments)

        tv_comments_count = TextView(context).apply {
            textSize = 12f
            setTextColor(grayColor)
        }
        addView(tv_comments_count)

        tv_read_duration = TextView(context).apply {
            id = R.id.tv_read_duration
            textSize = 12f
            setTextColor(grayColor)
        }
        addView(tv_read_duration)

        iv_bookmark = ImageView(context).apply {
            layoutParams = LayoutParams(iconSize, iconSize)
            imageTintList = ColorStateList.valueOf(grayColor)
            setImageResource(R.drawable.bookmark_states)
        }
        addView(iv_bookmark)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var currentHeight = paddingTop
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        // date + author
        measureChild(tv_date, widthMeasureSpec, heightMeasureSpec)
        tv_author.maxWidth = width - (tv_date.measuredWidth + 3 * padding)
        measureChild(tv_author, widthMeasureSpec, heightMeasureSpec)
        currentHeight += tv_author.measuredHeight

        // title block
        val titleHeight = posterSize + categorySize / 2
        tv_title.maxWidth = width - (titleHeight + 2 * paddingLeft + defaultMargin)
        measureChild(tv_title, widthMeasureSpec, heightMeasureSpec)
        currentHeight += max(tv_title.measuredHeight, titleHeight) + 2 * defaultMargin

        // description block
        measureChild(tv_description, widthMeasureSpec, heightMeasureSpec)
        currentHeight += tv_description.measuredHeight + 2 * defaultMargin

        // icons block
        measureChild(tv_likes_count, widthMeasureSpec, heightMeasureSpec)
        measureChild(tv_comments_count, widthMeasureSpec, heightMeasureSpec)
        measureChild(tv_read_duration, widthMeasureSpec, heightMeasureSpec)

        currentHeight += iconSize + paddingBottom
        setMeasuredDimension(width, currentHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentHeight = paddingTop
        var paddingLeft = getPaddingLeft()
        val bodyWidth = right - left - paddingLeft - paddingRight
        Log.e("ArticleItemView", "bodyWidth = $bodyWidth")

        // author + date
        tv_date.layout(paddingLeft, currentHeight, paddingLeft + tv_date.measuredWidth, currentHeight + tv_date.measuredHeight)
        paddingLeft += tv_date.right + padding
        tv_author.layout(paddingLeft, currentHeight, paddingLeft + tv_author.measuredWidth, currentHeight + tv_author.measuredHeight)
        currentHeight += tv_author.measuredHeight + defaultMargin
        paddingLeft = getPaddingLeft()

        // title block
        val titleHeight = posterSize + categorySize / 2
        if (titleHeight > tv_title.measuredHeight) {
            val diffH = (titleHeight - tv_title.measuredHeight) / 2
            tv_title.layout(paddingLeft, currentHeight + diffH, paddingLeft + tv_title.measuredWidth, currentHeight + diffH + tv_title.measuredHeight)
            paddingLeft = padding
            iv_poster.layout(paddingLeft + bodyWidth - posterSize, currentHeight, paddingLeft + bodyWidth, currentHeight + posterSize)
            iv_category.layout(iv_poster.left - categorySize / 2, iv_poster.bottom - categorySize / 2, iv_poster.left + categorySize / 2, iv_poster.bottom + categorySize / 2)
            currentHeight += titleHeight
        } else {
            val diffH = (tv_title.measuredHeight - titleHeight) / 2
            tv_title.layout(left, currentHeight, left + tv_title.measuredWidth, currentHeight + tv_title.measuredHeight)
            iv_poster.layout(left + bodyWidth - posterSize, currentHeight + diffH, left + bodyWidth, currentHeight + diffH + posterSize)
            iv_category.layout(iv_poster.left - categorySize / 2, iv_poster.bottom - categorySize / 2, iv_poster.left + categorySize / 2, iv_poster.bottom + categorySize / 2)
            currentHeight += tv_title.measuredHeight
        }
        paddingLeft = padding
        currentHeight += defaultMargin

        // description
        tv_description.layout(paddingLeft, currentHeight, paddingLeft + bodyWidth, currentHeight + tv_description.measuredHeight)
        currentHeight += tv_description.measuredHeight + paddingLeft

        // icons block
        val fontDiff = iconSize - tv_likes_count.measuredHeight
        iv_likes.layout(paddingLeft, currentHeight - fontDiff, paddingLeft + iconSize, currentHeight + iconSize - fontDiff)
        paddingLeft = iv_likes.right + defaultMargin

        tv_likes_count.layout(paddingLeft, currentHeight, paddingLeft + tv_likes_count.measuredWidth, currentHeight + tv_likes_count.measuredHeight)
        paddingLeft = tv_likes_count.right + padding

        iv_comments.layout(paddingLeft, currentHeight - fontDiff, paddingLeft + iconSize, currentHeight + iconSize - fontDiff)
        paddingLeft = iv_comments.right + defaultMargin

        tv_comments_count.layout(paddingLeft, currentHeight, paddingLeft + tv_comments_count.measuredWidth, currentHeight + tv_comments_count.measuredHeight)
        paddingLeft = tv_comments_count.right + padding

        tv_read_duration.layout(paddingLeft, currentHeight, paddingLeft + tv_read_duration.measuredWidth, currentHeight + tv_read_duration.measuredHeight)
        paddingLeft = padding

        iv_bookmark.layout(paddingLeft + bodyWidth - iconSize, currentHeight - fontDiff, paddingLeft + bodyWidth, currentHeight + iconSize - fontDiff)
    }

    fun bind(item: ArticleItemData) {
        val cornerRadius = context.dpToIntPx(8)

        Glide.with(context)
            .load(item.poster)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .override(posterSize)
            .into(iv_poster)

        Glide.with(context)
            .load(item.categoryIcon)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .override(categorySize)
            .into(iv_category)

        tv_date.text = item.date.format()
        tv_author.text = item.author
        tv_title.text = item.title
        tv_description.text = item.description
        tv_likes_count.text = item.likeCount.toString()
        tv_comments_count.text = item.commentCount.toString()
        tv_read_duration.text = "${item.readDuration} min read"
    }
}