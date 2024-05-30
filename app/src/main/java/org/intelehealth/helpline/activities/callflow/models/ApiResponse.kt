package org.intelehealth.helpline.activities.callflow.models

import com.google.gson.annotations.SerializedName

class ApiResponse<T>{

    @SerializedName("IncomingCalls")
    private var IncomingCalls: List<MissedCallsResponseDataModel?>? = null

    fun getIncomingCalls(): List<MissedCallsResponseDataModel?>? {
        return IncomingCalls
    }

    fun setIncomingCalls(incomingCalls: List<MissedCallsResponseDataModel?>?) {
        IncomingCalls = incomingCalls
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getOutgoingCalls(): List<MissedCallsResponseDataModel?>? {
        return OutgoingCalls
    }

    fun setOutgoingCalls(outgoingCalls: List<MissedCallsResponseDataModel?>?) {
        OutgoingCalls = outgoingCalls
    }

    @SerializedName("status")
    private var status: String? = null

    @SerializedName("OutgoingCalls")
    private var OutgoingCalls: List<MissedCallsResponseDataModel?>? = null

    fun getStatus(): String? {
        return status
    }

}
