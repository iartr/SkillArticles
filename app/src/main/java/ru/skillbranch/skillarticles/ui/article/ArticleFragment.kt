package ru.skillbranch.skillarticles.ui.article

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions.circleCropTransform
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_article.*
import kotlinx.android.synthetic.main.layout_bottombar.view.*
import kotlinx.android.synthetic.main.layout_submenu.view.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.repositories.Element
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.*
import ru.skillbranch.skillarticles.ui.base.*
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.Bottombar
import ru.skillbranch.skillarticles.ui.custom.ShimmerDrawable
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownBuilder
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.article.ArticleState
import ru.skillbranch.skillarticles.viewmodels.article.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory

class ArticleFragment : BaseFragment<ArticleViewModel>(), IArticleView {
    private val args: ArticleFragmentArgs by navArgs()

    override val viewModel: ArticleViewModel by viewModels {
        ViewModelFactory(
            owner = this,
            params = args.articleId
        )
    }

    override val layout: Int = R.layout.fragment_article
    override val binding: ArticleBinding by lazy { ArticleBinding() }

    override val prepareToolbar: (ToolbarBuilder.() -> Unit) = {
        setSubtitle(args.category)
        setLogo(args.categoryIcon)
        addMenuItem(
            MenuItemHolder(
                title = "Search",
                menuId = R.id.action_search,
                icon = R.drawable.ic_search_black_24dp,
                actionViewLayout = R.layout.search_view_layout
            )
        )
    }

    override val prepareBottombar: (BottombarBuilder.() -> Unit) = {
        this.addView(R.layout.layout_submenu)
            .addView(R.layout.layout_bottombar)
            .setVisibility(false)
    }

    private val bottombar
        get() = root.findViewById<Bottombar>(R.id.bottombar)

    private val submenu
        get() = root.findViewById<ArticleSubmenu>(R.id.submenu)

