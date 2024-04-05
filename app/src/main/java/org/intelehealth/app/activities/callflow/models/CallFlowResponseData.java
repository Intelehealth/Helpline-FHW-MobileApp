package org.intelehealth.app.activities.callflow.models;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

public class CallFlowResponseData {
    @SerializedName("CallStartTime")
    private String callStartTime;
    @SerializedName("Receiver")
    private String Receiver;

    public String getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(String callStartTime) {
        this.callStartTime = callStartTime;
    }

    public String getReceiver() {
        return Receiver;
    }

    public void setReceiver(String receiver) {
        Receiver = receiver;
    }

    public String getReceiverName() {
        return ReceiverName;
    }

    public void setReceiverName(String receiverName) {
        ReceiverName = receiverName;
    }

    public String getRecordingURL() {
        return recordingURL;
    }

    public void setRecordingURL(String recordingURL) {
        this.recordingURL = recordingURL;
    }

    @SerializedName("ReceiverName")
    private String ReceiverName;
    @SerializedName("RecordingURL")
    private String recordingURL;


}
