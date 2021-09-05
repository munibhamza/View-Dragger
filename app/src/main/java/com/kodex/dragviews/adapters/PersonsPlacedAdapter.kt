package com.kodex.dragviews.adapters


import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kodex.dragviews.R
import com.kodex.dragviews.base.BaseActivity
import com.kodex.dragviews.base.viewGone
import com.kodex.dragviews.base.viewVisible
import com.kodex.dragviews.customviews.boom.BoomReverse
import com.kodex.dragviews.databinding.PersonCatcherLayoutBinding
import com.kodex.dragviews.models.PersonModel
import com.kodex.dragviews.models.PersonPlacerModel

class PersonsPlacedAdapter(private var context: BaseActivity,private var mData:ArrayList<PersonPlacerModel>,private var delegate: PersonRecyclerClickListener):
    RecyclerView.Adapter<PersonPlacerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonPlacerViewHolder {
        return PersonPlacerViewHolder(LayoutInflater.from(context).inflate(R.layout.person_catcher_layout, null))
    }

    override fun onBindViewHolder(holder: PersonPlacerViewHolder, position: Int) {
        val item = mData[position]

        if(item.personModel==null){
            holder.binding?.tvDropItem?.viewVisible()
            holder.binding?.llDataContainer?.viewGone()
        }else{
            BoomReverse(holder.itemView)

            holder.binding?.tvDropItem?.viewGone()
            holder.binding?.llDataContainer?.viewVisible()
            item.personModel.personImage?.let { holder.binding?.ivPersonImageContainer?.setImageResource(it) }
            holder.binding?.tvPersonNameContainer?.text = item.personModel.personName?:""
            holder.itemView.setOnLongClickListener {
                delegate.onClickStopDrag(position,item.personModel,holder.itemView)
                true
            }
        }



        holder.itemView.setOnDragListener { view, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    Log.d("receive", "DRAG START")
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // animate or change bg when drag view on this view
                    val startAnimation = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    view.startAnimation(startAnimation)
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    view.clearAnimation()
                }
                DragEvent.ACTION_DROP -> {
                    val dataDrop = dragEvent.clipData
                    dataDrop.getItemAt(0).text
                    val gson= Gson()
                    val personObj = gson.fromJson(dataDrop.getItemAt(0).text.toString(),PersonModel::class.java)
                    val newItemToInsert = PersonPlacerModel(position,personObj)
                    mData[position] = newItemToInsert
                    notifyItemChanged(position)
                    
                    //if want to insert at new position
//                    mData.add(position,newItemToInsert)
//                    notifyItemInserted(position)

                    //use this delegate for data usage on main screen if needed
//                    delegate.onClickStopDrag(position,personObj,holder.itemView)
                    //call eventbus if needed
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    Log.d("receive", "ended")
                    view.clearAnimation()
                }
            }
            true
        }
    }

    override fun getItemCount(): Int = mData.size
}

class PersonPlacerViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
    val binding:PersonCatcherLayoutBinding?= DataBindingUtil.bind(itemView)
}

interface PersonRecyclerClickListener {
    fun onClickStopDrag(position: Int,item: PersonModel,view:View)
}