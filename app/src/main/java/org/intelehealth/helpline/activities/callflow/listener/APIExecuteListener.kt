package org.intelehealth.helpline.activities.callflow.listener

import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModel

interface APIExecuteListener<T> {
    fun onSuccess(result: MissedCallsResponseModel)
    fun onLoading(isLoading: Boolean)
    fun onFail(message: String)
    fun onError(throwable: Throwable)
}