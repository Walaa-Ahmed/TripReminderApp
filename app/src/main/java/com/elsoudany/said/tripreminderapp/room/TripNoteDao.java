package com.elsoudany.said.tripreminderapp.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface TripNoteDao {
    @Transaction
    @Query("Select * From trips WHERE uid = :id")
    public List<TripNote> getAllNotes(long id);
}

