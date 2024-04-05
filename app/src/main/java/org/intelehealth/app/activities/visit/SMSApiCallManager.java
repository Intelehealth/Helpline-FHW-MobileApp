package org.intelehealth.app.activities.visit;

import android.content.Context;
import android.graphics.Color;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.callflow.utils.InitiateHWToPatientCallFlow;
import org.intelehealth.app.activities.visit.model.SendSMSRequestModel;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class SMSApiCallManager {
    private static final String TAG = "SMSApiCallManager";

    public static void checkInternetAndCallApi(String phoneNumber, Context context, String smsMsgBody) {
        if (NetworkConnection.isOnline(context)) {
            apiCallForSendSmsRequest(phoneNumber, context, smsMsgBody);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        }
    }

    private static void apiCallForSendSmsRequest(String phoneNumber, Context context, String smsMsgBody) {
        Log.d(TAG, "apiCallForSendSmsRequest: smsMsgBody :" + smsMsgBody);
        Log.d(TAG, "apiCallForSendSmsRequest: phoneNumber :" + phoneNumber);
        CustomProgressDialog customProgressDialog = new CustomProgressDialog(context);
        customProgressDialog.show(context.getResources().getString(R.string.please_wait));

        //String serverUrl = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/provider/" + sessionManager.getProviderID() + "/"; //${target_provider_uuid}/attribute/${target_provider_attribute_uuid}
        SendSMSRequestModel sendSMSRequestModel = new SendSMSRequestModel();
        sendSMSRequestModel.setModule(AppConstants.MODULE);
        sendSMSRequestModel.setApikey(AppConstants.APIKEY);
        sendSMSRequestModel.setFrom(AppConstants.FROM);
        sendSMSRequestModel.setTo(phoneNumber);
        sendSMSRequestModel.setCtid(AppConstants.CTID);
        sendSMSRequestModel.setMsg(Html.fromHtml(smsMsgBody).toString());

        Log.d(TAG, "apiCallForSendSmsRequest: reqmodel : " + new Gson().toJson(sendSMSRequestModel));

        String serverUrl = AppConstants.SMS_API_REQUEST_URL;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ResponseBody> smsRequest = apiService.sendSMS(serverUrl, sendSMSRequestModel.getModule(),
                sendSMSRequestModel.getApikey(), sendSMSRequestModel.getTo(), sendSMSRequestModel.getFrom(),
                sendSMSRequestModel.getMsg(), sendSMSRequestModel.getCtid());
        smsRequest.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody responseBody) {
                Log.d(TAG, "onNext: response sms : " + new Gson().toJson(responseBody));
                customProgressDialog.dismiss();
                try {
                    if (responseBody != null) {
                        String jsonData = responseBody.string();
                        JSONObject jsonObject = new JSONObject(jsonData);

                        // Accessing values from the JSON object
                        String status = jsonObject.getString("Status");
                        System.out.println("Status: " + status);
                        if (!status.isEmpty() && status.equalsIgnoreCase("success")) {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.sms_sent), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.request_failed), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        System.out.println("Response body is null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                        /*  try {
                    String status = jsonObject.getString("Status");
                    if (status.equalsIgnoreCase("Success")) {
                        // Response is successful
                        String details = jsonObject.getString("Details");
                        // Do something with the details
                        Toast.makeText(context, ContextCompat.getString(context, R.string.sms_sent), Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(context, ContextCompat.getString(context, R.string.request_failed), Toast.LENGTH_SHORT).show();
                        // Response is not successful
                        // Handle the error case
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Handle JSON parsing error
                }*/
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                customProgressDialog.dismiss();

                Logger.logD(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                customProgressDialog.dismiss();

                Logger.logD(TAG, "completed");
            }
        });
    }


}
