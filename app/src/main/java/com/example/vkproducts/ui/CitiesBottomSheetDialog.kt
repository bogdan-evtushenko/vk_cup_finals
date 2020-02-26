package com.example.vkproducts.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkproducts.R
import com.example.vkproducts.logic.CitiesListItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*

class CitiesBottomSheetDialog(
    private val onCancelListener: (CitiesListItem) -> Unit,
    private val cities: List<CitiesListItem>,
    private var currentSelectingItem: CitiesListItem
) : BottomSheetDialogFragment() {

    private val citiesAdapter by lazy {
        CitiesAdapter(
            context!!,
            ::adjustClickingOnCity,
            cities,
            cities.indexOf(currentSelectingItem)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_layout, container, false)

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCancelListener(currentSelectingItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(view) {
        makeDialogFullyElapsed(view)

        btnClose.setOnClickListener {
            onCancelListener(currentSelectingItem)
            dismiss()
        }

        recyclerView.run {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = citiesAdapter
        }
    }

    private fun makeDialogFullyElapsed(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val bottomSheet =
                    (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                BottomSheetBehavior.from(bottomSheet!!).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    peekHeight = 0
                }
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun adjustClickingOnCity(cityItem: CitiesListItem) {
        currentSelectingItem = cityItem
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}