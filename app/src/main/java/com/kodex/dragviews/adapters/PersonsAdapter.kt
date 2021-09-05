package com.kodex.dragviews.adapters

import android.content.ClipData
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kodex.dragviews.R
import com.kodex.dragviews.base.BaseActivity
import com.kodex.dragviews.customviews.boom.BoomReverse
import com.kodex.dragviews.databinding.PersonLayoutBinding
import com.kodex.dragviews.models.PersonModel

class PersonsAdapter(private var context: BaseActivity,private var mData:ArrayList<PersonModel>):RecyclerView.Adapter<PersonsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonsViewHolder {
        return PersonsViewHolder(LayoutInflater.from(context).inflate(R.layout.person_layout, null))
    }

    override fun onBindViewHolder(holder: PersonsViewHolder, position: Int) {

        val item = mData[position]

        BoomReverse(holder.itemView)
        item.personImage?.let { holder.binding?.ivPersonImage?.setImageResource(it) }
        holder.binding?.tvPersonName?.text = item.personName?:""


        holder.binding?.root?.setOnLongClickListener {

            val gson = Gson()
            val jsonObj = gson.toJson(item)

            val data = ClipData.newPlainText(item.personName ?: "", jsonObj.toString())
            val shadowBuilder = View.DragShadowBuilder(
                it
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(data, shadowBuilder, it, View.DRAG_FLAG_OPAQUE)
            } else {
                it.startDrag(data,shadowBuilder,it, 0)//View.DRAG_FLAG_OPAQUE
            }
        }


    }

    override fun getItemCount(): Int = mData.size
}
class PersonsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
    val binding:PersonLayoutBinding?= DataBindingUtil.bind(itemView)
}