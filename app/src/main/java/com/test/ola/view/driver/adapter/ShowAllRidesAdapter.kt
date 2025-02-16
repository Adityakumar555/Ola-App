package com.test.ola.view.driver.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test.ola.databinding.DriverRidesBinding
import com.test.ola.models.RideAccept
import com.test.ola.view.driver.interfaces.DriverRideClickListener

class ShowAllRidesAdapter( val driverRideClickListener: DriverRideClickListener) : RecyclerView.Adapter<ShowAllRidesAdapter.ViewHolder>() {

    private var rideList = mutableListOf<RideAccept>()


    fun updateRideList(newList: List<RideAccept>){
        rideList.clear()
        rideList = newList.toMutableList()
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: DriverRidesBinding):RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DriverRidesBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return rideList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rideList = rideList[position]


        if (rideList.rideStatus=="Complete"){
            holder.binding.completeRide.visibility = View.GONE
            holder.binding.cancelRide.visibility = View.GONE
            holder.binding.rideComplete.visibility = View.VISIBLE

        }else{
            holder.binding.completeRide.visibility = View.VISIBLE
            holder.binding.cancelRide.visibility = View.VISIBLE
            holder.binding.rideComplete.visibility = View.GONE
        }

        holder.binding.from.text = rideList.from
        holder.binding.to.text = rideList.to

        holder.binding.cancelRide.setOnClickListener {
            rideList.rideId?.let { it1 -> driverRideClickListener.rideCancel(it1) }
        }

        holder.binding.completeRide.setOnClickListener {
            rideList.rideId?.let { it1 -> driverRideClickListener.rideComplete(it1) }
        }


    }
}