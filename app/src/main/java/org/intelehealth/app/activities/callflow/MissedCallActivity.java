package org.intelehealth.app.activities.callflow;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.callflow.adapter.MissedCallsAdapter;
import org.intelehealth.app.activities.callflow.adapter.RecordedCallsAdapter;
import org.intelehealth.app.activities.callflow.models.CallFlowResponseData;
import org.intelehealth.app.activities.callflow.models.CallFlowResponseModelClass;
import org.intelehealth.app.activities.callflow.models.MissedCallsResponseDataModel;
import org.intelehealth.app.activities.callflow.models.MissedCallsResponseModel;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityMissedCallsBinding;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MissedCallActivity extends AppCompatActivity {
    private static final String TAG = "MissedCallsActivity";
    private ObjectAnimator syncAnimator;
    private SessionManager sessionManager;
    private ActivityMissedCallsBinding binding;
    private int currentPage = 1;
    private boolean isLoading = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMissedCallsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUI();
    }

    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_missed_call);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        ImageView ivBackArrow = toolbar.findViewById(R.id.iv_back_arrow_common);
        tvTitle.setText(getResources().getString(R.string.missed_calls));

        ivIsInternet.setOnClickListener(v -> {
            SyncUtils.syncNow(MissedCallActivity.this, ivIsInternet, syncAnimator);
        });

        ivBackArrow.setOnClickListener(v -> {
            Intent intent = new Intent(MissedCallActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });

        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        apiCallForGetListOfMissedCalls(MissedCallActivity.this);

        binding.rvMissedCalls.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        // Load next page when reaching the end of the list
                        loadNextPage();
                    }
                }
            }
        });
    }

    private void loadNextPage() {
        isLoading = true;
        currentPage++;
        apiCallForGetListOfMissedCalls(MissedCallActivity.this);
    }

    private void apiCallForGetListOfMissedCalls(Context context) {
        String finalURL = BuildConfig.SERVER_URL + "/noanswer";
        if (sessionManager.getLoginHWMobileNumber().isEmpty())
            return;
        CustomProgressDialog customProgressDialog = new CustomProgressDialog(context);
        customProgressDialog.show(context.getResources().getString(R.string.please_wait));

        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<MissedCallsResponseModel> resultsObservable = apiService.getMissedCalls(finalURL, AppConstants.AUTH_HEADER_CALL_FLOW);
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<>() {
                        @Override
                        public void onNext(MissedCallsResponseModel res) {
                            customProgressDialog.dismiss();
                            Log.d(TAG, "onNext: missed data 1 : "+new Gson().toJson(res));
                            if (!res.getStatus().isEmpty() && res.getStatus().equalsIgnoreCase("ok") && res.getData() != null && res.getData().size() != 0) {
                                updateDataInView(res.getData());
                            } else {
                                //no data
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            customProgressDialog.dismiss();
                            Toast.makeText(context, context.getResources().getString(R.string.try_later), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onComplete() {
                            customProgressDialog.dismiss();
                        }
                    });
        } catch (IllegalArgumentException e) {
        }

    }

    private void updateDataInView(List<MissedCallsResponseDataModel> data) {
        MissedCallsAdapter missedCallsAdapter = new MissedCallsAdapter(MissedCallActivity.this, data);
        binding.rvMissedCalls.setAdapter(missedCallsAdapter);
    }
}
