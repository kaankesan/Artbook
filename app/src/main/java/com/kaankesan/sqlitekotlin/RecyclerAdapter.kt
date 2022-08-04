package com.kaankesan.sqlitekotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_row.view.*

class RecyclerAdapter(val artList : ArrayList<art>): RecyclerView.Adapter<RecyclerAdapter.recyclerViewHolder>() {

    class recyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onBindViewHolder(holder: recyclerViewHolder, position: Int) {
        holder.itemView.RecyclerViewRow.text = artList.get(position).name
        holder.itemView.RecyclerViewRow.setOnClickListener{
            val intent = Intent(holder.itemView.context,DetailsActivity::class.java)
            intent.putExtra("idIx","old")
            intent.putExtra("Id",artList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return artList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): recyclerViewHolder {
        val view  =  LayoutInflater.from(parent.context).inflate(R.layout.recycler_row,parent,false)
        return recyclerViewHolder(view)
    }

}