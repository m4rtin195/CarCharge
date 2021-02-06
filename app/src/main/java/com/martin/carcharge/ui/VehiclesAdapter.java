package com.martin.carcharge.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.martin.carcharge.R;
import com.martin.carcharge.models.Vehicle;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VehiclesAdapter extends RecyclerView.Adapter<VehiclesAdapter.VehicleHolder>
{
    private final Context context;
    private final List<Vehicle> array;
    private OnItemClickListener onItemClickListener;
    
    VehiclesAdapter(Context context, List<Vehicle> array)
    {
        this.context = context;
        this.array = array;
    }
    
    @NotNull
    @Override
    public VehicleHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.row_vehicle, parent, false);
        return new VehicleHolder(view);
    }
    
    @Override
    public void onBindViewHolder(VehicleHolder holder, int position)
    {
        Vehicle vehicle = array.get(position);
        holder.text_vehicleName.setText(vehicle.getName());
        holder.text_vehicleState.setText("not implemented");
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
    
    public void add(Vehicle vehicle)
    {
        array.add(vehicle);
        this.notifyDataSetChanged();
    }
    
    public void remove(int id)
    {
        array.remove(id);
        this.notifyDataSetChanged();
    }
    
    void setOnItemClickListener(OnItemClickListener listener)
    {
        onItemClickListener = listener;
    }
    
    
    
    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }
    
    public class VehicleHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        protected TextView text_vehicleName, text_vehicleState;
        
        VehicleHolder(View itemView)
        {
            super(itemView);
            text_vehicleName = itemView.findViewById(R.id.text_vehicleName2);
            text_vehicleState = itemView.findViewById(R.id.text_vehicleState);
            
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