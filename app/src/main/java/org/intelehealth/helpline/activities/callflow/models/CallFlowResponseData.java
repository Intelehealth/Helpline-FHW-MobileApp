package org.intelehealth.helpline.activities.callflow.models;

import com.google.gson.annotations.SerializedName;

public class CallFlowResponseData {
    @SerializedName("CallStartTime")
    private String CallStartTime;

    @SerializedName("CallType")
    private String CallType;

    @SerializedName("PatientNumber")
    private String PatientNumber;

    public String getCallStartTime() {
        return CallStartTime;
    }

    public void setCallStartTime(String callStartTime) {
        CallStartTime = callStartTime;
    }

    public String getCallType() {
        return CallType;
    }

    public void setCallType(String callType) {
        CallType = callType;
    }

    public String getPatientNumber() {
        return PatientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        PatientNumber = patientNumber;
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
        return RecordingURL;
    }

    public void setRecordingURL(String recordingURL) {
        RecordingURL = recordingURL;
    }

    public String getRecord_id() {
        return record_id;
    }

    public void setRecord_id(String record_id) {
        this.record_id = record_id;
    }

    @SerializedName("Receiver")
    private String Receiver;

    @SerializedName("ReceiverName")
    private String ReceiverName;
    @SerializedName("RecordingURL")
    private String RecordingURL;

    @SerializedName("record_id")
    private String record_id;


}
