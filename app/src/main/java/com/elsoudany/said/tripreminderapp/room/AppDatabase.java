package com.elsoudany.said.tripreminderapp.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Trip.class, User.class,Note.class}, version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDAO userDAO();
    public abstract UserTripDAO userTripDAO();
    public abstract TripDAO tripDAO();
    public abstract TripNoteDao tripNoteDao();
    public abstract NoteDao noteDao();

}
