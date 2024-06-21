package org.intelehealth.helpline.activities.callflow.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

 data class CallRequestModel(
     @SerializedName("url")
     @Expose
     var url: String,
     @SerializedName("authHeader")
     @Expose
     var authHeader: String
     ) : Serializable {
         constructor() : this("", "")
     }
