package org.intelehealth.helpline.activities.callflow.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MissedCallsResponseModel {
    @SerializedName("IncomingCalls")
    private List<MissedCallsResponseDataModel> IncomingCalls;

    public List<MissedCallsResponseDataModel> getIncomingCalls() {
        return IncomingCalls;
    }

    public void setIncomingCalls(List<MissedCallsResponseDataModel> incomingCalls) {
        IncomingCalls = incomingCalls;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MissedCallsResponseDataModel> getOutgoingCalls() {
        return OutgoingCalls;
    }

    public void setOutgoingCalls(List<MissedCallsResponseDataModel> outgoingCalls) {
        OutgoingCalls = outgoingCalls;
    }

    @SerializedName("status")
    private String status;
    @SerializedName("OutgoingCalls")
    private List<MissedCallsResponseDataModel> OutgoingCalls;

    public String getStatus() {
        return status;
    }
}
