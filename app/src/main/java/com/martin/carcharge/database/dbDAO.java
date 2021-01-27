package com.martin.carcharge.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

import java.sql.Timestamp;
import java.util.List;

@Dao
public interface dbDAO
{
    /** Table vehicles **/
    
    @Insert
    long insert(Vehicle v);

    @Query("SELECT * FROM vehicles WHERE id IN (:vehicleId)")
    Vehicle getVehicle(long vehicleId);
    
    @Query("SELECT * FROM vehicles ORDER BY id ASC")
    List<Vehicle> getAllVehicles();
    
    @Update
    void updateVehicle(Vehicle v);
    
    @Delete
    void deleteVehicle(Vehicle v);
    
    
    /** Table vehicle_statuses **/

    @Insert
    long insert(VehicleStatus vs);
    
    @Query("SELECT * FROM vehicle_statuses WHERE id IN (:statusId)")
    VehicleStatus getStatus(long statusId);
    //todo nie radsej last status?
    
    @Query("SELECT * FROM vehicle_statuses WHERE vehicleId IN (:vehicleId) AND timestamp BETWEEN (:from) AND (:to)")
    List<VehicleStatus> getStatuses(long vehicleId, Timestamp from, Timestamp to);
    
    @Query("DELETE FROM vehicle_statuses")
    void deleteAllStatuses();
    
}
