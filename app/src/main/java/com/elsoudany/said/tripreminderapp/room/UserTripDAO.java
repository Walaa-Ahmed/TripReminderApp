package com.elsoudany.said.tripreminderapp.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface UserTripDAO {
    @Transaction
    @Query("Select * From user WHERE userId = :id")
    public List<UserTrip> getAllTrips(String id);
}
