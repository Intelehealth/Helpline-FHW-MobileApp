package org.intelehealth.helpline.activities.callflow.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import org.intelehealth.helpline.activities.callflow.listener.APIExecuteListener
import org.intelehealth.helpline.activities.callflow.utils.NetworkHelper
import org.intelehealth.klivekit.data.PreferenceHelper

open class BaseViewModel(  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
private val networkHelper: NetworkHelper? = null,
private val preferenceHelper: PreferenceHelper? = null
) : ViewModel() {
    private val loadingData = MutableLiveData<Boolean>()

    @JvmField
    var loading: LiveData<Boolean> = loadingData

    private val failResult = MutableLiveData<String>()

    @JvmField
    var failDataResult: LiveData<String> = failResult

    private val errorResult = MutableLiveData<Throwable>()

    @JvmField
    var errorDataResult: LiveData<Throwable> = errorResult


    abstract inner class ExecutionListener<T> : APIExecuteListener<T> {
        override fun onLoading(isLoading: Boolean) {
            loadingData.postValue(isLoading)
        }

        override fun onFail(message: String) {
            failResult.postValue(message)
            loadingData.postValue(false)
            Log.d(TAG, "onFail: message : $message")
        }

        override fun onError(throwable: Throwable) {
            Log.d(TAG, "onFail: throwable : " + throwable.localizedMessage)
            errorResult.postValue(throwable)
            loadingData.postValue(false)
        }
    }


    fun <L> executeLocalQuery(
            queryCall: () -> L?
    ) = flow {
        val localData = queryCall.invoke()
        localData?.let { emit(Result.Success(localData, "")) } ?: kotlin.run {
            emit(Result.Error<L>("No record found"))
        }
    }.onStart {
        emit(Result.Loading<L>("Please wait..."))
    }.flowOn(dispatcher)

    /**
     * Handle response here in base with loading and error message
     *
     */
    fun <T> handleResponse(it: Result<T>, callback: (data: List<T>) -> Unit) {
        println("handleResponse status ${it.status} ${it.message}")
        when (it.status) {
            Result.Status.LOADING -> {
                loadingData.postValue(true)
            }

            Result.Status.FAIL -> {
                loadingData.postValue(false)
                failResult.postValue("")
            }

            Result.Status.SUCCESS -> {
                loadingData.postValue(false)
                it.data?.let { data ->
                    println("data ${Gson().toJson(data)}")
                    callback(listOf(data))
                } ?: failResult.postValue(it.message ?: "")
            }

            Result.Status.ERROR -> {
                println("ERROR ${it.message}")
                loadingData.postValue(false)
                errorResult.postValue(Throwable(it.message))
            }
        }
    }

    fun updateFailResult(message: String) {
        failResult.postValue(message)
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}