package org.intelehealth.app.activities.visit.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SendSMSRequestModel {

    @SerializedName("module")
    @Expose
    private String module;

    @SerializedName("apikey")
    @Expose
    private String apikey;

    @SerializedName("to")
    @Expose
    private String to;

    @SerializedName("from")
    @Expose
    private String from;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCtid() {
        return ctid;
    }

    public void setCtid(String ctid) {
        this.ctid = ctid;
    }

    @SerializedName("msg")
    @Expose
    private String msg;

    @SerializedName("ctid")
    @Expose
    private String ctid;

}
