package org.intelehealth.helpline.activities.callflow.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CallResponseModel<T> {
    @SerializedName("success")
    @Expose
    private val success = false

    @SerializedName("message")
    @Expose
    private val message: String? = null

    @SerializedName("data")
    @Expose
    private val data: T? = null
}