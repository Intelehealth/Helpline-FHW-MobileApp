package org.intelehealth.helpline.activities.callflow.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.helpline.R
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseDataModel
import org.intelehealth.helpline.activities.callflow.utils.ConnectToWhatsapp
import org.intelehealth.helpline.activities.callflow.utils.InitiateHWToPatientCallFlow
import org.intelehealth.helpline.models.dto.PatientDTO
import org.intelehealth.helpline.utilities.NetworkConnection
import org.intelehealth.klivekit.chat.model.ItemHeader
import org.intelehealth.klivekit.chat.ui.adapter.BaseRecyclerViewAdapter
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.util.LinkedList

class CallsTypeAdapter(
        context: Context,
        item: LinkedList<ItemHeader>
) : BaseRecyclerViewAdapter<ItemHeader>(context, item) {


    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_NORMAL = 1
    private var isLoading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_NORMAL) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_missed_call_layout, parent, false)
            MyViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.load_more_progressbar, parent, false)
            ProgressHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /*if (holder is MyViewHolder) {
            holder.bind(missedCallsResponseDataModels[position])
            handleClickListener(holder, missedCallsResponseDataModels[position].patientNumber)
        } else if (holder is ProgressHolder) {
            if (isLoading) {
                holder.progressBar.visibility = View.VISIBLE
                holder.layoutProgress.visibility = View.VISIBLE
            } else {
                holder.progressBar.visibility = View.GONE
                holder.layoutProgress.visibility = View.GONE
                // If needed, a delayed action can be re-implemented here
            }
        }*/
    }

    override fun getItemCount(): Int {
        return 0;
      //  return missedCallsResponseDataModels.size + if (isLoading) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return 0;
        //return if (position == missedCallsResponseDataModels.size) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
    }

    fun addItems(newMissedCallsResponseDataModels: List<MissedCallsResponseDataModel>) {
       /* missedCallsResponseDataModels.addAll(newMissedCallsResponseDataModels)
        notifyDataSetChanged()*/
    }

    fun addLoading() {
        isLoading = true
        notifyDataSetChanged()
    }

    fun removeLoading() {
        isLoading = false
        notifyDataSetChanged()
    }

    fun clear() {
        //missedCallsResponseDataModels.clear()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.tv_patient_name_call)
        private val textViewDescription: TextView = itemView.findViewById(R.id.tv_date_time_call)
        private val cardMissedCall: CardView = itemView.findViewById(R.id.card_missed_call)
        val ivCall: ImageView = itemView.findViewById(R.id.iv_call_missed_call)
        val ivWhatsapp: ImageView = itemView.findViewById(R.id.iv_call_whatsapp)
        private val tvTypeOfCall: TextView = itemView.findViewById(R.id.tv_type_of_call_recording)

        fun bind(item: MissedCallsResponseDataModel) {
            textViewTitle.text = item.patientNumber
            textViewDescription.text = DateTimeUtils.convertDateToDisplayFormatInCall(item.callTime)
            tvTypeOfCall.text = item.callType
        }
    }

    inner class ProgressHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar_load_more)
        val layoutProgress: ConstraintLayout = itemView.findViewById(R.id.layout_load_more_progress)
    }

    private fun handleClickListener(holder: MyViewHolder, phoneNumber: String) {
        holder.ivCall.setOnClickListener {
            if (NetworkConnection.isOnline(context)) {
                val initiateHWToPatientCallFlow = InitiateHWToPatientCallFlow()
                initiateHWToPatientCallFlow.initiateCallFlowFromHwToPatient(phoneNumber, context, "")
            } else {
                Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show()
            }
        }
        holder.ivWhatsapp.setOnClickListener {
            ConnectToWhatsapp.connectToWhatsappOnUserProfile(context, phoneNumber)
        }
    }
}
