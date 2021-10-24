package com.android.w2sapp.ui.posts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.w2sapp.data.api.PostService
import com.android.w2sapp.data.models.Resource
import com.android.w2sapp.data.models.post.Post
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(private val postService: PostService) : ViewModel() {


    val postsLiveData: LiveData<Resource<MutableList<Post>>>
        get() = _postsLiveData

    private var _postsLiveData = MutableLiveData<Resource<MutableList<Post>>>()

    private val _chipPosts = mutableListOf<Post>()

    val chipPostsLiveData: LiveData<Resource<List<Post>>>
        get() = _chipPostsLiveData

    private var _chipPostsLiveData = MutableLiveData<Resource<List<Post>>>()


    fun getAllLocalPosts(context: Context) {
        val inputStream = context.assets.open("posts_data.json")
        viewModelScope.launch {
            _postsLiveData.value = Resource.Loading()
            try {
                val result = withContext(Dispatchers.IO) {
                    inputStream.bufferedReader().use {
                        Gson().fromJson(it.readText(), Array<Post>::class.java)
                    }
                }
                _postsLiveData.value = Resource.Success(result.toMutableList())
            } catch (e: Exception) {
                _postsLiveData.value = Resource.Error(e.message)
            }

        }
    }


    fun getAllPostsFromApi() {
        viewModelScope.launch {
            _postsLiveData.value = Resource.Loading()
            try {
                val result = postService.getPosts()
                if (result?.isNotEmpty() == true) {
                    _postsLiveData.value = Resource.Success(result.toMutableList())
                } else {
                    _postsLiveData.value = Resource.Empty()
                }

            } catch (e: Exception) {
                Timber.d(e)
                if (e is IOException) {
                    _postsLiveData.value = Resource.Offline()
                } else {
                    _postsLiveData.value = Resource.Error(e.message)
                }
            }

        }
    }

    fun addToChipGroup(post: Post) {
        _chipPosts.add(post)
        _chipPostsLiveData.value = Resource.Success(_chipPosts)

        val removedItemIndex = _postsLiveData.value?.data?.indexOf(post) ?: -1
        if (removedItemIndex != -1) {
            _postsLiveData.value?.data?.removeAt(removedItemIndex)
        }
    }

    fun removeFromChipGroup(post: Post) {
        val removingChipIndex = _chipPosts.indexOf(post)
        _chipPosts.removeAt(removingChipIndex)
        _chipPostsLiveData.value = Resource.Success(_chipPosts)

        _postsLiveData.value?.data?.add(0, post)
    }

}