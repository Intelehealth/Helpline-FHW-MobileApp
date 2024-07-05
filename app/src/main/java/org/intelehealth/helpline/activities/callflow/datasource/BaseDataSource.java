package org.intelehealth.helpline.activities.callflow.datasource;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.intelehealth.helpline.activities.callflow.listener.APIExecuteListener;
import org.intelehealth.helpline.activities.callflow.listener.OnAPISuccessListener;
import org.intelehealth.helpline.activities.callflow.models.ApiResponse;
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModel;
import org.intelehealth.helpline.appointment.api.Api;
import org.intelehealth.helpline.networkApiCalls.ApiInterface;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseDataSource{
    protected final ApiInterface apiInterface;
        private static final String TAG = "BaseDataSource";

        public BaseDataSource(ApiInterface apiInterface) {
            this.apiInterface = apiInterface;
        }

        private <T, S> void enqueueCall(APIExecuteListener<T> executeListener, Call<S> call, OnAPISuccessListener<S> successListener) {
            executeListener.onLoading(true);
            call.enqueue(new Callback<S>() {
                @Override
                public void onResponse(@NonNull Call<S> call, @NonNull Response<S> response) {
                    executeListener.onLoading(false);
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            successListener.onSuccess(response.body());
                        } else {
                            executeListener.onFail("No data found");
                        }
                    } else {
                        String errorMessage;
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage = response.message();
                        }
                        executeListener.onFail(errorMessage);
                       // executeListener.onFail("Error: " + response.code() + " " + errorMessage);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<S> call, Throwable t) {
                    executeListener.onLoading(false);
                    executeListener.onError(t);
                }
            });
        }

        public <T> void executeCall(APIExecuteListener<T> executeListener, Call<MissedCallsResponseModel> call) {
            enqueueCall(executeListener, call, result -> {
                if (result != null && result.getStatus() != null && result.getStatus().equalsIgnoreCase("ok"))
                    executeListener.onSuccess(result);
                // else executeListener.onFail(result());
            });
        }

}
