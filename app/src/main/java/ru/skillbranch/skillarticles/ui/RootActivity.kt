package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory

class RootActivity : BaseActivity<ArticleViewModel>(), IArticleView {

    override val layout: Int = R.layout.activity_root
    override lateinit var viewModel: ArticleViewModel

    private var isSearching = false
    private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) { articleState -> renderUi(articleState) }
        viewModel.observeNotifications(this) { renderNotifications(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = getString(R.string.article_search_placeholder)

        if (isSearching) {
            menuItem.expandActionView()
            searchView.setQuery(searchQuery, false)
            searchView.clearFocus()
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

        return super.onCreateOptionsMenu(menu)
    }

    override fun setupViews() {
        setupToolbar()
        setupBottombar()
        setupSubmenu()
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun renderSearchPosition(searchPosition: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearSearchResult() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showSearchBar() {
        bottombar.setSearchState(true)
        scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        bottombar.setSearchState(false)
        scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }

    // Как только на Article что-то изменится, тут же будет обработан этот метод
    // Потому что мы отсюда подписались на изменения

    /**
     * Пример обработки:
     * btn_like.click() клик на эту кнопку -> viewModel.handleLike() -> изменяется значение в Model (сейчас - DataHolder) ->
     -> поскольку во ViewModel мы подписались на изменения Model, то там мы получаем уведомление об изменении ->
     получили изменения во ViewModel -> Она оповестила View (этот класс)
     * */
    private fun renderUi(articleState: ArticleState) {
        if (articleState.isSearch) showSearchBar() else hideSearchBar()

        // bind submenu state
        btn_settings.isChecked = articleState.isShowMenu
        if (articleState.isShowMenu) submenu.open() else submenu.close()

        // bind article person data
        btn_like.isChecked = articleState.isLike
        btn_bookmark.isChecked = articleState.isBookmark

        // bind submenu views
        switch_mode.isChecked = articleState.isDarkMode
        delegate.localNightMode = if (articleState.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (articleState.isBigText) {
            tv_text_container.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        } else {
            tv_text_container.textSize = 14f
            btn_text_up.isChecked = false
            btn_text_down.isChecked = true
        }

        // bind content
        tv_text_container.text = if (articleState.isLoadingContent) "loading" else articleState.content.first() as String

        //bind toolbar
        toolbar.title = articleState.title ?: "loading"
        toolbar.subtitle = articleState.category ?: "loading"
        if (articleState.categoryIcon != null) toolbar.logo = getDrawable(articleState.categoryIcon as Int)
    }

    private fun renderNotifications(notify: Notify) {
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)
            .setActionTextColor(getColor(R.color.color_accent_dark))

        when (notify) {
            is Notify.TextMessage -> { }

            is Notify.ActionMessage -> {
                snackbar.setAction(notify.actionLabel) { notify.actionHandler.invoke() }
            }

            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) { notify.errHandler?.invoke() }
                }
            }
        }

        snackbar.show()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Задаем для логотипа некоторые надстройки
        val logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        (logo?.layoutParams as? Toolbar.LayoutParams)?.let {
            it.width = dpToIntPx(40)
            it.height = dpToIntPx(40)
            it.marginEnd = dpToIntPx(10)
            logo.layoutParams = it
        }
    }

    // Activity/Fragment получает информацию о том, что было совершено действие
    // А затем поручает обработку ViewModel
    private fun setupSubmenu() {
        btn_text_up.setOnClickListener { viewModel.handleUpText() }
        btn_text_down.setOnClickListener { viewModel.handleDownText() }
        switch_mode.setOnClickListener { viewModel.handleNightMode() }
    }

    // Activity/Fragment получает информацию о том, что было совершено действие
    // А затем поручает обработку ViewModel
    private fun setupBottombar() {
        btn_like.setOnClickListener { viewModel.handleLike() }
        btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        btn_share.setOnClickListener { viewModel.handleShare() }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }

        btn_result_up.setOnClickListener {
            if (search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleUpResult()
        }

        btn_result_down.setOnClickListener {
            if (search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleDownResult()
        }

        btn_search_close.setOnClickListener {
            viewModel.handleSearchMode(false)
            invalidateOptionsMenu()
        }
    }
}
