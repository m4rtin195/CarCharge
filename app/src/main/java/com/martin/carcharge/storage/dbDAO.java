package com.martin.carcharge.storage;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

import java.sql.Timestamp;
import java.util.List;

@SuppressWarnings("DefaultAnnotationParam")
@Dao
public interface dbDAO
{
    /** Table vehicles **/
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertVehicle(Vehicle v);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVehicles(List<Vehicle> list);

    @Query("SELECT * FROM vehicles WHERE id IN (:vehicleId)")
    Vehicle getVehicle(long vehicleId);
    
    @Query("SELECT * FROM vehicles ORDER BY id ASC")
    List<Vehicle> getAllVehicles();
    
    @Update
    void updateVehicle(Vehicle v);
    
    @Delete
    void deleteVehicle(Vehicle v);
    
    @Query("DELETE FROM vehicles")
    void deleteAllVehicles();
    
    
    /** Table vehicle_statuses **/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertStatus(VehicleStatus vs);
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertStatuses(List<VehicleStatus> list);
    
    @Query("SELECT * FROM vehicle_statuses WHERE vehicleId IN (:vehicleId) ORDER BY timestamp DESC LIMIT 1")
    VehicleStatus getLastStatus(long vehicleId);
    
    @Query("SELECT * FROM vehicle_statuses WHERE vehicleId IN (:vehicleId) AND timestamp BETWEEN (:from) AND (:to)")
    List<VehicleStatus> getStatuses(long vehicleId, Timestamp from, Timestamp to);
    
    @Query("DELETE FROM vehicle_statuses WHERE id IN (SELECT id FROM vehicle_statuses ORDER BY timestamp ASC LIMIT (:count))")
    void deleteStatusesCount(int count);
    
    @Query("DELETE FROM vehicle_statuses")
    void deleteAllStatuses();
    
    @Query("SELECT COUNT(*) FROM vehicle_statuses")
    int countStatuses();
}
