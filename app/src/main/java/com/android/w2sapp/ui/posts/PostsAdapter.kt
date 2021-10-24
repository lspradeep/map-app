package com.android.w2sapp.ui.posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.w2sapp.data.models.post.Post
import com.android.w2sapp.databinding.ItemPostBinding

class PostsAdapter(private val listener: PostsListener) :
    RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    private val adapterData = mutableListOf<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {

        return PostViewHolder(
            ItemPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )

    }

    override fun onBindViewHolder(holderPost: PostViewHolder, position: Int) {
        holderPost.bind(adapterData[position])
    }

    override fun getItemCount(): Int {
        return adapterData.size
    }

    fun getData(): List<Post> {
        return adapterData
    }

    fun insertItem(post: Post) {
        adapterData.add(0, post)
        notifyItemInserted(0)
    }

    fun removeItem(indexToUpdate: Int) {
        adapterData.removeAt(indexToUpdate)
        notifyItemRemoved(indexToUpdate)
    }

    fun addAll(data: List<Post>) {
        adapterData.addAll(data)
        notifyItemRangeInserted(0, (data.size - 1))
    }

    inner class PostViewHolder(private val itemBinding: ItemPostBinding, private val listener: PostsListener) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    itemBinding.checkbox.isChecked = false
                    listener.onPostItemChecked(
                        absoluteAdapterPosition,
                    )
                }
            }
        }

        fun bind(post: Post) {
            itemBinding.checkbox.text = post.title
        }
    }
}