package com.example.vkproducts.ui.share

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.vkproducts.R
import com.example.vkproducts.ui.share.logic.VKWallPostCommand
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import kotlinx.android.synthetic.main.bottom_sheet_layout_wall.view.*

class ShareBottomSheetDialog(private val uri: Uri, private val bitmap: Bitmap) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_layout_wall, container, false)
    }

    private fun adjustKeyboard() =
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(view) {
        adjustKeyboard()
        makeDialogFullyElapsed(view)

        Glide.with(this)
            .load(bitmap)
            .into(ivPhoto)

        btnSend.setOnClickListener {
            sharePost(view.etComment.text.toString(), view)
            btnSend.text = getString(R.string.loading)
            btnSend.isClickable = false
            showToast("sending")
        }
        btnClose.setOnClickListener { dismiss() }
    }

    private fun sharePost(message: String, view: View) {
        val photos = listOf(uri)
        VK.execute(VKWallPostCommand(message, photos), object : VKApiCallback<Int> {
            override fun success(result: Int) {
                showToast("Posting is successful!")
                dismiss()
            }

            override fun fail(error: Exception) {
                showToast(getString(R.string.something_went_wrong))
                println("erorr : ${error.message}")
                view.btnSend.text = getString(R.string.send)
            }
        })
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

    private fun showToast(message: String) {
        context?.let { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }
}