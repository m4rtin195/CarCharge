package com.martin.carcharge.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.martin.carcharge.models.Vehicle;

import java.util.List;

@Dao
public interface dbDAO
{
    @Insert
    long insert(Vehicle vehicle);

    @Query("DELETE FROM Vehicle")
    void deleteAll();

    @Delete
    void delete(Vehicle Vehicle);

    @Query("SELECT * FROM Vehicle ORDER BY id ASC")
    List<Vehicle> getAllVehicles();

    @Query("SELECT * FROM Vehicle WHERE id IN (:VehicleId)")
    Vehicle getVehicle(long VehicleId);

    @Update
    void updateVehicle(Vehicle vehicle);
 }
