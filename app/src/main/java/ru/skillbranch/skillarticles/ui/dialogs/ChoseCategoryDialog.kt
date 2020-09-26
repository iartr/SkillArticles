package ru.skillbranch.skillarticles.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import ru.skillbranch.skillarticles.R

class ChoseCategoryDialog : DialogFragment() {
    companion object {
        const val CHOOSE_CATEGORY_KEY = "CHOOSE_CATEGORY_KEY"
        const val SELECTED_CATEGORIES = "SELECTED_CATEGORIES"
    }

    private val selectedCategories = mutableListOf<String>()
    private val args: ChoseCategoryDialogArgs by navArgs()
    private val categoryAdapter = CategoryAdapter { categoryId: String, isChecked: Boolean ->
        toggleCategory(categoryId, isChecked)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val categories = args.categories.toList()
        selectedCategories.clear()
        selectedCategories.addAll(savedInstanceState?.getStringArray("checked") ?: args.selectedCategories)
        val categoryItems = categories.map { it.toCategoryDataItem(selectedCategories.contains(it.categoryId)) }

        categoryAdapter.submitList(categoryItems)

        val listView = layoutInflater.inflate(R.layout.fragment_choose_category_dialog, null) as RecyclerView
        listView.adapter = categoryAdapter

        return AlertDialog.Builder(requireContext())
            .setView(listView)
            .setTitle("Chose category")
            .setPositiveButton("Apply") { _, _ ->
                setFragmentResult(CHOOSE_CATEGORY_KEY, bundleOf(SELECTED_CATEGORIES to selectedCategories))
            }
            .setNegativeButton("Reset") { _, _ ->
                setFragmentResult(CHOOSE_CATEGORY_KEY, bundleOf(SELECTED_CATEGORIES to emptyList<String>()))
            }
            .create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray("checked", selectedCategories.toTypedArray())
        super.onSaveInstanceState(outState)
    }

    private fun toggleCategory(categoryId: String, isChecked: Boolean) {
        if (isChecked) selectedCategories.add(categoryId)
        else selectedCategories.remove(categoryId)
        val categories = args.categories.toList()
        val categoryItems = categories.map { it.toCategoryDataItem(selectedCategories.contains(it.categoryId)) }
        categoryAdapter.submitList(categoryItems)
    }

}