package ru.skillbranch.skillarticles.ui.bookmarks

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bookmarks.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.articles.ArticlesAdapter
import ru.skillbranch.skillarticles.ui.articles.ArticlesFragmentDirections
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.base.MenuItemHolder
import ru.skillbranch.skillarticles.ui.base.ToolbarBuilder
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.bookmarks.BookmarksState
import ru.skillbranch.skillarticles.viewmodels.bookmarks.BookmarksViewModel

class BookmarksFragment : BaseFragment<BookmarksViewModel>() {
    override val viewModel: BookmarksViewModel by viewModels()
    override val layout: Int = R.layout.fragment_bookmarks

    override val prepareToolbar: (ToolbarBuilder.() -> Unit) = {
        /*addMenuItem(
            MenuItemHolder(
                "Search",
                R.id.action_search,
                R.drawable.ic_search_black_24dp,
                R.layout.search_view_layout
            )
        )*/
    }

    private val articlesAdapter = ArticlesAdapter(
        listener = { item ->
            val direction = ArticlesFragmentDirections.actionNavArticlesToPageArticle(
                item.id,
                item.author,
                item.authorAvatar,
                item.category,
                item.categoryIcon,
                item.poster,
                item.title,
                item.date
            )

            viewModel.navigate(NavigationCommand.To(direction.actionId, direction.arguments))
        },
        bookmarkListener = { id, checked ->
            viewModel.handleToggleBookmark(id, checked)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun setupViews() {
        with(rv_bookmarks) {
            adapter = articlesAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        viewModel.observeList(viewLifecycleOwner) { data ->
            articlesAdapter.submitList(data)
        }
    }

    inner class BookmarksBinding : Binding() {
//        var isFocusedSearch = false
        var searchQuery: String? = null
        var isSearch = false

        var isLoading: Boolean by RenderProp(true) {
            // TODO: Show shimmer on rv_list
        }

        override fun bind(data: IViewModelState) {
            data as BookmarksState
            isSearch = data.isSearch
            searchQuery = data.searchQuery
            isLoading = data.isLoading
        }

        override fun saveUi(outState: Bundle) {
            outState.putBoolean("isSearch", isSearch)
            outState.putString("searchQuery", searchQuery)
        }

        override fun restoreUi(savedState: Bundle?) {
            isSearch = savedState?.getBoolean("isSearch") ?: false
            searchQuery = savedState?.getString("searchQuery")
        }
    }
}
