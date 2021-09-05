package com.kodex.dragviews


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kodex.dragviews.adapters.PersonRecyclerClickListener
import com.kodex.dragviews.adapters.PersonsAdapter
import com.kodex.dragviews.adapters.PersonsPlacedAdapter
import com.kodex.dragviews.base.BaseActivity
import com.kodex.dragviews.customeview.tooltip.SimpleTooltip
import com.kodex.dragviews.databinding.ActivityMainBinding
import com.kodex.dragviews.models.PersonModel
import com.kodex.dragviews.models.PersonPlacerModel

class MainActivity : BaseActivity() {

    private var binding:ActivityMainBinding?=null

    private var mAdapterPersons:PersonsAdapter?=null
    private var mAdapterPlacer:PersonsPlacedAdapter?=null

    private var mDataPersons = ArrayList<PersonModel>()
    private var mDataPlacer = ArrayList<PersonPlacerModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        setupRecyclers()


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclers() {

        mDataPersons.add(PersonModel(1,"Carla",R.drawable.patientcarla))
        mDataPersons.add(PersonModel(2,"Professor",R.drawable.alvaro))
        mDataPersons.add(PersonModel(3,"Jackeline",R.drawable.patientjackline))
        mDataPersons.add(PersonModel(4,"Leah",R.drawable.patientlia))

        binding?.rvPersons?.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        mAdapterPersons = PersonsAdapter(this,mDataPersons)
        binding?.rvPersons?.adapter = mAdapterPersons


        mDataPlacer.add(PersonPlacerModel(1,null))
        mDataPlacer.add(PersonPlacerModel(1,null))
        mDataPlacer.add(PersonPlacerModel(1,null))
        mDataPlacer.add(PersonPlacerModel(1,null))
        mDataPlacer.add(PersonPlacerModel(1,null))
        mDataPlacer.add(PersonPlacerModel(1,null))
        binding?.rvPlacer?.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        mAdapterPlacer = PersonsPlacedAdapter(this,mDataPlacer,object:PersonRecyclerClickListener{
            override fun onClickStopDrag(position:Int,item: PersonModel,view: View) {
                Log.d("MunibPersonPlaced",item.personName.toString())

                //showing tooltip here on the view which is clicked
                val toolTip = showToolTip(view, Gravity.BOTTOM, R.layout.tool_tip_view, "Hola")
                toolTip.findViewById<AppCompatButton>(R.id.btnDeletePerson).setTextColor(Color.parseColor("#FFF44336"))
                toolTip.findViewById<AppCompatButton>(R.id.btnDeletePerson).text = """Remove ${item.personName}"""
                toolTip.findViewById<AppCompatButton>(R.id.btnDeletePerson).setOnClickListener {
                    mDataPlacer[position] = PersonPlacerModel(position,null)
                    mAdapterPlacer?.notifyItemChanged(position)
                    toolTip.dismiss()
                }
                toolTip.findViewById<AppCompatButton>(R.id.doAnything).setOnClickListener {
                    toolTip.dismiss()
                }


            }

        })
        binding?.rvPlacer?.adapter = mAdapterPlacer


        mAdapterPersons?.notifyDataSetChanged()
        mAdapterPlacer?.notifyDataSetChanged()

    }


//    fun ScrollView.scrollToView(view: View) {
//
//        // View needs a focus
//        view.requestFocus()
//
//        // Determine if scroll needs to happen
//        val scrollBounds = Rect()
//        this.getHitRect(scrollBounds)
//        if (!view.getLocalVisibleRect(scrollBounds)) {
//            Handler().post { this.smoothScrollTo(0, view.bottom) }
//        }
//    }
//
//    fun NestedScrollView.scrollToView(view: View, scrollPositionType: Int = Int.MAX_VALUE) {
//
//        // View needs a focus
//        view.requestFocus()
//
//        // Determine if scroll needs to happen
//        val scrollBounds = Rect()
//        this.getHitRect(scrollBounds)
//        if (!view.getLocalVisibleRect(scrollBounds)) {
//            if (scrollPositionType == Int.MAX_VALUE) {
//                Handler().post { this.smoothScrollTo(0, view.bottom) }
//            } else {
//                Handler().post { this.smoothScrollTo(0, scrollPositionType) }
//            }
//
//        }
//    }
}