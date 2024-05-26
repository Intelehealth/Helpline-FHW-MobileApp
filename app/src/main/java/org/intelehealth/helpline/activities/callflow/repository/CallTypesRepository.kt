package org.intelehealth.helpline.activities.callflow.repository

import android.util.Log
import org.intelehealth.helpline.activities.callflow.datasource.CallTypesDataSource
import org.intelehealth.helpline.activities.callflow.listener.APIExecuteListener
import org.intelehealth.helpline.activities.callflow.models.BaseViewModel
import org.intelehealth.helpline.activities.callflow.models.CallRequestModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseDataModel
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModel
import org.intelehealth.helpline.utilities.SessionManager

class CallTypesRepository(private val dataSource: CallTypesDataSource) {

    fun getMissedCalls(executeListener: APIExecuteListener<MissedCallsResponseModel>, callRequestModel: CallRequestModel) {
        Log.d("TAG", "k23getMissedCalls: ")
        dataSource.getMissedCalls(executeListener,callRequestModel)
    }


    /*
        suspend fun getMissedCalls(page: Int): Result<List<MissedCallsResponseDataModel>> {
            // Check network connection
            if (!NetworkConnection.isOnline(context)) {
                return Result.Error("No network connection")
            }

            val sessionManager = SessionManager(context)
            // Check for logged-in user's mobile number
            if (sessionManager.loginHWMobileNumber.isEmpty()) {
                return Result.Error("No login mobile number found")
            }

            val finalURL = "${BuildConfig.SERVER_URL}/noanswer/$page"

            return withContext(Dispatchers.IO) {
                try {
                    val response = apiService.getMissedCalls1(finalURL, AppConstants.AUTH_HEADER_CALL_FLOW)
                    val res = response.body()

                    if (res != null && res.status == "ok" &&  res.data.isNotEmpty()) {
                        if (!res.data.isNullOrEmpty()) {
                            Result.Success(res.data, "data received")
                        } else {
                            Result.Fail("No data available")
                        }
                    } else {
                        Result.Fail("Failed to fetch data: ${response.message()}")
                    }
                } catch (e: Exception) {
                    Result.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    */
}