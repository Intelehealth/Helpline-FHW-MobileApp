package org.intelehealth.helpline.activities.callflow.listener

interface OnAPISuccessListener<T> {
    fun onSuccess(result: T?)

}