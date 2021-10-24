package com.android.w2sapp.ui.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.w2sapp.R
import com.android.w2sapp.data.models.ResourceStatus
import com.android.w2sapp.databinding.FragmentPostsBinding
import com.android.w2sapp.utils.action
import com.android.w2sapp.utils.snack
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostsDialogFragment : BottomSheetDialogFragment(), PostsListener, View.OnClickListener {


    private lateinit var binding: FragmentPostsBinding
    private lateinit var postsViewModel: PostsViewModel
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postsViewModel = ViewModelProvider(this).get(PostsViewModel::class.java)
        setRecycler()
        setObservers()
        setListeners()
//        postsViewModel.getAllLocalPosts(requireContext())
        postsViewModel.getAllPostsFromApi()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.text_cancel) {
            dismiss()
        } else if (v?.id == R.id.text_save) {

        }
    }

    override fun onPostItemChecked(adapterPos: Int) {
        postsViewModel.addToChipGroup(adapter.getData()[adapterPos])
        adapter.removeItem(adapterPos)
    }

    private fun setListeners() {
        binding.textCancel.setOnClickListener(this)
        binding.textSave.setOnClickListener(this)
    }

    private fun setRecycler() {
        adapter = PostsAdapter(this)
        binding.recycler.layoutManager = LinearLayoutManager(context)
        binding.recycler.adapter = adapter
    }

    private fun setObservers() {
        postsViewModel.postsLiveData.observe(this) { resource ->
            if (resource != null) {
                when (resource.status) {
                    ResourceStatus.LOADING -> {
                        binding.progressCircular.isVisible = true
                    }
                    ResourceStatus.SUCCESS -> {
                        binding.progressCircular.isVisible = false

                        adapter.addAll(resource.data.orEmpty())
                    }
                    ResourceStatus.EMPTY -> {
                        binding.progressCircular.isVisible = false
                    }
                    ResourceStatus.ERROR -> {
                        binding.progressCircular.isVisible = false
                        binding.root.snack(R.string.something_went_wrong) {
                            action(getString(R.string.okay), R.color.black) {
                                dismiss()
                            }
                        }
                    }
                    ResourceStatus.OFFLINE_ERROR -> {
                        binding.progressCircular.isVisible = false
                    }
                }
            }
        }

        postsViewModel.chipPostsLiveData.observe(this) { resource ->
            if (resource != null) {
                when (resource.status) {
                    ResourceStatus.SUCCESS -> {
                        binding.chipGroup.removeAllViews()
                        resource.data?.forEach { post ->
                            val chip = Chip(requireContext())
                            chip.text = post.title
                            chip.isCloseIconVisible = true
                            chip.closeIcon =
                                ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
                            chip.setOnCloseIconClickListener {
                                postsViewModel.removeFromChipGroup(post)
                                adapter.insertItem(post)
                            }
                            binding.chipGroup.addView(chip)
                        }
                    }
                    else -> {
                        binding.root.snack(R.string.something_went_wrong) {
                            action(getString(R.string.okay), R.color.black) {
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }


    companion object {
        fun newInstance() = PostsDialogFragment()

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}