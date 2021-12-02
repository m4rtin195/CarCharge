package com.martin.carcharge.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.martin.carcharge.R;
import com.martin.carcharge.databinding.RowVehicleBinding;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VehiclesAdapter extends RecyclerView.Adapter<VehiclesAdapter.VehicleHolder>
{
    private final Context context;
    private final LifecycleOwner parentLco;
    private List<Vehicle> array;
    private final List<ScheduledExecutorService> updateExecutors;
    private OnItemClickListener onItemClickListener;
    
    VehiclesAdapter(Context context, LifecycleOwner lco, List<Vehicle> array)
    {
        this.context = context;
        this.parentLco = lco;
        this.array = array;
        this.updateExecutors = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public VehicleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.row_vehicle, parent, false);
        return new VehicleHolder(view);
    }
    
    @Override
    public void onBindViewHolder(VehicleHolder holder, int position)
    {
        Vehicle v = array.get(position);
        holder.text_vehicleName.setText(v.getName());
        
        
        
        v.vehicleStatus().observe(parentLco, vs ->
        {
            String ss = _getLastStatusString(v.vehicleStatus().getValue());
            holder.text_vehicleState.setText(ss);
        });
        
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        updateExecutors.add(position, executor);
        executor.scheduleAtFixedRate(() -> //new Runnable()
        {
            String ss = _getLastStatusString(v.vehicleStatus().getValue());
            ((Activity)context).runOnUiThread(() ->
                    holder.text_vehicleState.setText(ss));
        }, 30, 30, TimeUnit.SECONDS); //todo relative string min threshold ("now") ?
        
        //holder.text_vehicleState.setText("Last status: Idle (today, 14:32)");
        holder.text_vehicleState.setText(_getLastStatusString(v.vehicleStatus().getValue()));
        holder.image_vehicleIcon.setImageBitmap(v.getImage());
    }
    
    @Override
    public int getItemCount()
    {
        return array.size();
    }
    
    Vehicle get(int id)
    {
        return array.get(id);
    }
    
    @SuppressLint("NotifyDataSetChanged")
    public void fill(List<Vehicle> array)
    {
        this.array = array;
        this.notifyDataSetChanged();
    }
    
    public void update(Vehicle v)
    {
        int index = array.indexOf(v);
        //array.set(index, v); //todo treba???
        this.notifyItemChanged(index);
    }
    
    void setOnItemClickListener(OnItemClickListener listener)
    {
        onItemClickListener = listener;
    }
    
    public void shutdownExecutors()
    {
        for(ScheduledExecutorService e : updateExecutors)
        {
            e.shutdown();
        }
    }
    
    private String _getLastStatusString(VehicleStatus vs)
    {
        String ss;
        if(vs != null && vs.getState().isNormal())
            ss = "Last status: " +
                    vs.getState().asString(context, false) +
                    " (" + DateUtils.getRelativeTimeSpanString(vs.getTimestamp().getTime(),
                    new Date().getTime(), 0, /*DateUtils.FORMAT_ABBREV_RELATIVE*/0) + ")";
        else
            ss = "Last status: unknown";
        return ss;
        
    }
    
    /**********/
    
    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }
    
    //One line in RecyclerView
    public class VehicleHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        protected ImageView image_vehicleIcon;
        protected TextView text_vehicleName, text_vehicleState;
        
        VehicleHolder(View itemView)
        {
            super(itemView);
            
            RowVehicleBinding binding = RowVehicleBinding.bind(itemView);
            image_vehicleIcon = binding.imageVehicleIcon;
            text_vehicleName = binding.textVehicleName2;
            text_vehicleState = binding.textVehicleState;
            
            itemView.setOnClickListener(this);
        }
        
        @Override
        public void onClick(View view)
        {
            if(onItemClickListener != null)
                onItemClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}