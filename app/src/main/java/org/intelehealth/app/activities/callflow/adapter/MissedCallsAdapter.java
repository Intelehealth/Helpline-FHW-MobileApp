package org.intelehealth.app.activities.callflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.callflow.models.MissedCallsResponseDataModel;
import org.intelehealth.app.activities.callflow.models.MissedCallsResponseModel;
import org.intelehealth.app.activities.callflow.utils.InitiateHWToPatientCallFlow;
import org.intelehealth.app.activities.callflow.models.CallFlowResponseData;
import org.intelehealth.app.activities.callflow.utils.ConnectToWhatsapp;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.RowItemMissedCallLayoutBinding;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.util.List;

public class MissedCallsAdapter extends RecyclerView.Adapter<MissedCallsAdapter.MyViewHolder> {
    private static final String TAG = "MissedCallsAdapter";
    private Context context;
    private List<MissedCallsResponseDataModel> missedCallsList;

    public MissedCallsAdapter(Context context, List<MissedCallsResponseDataModel> missedCallsList) {
        this.context = context;
        this.missedCallsList = missedCallsList;
    }

    @Override
    public MissedCallsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowItemMissedCallLayoutBinding binding = RowItemMissedCallLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MissedCallsAdapter.MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MissedCallsAdapter.MyViewHolder holder, int position) {
        MissedCallsResponseDataModel callFlowResponseData = missedCallsList.get(position);
        if (callFlowResponseData.getName() != null && !callFlowResponseData.getName().isEmpty())
            holder.binding.tvPatientNameCall.setText(callFlowResponseData.getName());
        else
            holder.binding.tvPatientNameCall.setText(callFlowResponseData.getNoanswer());//i.e.phone number.will change the key name - not proper

        holder.binding.tvDateTimeCall.setText(DateTimeUtils.convertDateToDisplayFormatInCall(callFlowResponseData.getCallTime()));
        handleClickListener(holder, holder.binding, callFlowResponseData.getNoanswer());
    }

    private void handleClickListener(MyViewHolder holder, RowItemMissedCallLayoutBinding binding, String phoneNumber) {
        holder.binding.ivCallMissedCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkConnection.isOnline(context)) {
                    InitiateHWToPatientCallFlow initiateHWToPatientCallFlow = new InitiateHWToPatientCallFlow();
                    initiateHWToPatientCallFlow.initiateCallFlowFromHwToPatient(phoneNumber, context, "");
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.binding.ivCallWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectToWhatsapp.connectToWhatsappOnUserProfile(context, phoneNumber);
            }
        });
    }

    @Override
    public int getItemCount() {
        return missedCallsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RowItemMissedCallLayoutBinding binding;

        public MyViewHolder(RowItemMissedCallLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}