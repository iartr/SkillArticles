package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
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
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {

    private lateinit var viewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) { articleState -> renderUi(articleState) }
        viewModel.observeNotifications(this) { renderNotifications(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.article_menu, menu)

        val content = viewModel.currentState
        val actionSearch = menu.findItem(R.id.action_search)
        val searchView = actionSearch.actionView as SearchView
        searchView.inputType = InputType.TYPE_CLASS_TEXT
        searchView.queryHint = getString(R.string.search)

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

        actionSearch.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })

        if (content.isSearch) {
            actionSearch.expandActionView()
            searchView.requestFocus()
            searchView.setQuery(content.searchQuery ?: "", false)
        }

        return super.onCreateOptionsMenu(menu)
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
    }
}
