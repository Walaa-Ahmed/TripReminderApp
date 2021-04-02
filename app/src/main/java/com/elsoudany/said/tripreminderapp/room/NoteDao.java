package com.elsoudany.said.tripreminderapp.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM note")
    List<Note> getAll();

    @Query("SELECT * FROM note WHERE uid = :id")
    Note getNote(Long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Note... notes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Note note);

    @Delete
    void delete(Note note);

    @Delete
    void deleteAll(List<Note> notes);

}
