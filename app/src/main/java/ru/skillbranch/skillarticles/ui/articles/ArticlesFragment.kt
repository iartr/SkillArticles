package ru.skillbranch.skillarticles.ui.articles

import android.content.res.ColorStateList
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Color
import android.os.Bundle
import android.provider.BaseColumns
import android.view.Menu
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_articles.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.base.MenuItemHolder
import ru.skillbranch.skillarticles.ui.base.ToolbarBuilder
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesState
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class ArticlesFragment : BaseFragment<ArticlesViewModel>() {

    override val viewModel: ArticlesViewModel by activityViewModels()
    override val layout: Int = R.layout.fragment_articles
    override val binding: ArticlesBinding by lazy { ArticlesBinding() }
    private val args: ArticlesFragmentArgs by navArgs()

    override val prepareToolbar: (ToolbarBuilder.() -> Unit) = {
        addMenuItem(
            MenuItemHolder(
                "Search",
                R.id.action_search,
                R.drawable.ic_search_black_24dp,
                R.layout.search_view_layout
            )
        )

        addMenuItem(
            MenuItemHolder(
                "Filter",
                R.id.action_filter,
                R.drawable.ic_filter,
                null
            ) {
                val action = ArticlesFragmentDirections.actionChoseCategory(
                    binding.categories.toTypedArray(),
                    binding.selectedCategories.toTypedArray()
                )
                viewModel.navigate(NavigationCommand.To(action.actionId, action.arguments))
            }
        )
    }

    private lateinit var suggestionsAdapter: SimpleCursorAdapter

    private val articlesAdapter = ArticlesAdapter { item, isToggleBookmark ->
        if (isToggleBookmark) {
            viewModel.handleToggleBookmark(item.id)
        } else {
            val direction = ArticlesFragmentDirections.actionToPageArticle(
                item.id,
                item.author,
                item.authorAvatar!!,
                item.category,
                item.categoryIcon,
                item.poster,
                item.title,
                item.date
            )

            viewModel.navigate(NavigationCommand.To(direction.actionId, direction.arguments))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        suggestionsAdapter = SimpleCursorAdapter(
            context,
            android.R.layout.simple_list_item_1,
            null, // Cursor
            arrayOf("tag"), // Cursor column for bind on view,
            intArrayOf(android.R.id.text1), // id for bind
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        suggestionsAdapter.setFilterQueryProvider { constraint -> populateAdapter(constraint) }

        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        if (binding.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(binding.searchQuery, false)
        }

        val autoTv = searchView.findViewById<AutoCompleteTextView>(R.id.search_src_text)
        autoTv.threshold = 1

        searchView.suggestionsAdapter = suggestionsAdapter
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int) = false

            override fun onSuggestionClick(position: Int): Boolean {
                suggestionsAdapter.cursor.moveToPosition(position)
                val query = suggestionsAdapter.cursor.getString(1)
                searchView.setQuery(query, true)
                viewModel.handleSuggestion(query)

                return false
            }
        })

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false) // or true?
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

        searchView.setOnCloseListener {
            viewModel.handleSearchMode(false)
            true
        }
    }

    override fun setupViews() {
        with(rv_articles) {
            adapter = articlesAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        viewModel.observeList(viewLifecycleOwner, args.isBookmarks) { data ->
            articlesAdapter.submitList(data)
        }

        viewModel.observeTags(viewLifecycleOwner) { tags ->
            binding.tags = tags
        }

        viewModel.observeCategories(viewLifecycleOwner) { categories ->
            binding.categories = categories
        }
    }

    private fun populateAdapter(constraint: CharSequence?): Cursor {
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, "tag")) // create cursor for table with 2 colums _id, tag
        constraint ?: return cursor

        val currentCursor = suggestionsAdapter.cursor
        currentCursor.moveToFirst()

        for (i in 0 until currentCursor.count) {
            val tagValue = currentCursor.getString(1)
            if (tagValue.contains(constraint, true)) cursor.addRow(arrayOf<Any>(i, tagValue))
            currentCursor.moveToNext()
        }
        return cursor
    }

    inner class ArticlesBinding : Binding() {
        var searchQuery: String? = null
        var isSearch = false

        var isLoading: Boolean by RenderProp(true) {
            // TODO: Show shimmer on rv_list
        }

        var isHashtagSearch: Boolean by RenderProp(false)
        var tags: List<String> by RenderProp(emptyList())

        var categories: List<CategoryData> = emptyList()
        var selectedCategories: List<String> by RenderProp(emptyList()) {
            var drawable = toolbar.menu?.findItem(R.id.action_filter)?.icon ?: return@RenderProp
            drawable = DrawableCompat.wrap(drawable)

            if (it.isNotEmpty()) DrawableCompat.setTint(drawable, resources.getColor(R.color.color_accent, null))
            else DrawableCompat.setTint(drawable, resources.getColor(R.color.color_on_article_bar, null))

            toolbar.menu.findItem(R.id.action_filter).icon = drawable
        }

        override fun bind(data: IViewModelState) {
            data as ArticlesState
            isSearch = data.isSearch
            searchQuery = data.searchQuery
            isLoading = data.isLoading
            isHashtagSearch = data.isHashtagSearch
            selectedCategories = data.selectedCategories
        }

        override var afterInflated: (() -> Unit)? = {
            dependsOn<Boolean, List<String>>(::isHashtagSearch, ::tags) { isHashtagSearch, tags ->
                val cursor = MatrixCursor(arrayOf(BaseColumns._ID, "tag"))

                if (isHashtagSearch && tags.isNotEmpty()) {
                    for ((counter, tag) in tags.withIndex()) {
                        cursor.addRow(arrayOf(counter, tag))
                    }
                }

                suggestionsAdapter.changeCursor(cursor)
            }
        }
    }
}
