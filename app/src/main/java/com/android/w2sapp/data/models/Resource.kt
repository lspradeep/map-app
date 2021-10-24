package com.android.w2sapp.data.models

sealed class Resource<T>(val data: T?, val status: ResourceStatus, val message: String?) {
    class Loading<T>() : Resource<T>(null, ResourceStatus.LOADING, null)

    data class Success<T>(val d: T?) : Resource<T>(d, ResourceStatus.SUCCESS, null)

    class Empty<T>() : Resource<T>(null, ResourceStatus.EMPTY, null)

    data class Error<T>(val msg: String?) : Resource<T>(null, ResourceStatus.ERROR, msg)

    class Offline<T>() : Resource<T>(null, ResourceStatus.OFFLINE_ERROR, null)
}
