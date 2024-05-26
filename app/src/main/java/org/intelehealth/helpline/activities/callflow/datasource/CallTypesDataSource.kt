package org.intelehealth.helpline.activities.callflow.datasource

import org.intelehealth.helpline.activities.callflow.listener.APIExecuteListener
import org.intelehealth.helpline.activities.callflow.models.ApiResponse
import org.intelehealth.helpline.activities.callflow.models.CallRequestModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseDataModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModel
import org.intelehealth.helpline.networkApiCalls.ApiInterface
import retrofit2.Call

class CallTypesDataSource(apiInterface: ApiInterface): BaseDataSource(apiInterface) {
    fun getMissedCalls(executeListener: APIExecuteListener<MissedCallsResponseModel>, callRequestModel: CallRequestModel) {
        val call: Call<MissedCallsResponseModel> = apiInterface.getMissedCalls1(callRequestModel.url,callRequestModel.authHeader)
        executeCall(executeListener, call)
    }
}