package org.intelehealth.app.activities.callflow.adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.callflow.models.MissedCallsResponseDataModel;
import org.intelehealth.app.activities.callflow.utils.ConnectToWhatsapp;
import org.intelehealth.app.activities.callflow.utils.InitiateHWToPatientCallFlow;
import org.intelehealth.app.databinding.RowItemMissedCallLayoutBinding;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.util.List;

public class MissedCallsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;

    private List<MissedCallsResponseDataModel> mMissedCallsResponseDataModels;
    private Context context;
    private boolean isLoading = false;

    public MissedCallsAdapter(Context context, List<MissedCallsResponseDataModel> MissedCallsResponseDataModels) {
        this.mMissedCallsResponseDataModels = MissedCallsResponseDataModels;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_missed_call_layout, parent, false);
            return new MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_progressbar, parent, false);
            return new ProgressHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).onBind(mMissedCallsResponseDataModels.get(position));
            handleClickListener((MyViewHolder) holder, mMissedCallsResponseDataModels.get(position).getPatientNumber());
        } else if (holder instanceof ProgressHolder) {
            // Handle progress bar view holder
            ProgressHolder progressHolder = (ProgressHolder) holder;
            if (isLoading) {
                progressHolder.progressBar.setVisibility(View.VISIBLE);
                progressHolder.layoutProgress.setVisibility(View.VISIBLE);
            } else {
                progressHolder.progressBar.setVisibility(View.GONE);
                progressHolder.layoutProgress.setVisibility(View.GONE);
                final Handler handler = new Handler();

               /* Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progressHolder.progressBar.setVisibility(View.GONE);
                        } catch (IllegalStateException ed) {
                            ed.printStackTrace();
                        }
                    }
                };
                handler.postDelayed(runnable, 3000);
*/
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMissedCallsResponseDataModels.size() + (isLoading ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        Log.d("TAG", "getItemViewType: ");
        return position == mMissedCallsResponseDataModels.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
    }

    public void addItems(List<MissedCallsResponseDataModel> MissedCallsResponseDataModels) {
        mMissedCallsResponseDataModels.addAll(MissedCallsResponseDataModels);
        notifyDataSetChanged();
    }

    public void addLoading() {
        isLoading = true;
        notifyDataSetChanged();
    }

    public void removeLoading() {
        isLoading = false;
        notifyDataSetChanged();
    }

    public void clear() {
        mMissedCallsResponseDataModels.clear();
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        CardView cardMissedCall;
        ImageView ivCall;
        ImageView ivWhatsapp;

        MyViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.tv_patient_name_call);
            textViewDescription = itemView.findViewById(R.id.tv_date_time_call);
            cardMissedCall = itemView.findViewById(R.id.card_missed_call);
            ivCall = itemView.findViewById(R.id.iv_call_missed_call);
            ivWhatsapp = itemView.findViewById(R.id.iv_call_whatsapp);
        }

        public void onBind(MissedCallsResponseDataModel item) {
            textViewTitle.setText(item.getPatientNumber());
            textViewDescription.setText(DateTimeUtils.convertDateToDisplayFormatInCall(item.getCallTime()));
        }
    }

    public class ProgressHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        ConstraintLayout layoutProgress;

        ProgressHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar_load_more);
            layoutProgress = itemView.findViewById(R.id.layout_load_more_progress);

        }
    }

    private void handleClickListener(MyViewHolder holder, String phoneNumber) {
        holder.ivCall.setOnClickListener(new View.OnClickListener() {
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
        holder.ivWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectToWhatsapp.connectToWhatsappOnUserProfile(context, phoneNumber);
            }
        });
    }
}