    private val commentsAdapter by lazy {
        CommentsAdapter {
            viewModel.handleReplyTo(it.slug, it.user.name)
            et_comment.requestFocus()
            scroll.smoothScrollTo(0, wrap_comments.top)
            et_comment.context.showKeyboard(et_comment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun setupViews() {
        root.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setupBottombar()
        setupSubmenu()

        val avatarSize = root.dpToIntPx(40)
        val cornerRadius = root.dpToIntPx(8)

        val baseColor = root.getColor(R.color.color_gray_light)
        val posterWidth = resources.displayMetrics.widthPixels - root.dpToIntPx(32)
        val posterHeight = (posterWidth / 16f * 9).toInt()
        val highlightColor = requireContext().getColor(R.color.color_divider)

        val avatarShimmer = ShimmerDrawable.Builder()
            .setBaseColor(baseColor)
            .setHighlightColor(highlightColor)
            .setShimmerWidth(posterWidth)
            .addShape(ShimmerDrawable.Shape.Round(avatarSize))
            .build()
            .apply { start() }

        val posterShimmer = ShimmerDrawable.Builder()
            .setBaseColor(baseColor)
            .setHighlightColor(highlightColor)
            .addShape(
                ShimmerDrawable.Shape.Rectangle(
                    width = posterWidth,
                    height = posterHeight,
                    cornerRadius = cornerRadius
                )
            )
            .build()
            .apply { start() }

        Glide.with(root)
            .load(args.authorAvatar)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    avatarShimmer.stop()
                    return false
                }
            })
            .placeholder(avatarShimmer)
            .apply(circleCropTransform())
            .override(avatarSize)
            .into(iv_author_avatar)

        Glide.with(root)
            .load(args.poster)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    posterShimmer.stop()
                    return false
                }
            })
            .placeholder(posterShimmer)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .into(iv_poster)

        tv_title.text = args.title
        tv_author.text = args.author
        tv_date.text = args.date.format()

        et_comment.setOnEditorActionListener { view, _, _ ->
            root.hideKeyboard()

            viewModel.handleSendComment(view.text.toString())

            if (viewModel.currentState.isAuth) {
                view.text = null
                view.clearFocus()
            }

            true
        }

        et_comment.setOnFocusChangeListener { _, hasFocus ->
            viewModel.handleCommentFocus(hasFocus)
        }

        wrap_comments.setEndIconOnClickListener {
            requireActivity().hideKeyboard()
            viewModel.handleClearComment()
            et_comment.text = null
            et_comment.clearFocus()
        }

        with(rv_comments) {
            layoutManager = LinearLayoutManager(context)
            adapter = commentsAdapter
        }

        viewModel.observeList(viewLifecycleOwner) {
            commentsAdapter.submitList(it)
        }
    }

    override fun showSearchBar() {
        bottombar.setSearchState(true)
        scroll.setMarginOptionally(bottom = root.dpToIntPx(56))
    }

    override fun hideSearchBar() {
        bottombar.setSearchState(false)
        scroll.setMarginOptionally(bottom = 0)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = getString(R.string.article_search_placeholder)

        if (binding.isSearching) {
            menuItem.expandActionView()
            searchView.setQuery(binding.searchQuery, false)
            if (binding.isFocusedSearch) searchView.requestFocus() else searchView.clearFocus()
        }

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })
    }

    override fun onDestroyView() {
        root.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        super.onDestroyView()
    }

    private fun setupSubmenu() {
        submenu.btn_text_up.setOnClickListener { viewModel.handleUpText() }
        submenu.btn_text_down.setOnClickListener { viewModel.handleDownText() }
        submenu.switch_mode.setOnClickListener { viewModel.handleNightMode() }
    }

    private fun setupBottombar() {
        bottombar.btn_like.setOnClickListener { viewModel.handleLike() }
        bottombar.btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        bottombar.btn_share.setOnClickListener { viewModel.handleShare() }
        bottombar.btn_settings.setOnClickListener { viewModel.handleToggleMenu() }

        bottombar.btn_result_up.setOnClickListener {
            if (!tv_text_content.hasFocus()) tv_text_content.requestFocus()
            root.hideKeyboard()
            viewModel.handleUpResult()
        }

        bottombar.btn_result_down.setOnClickListener {
            if (!tv_text_content.hasFocus()) tv_text_content.requestFocus()
            root.hideKeyboard()
            viewModel.handleDownResult()
        }

        bottombar.btn_search_close.setOnClickListener {
            viewModel.handleSearchMode(false)
            root.invalidateOptionsMenu()
        }
    }

    private fun setupCopyListener() {
        tv_text_content.setCopyListener { copy ->
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied code", copy)
            clipboard.setPrimaryClip(clip)
            viewModel.handleCopyCode()
        }
    }

    inner class ArticleBinding : Binding() {
        private val mb = MarkdownBuilder(requireContext())

        var isFocusedSearch = false
        var isSearching = false
        var searchQuery: String? = null

        private var isLoadingContent: Boolean by RenderProp(true) {
            tv_text_content.isLoading = it
        }

        private var isLike: Boolean by RenderProp(false) { bottombar.btn_like.isChecked = it }
        private var isBookmark: Boolean by RenderProp(false) { bottombar.btn_bookmark.isChecked = it }
        private var isShowMenu: Boolean by RenderProp(false) {
            bottombar.btn_settings.isChecked = it
            if (it) submenu.open() else submenu.close()
        }

        private var isBigText: Boolean by RenderProp(false) {
            if (it) {
                tv_text_content.textSize = 18f
                submenu.btn_text_up.isChecked = true
                submenu.btn_text_down.isChecked = false
            } else {
                tv_text_content.textSize = 14f
                submenu.btn_text_up.isChecked = false
                submenu.btn_text_down.isChecked = true
            }
        }

        private var isDarkMode: Boolean by RenderProp(false, needInit = false) {
            submenu.switch_mode.isChecked = it
            root.delegate.localNightMode = if (it) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        }

        private var isSearch: Boolean by RenderProp(false) {
            if (it) {
                showSearchBar()
                with(toolbar) {
                    (layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                        AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
                }
            } else {
                hideSearchBar()
                with(toolbar) {
                    (layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                        AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
                }
            }
        }

        private var searchResults: List<Pair<Int, Int>> by RenderProp(emptyList())
        private var searchPosition: Int by RenderProp(0)

        private var content: List<MarkdownElement> by RenderProp(emptyList()) {
            tv_text_content.setContent(it)
            if (it.isNotEmpty()) setupCopyListener()
        }

        private var answerTo by RenderProp("Comment") { wrap_comments.hint = it }
        private var isShowBottombar by RenderProp(true) {
            if (it) bottombar.show() else bottombar.hide()
            if (submenu.isOpen) submenu.isVisible = it
        }

        private var comment by RenderProp("") {
            et_comment.setText(it)
        }

        private var hashtags: List<String> by RenderProp(emptyList()) {
            val tags = buildSpannedString {
                it.forEach { tag ->
                    mb.buildElement(Element.InlineCode(tag), this)
                    append(" ")
                }
            }
            tv_hashtags.setText(tags, TextView.BufferType.SPANNABLE)
        }

        private var source: String by RenderProp("") { link ->
            val s = buildSpannedString {
                mb.buildElement(Element.Link(link, "Article source"), this)
            }
            tv_source.setText(s, TextView.BufferType.SPANNABLE)
        }

        override var afterInflated: (() -> Unit)? = {
            dependsOn<Boolean, Boolean, List<Pair<Int, Int>>, Int>(
                ::isLoadingContent,
                ::isSearch,
                ::searchResults,
                ::searchPosition
            ) { isLoadingContent, isSearch, searchResults, searchPosition ->
                if (!isLoadingContent && isSearch) {
                    tv_text_content.renderSearchResult(searchResults)
                    tv_text_content.renderSearchPosition(searchResults.getOrNull(searchPosition))
                }
                if (!isLoadingContent && !isSearch) {
                    tv_text_content.clearSearchResult()
                }

                bottombar.bindSearchInfo(searchResults.size, searchPosition)
            }
        }

        // It is Observed by viewModel
        override fun bind(data: IViewModelState) {
            data as ArticleState
            isLike = data.isLike
            isBookmark = data.isBookmark
            isShowMenu = data.isShowMenu
            isBigText = data.isBigText

            content = data.content

            isDarkMode = data.isDarkMode

            isLoadingContent = data.isLoadingContent
            isSearch = data.isSearch
            isSearching = data.isSearch
            searchQuery = data.searchQuery
            searchPosition = data.searchPosition
            searchResults = data.searchResults
            answerTo = data.answerTo ?: "Comment"
            isShowBottombar = data.showBottomBar
            comment = data.comment ?: ""
            hashtags = data.hashtags
            if (data.source != null) source = data.source
        }

        // It is called in fragment.onSaveInstanceState()
        override fun saveUi(outState: Bundle) {
            outState.putBoolean(::isFocusedSearch.name, search_view?.hasFocus() ?: false)
        }

        // It is called in fragment.onViewCreated
        override fun restoreUi(savedState: Bundle?) {
            isFocusedSearch = savedState?.getBoolean(::isFocusedSearch.name) ?: false
        }
    }
}
