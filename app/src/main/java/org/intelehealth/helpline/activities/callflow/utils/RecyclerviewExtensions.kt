package org.intelehealth.helpline.activities.callflow.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setupLinearView(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) : LinearLayoutManager{
   /* layoutManager = LinearLayoutManager(this.context)
    this.adapter = adapter*/
    val layoutManager = LinearLayoutManager(this.context)
    this.layoutManager = layoutManager
    this.adapter = adapter
    return layoutManager
}