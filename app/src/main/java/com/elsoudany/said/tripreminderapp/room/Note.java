package com.elsoudany.said.tripreminderapp.room;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Note {
    @PrimaryKey (autoGenerate = true)
    public long uid;
    public String noteBody;
    public long tripUid;

    public Note() {
    }

    @Ignore
    public Note(String noteBody, long tripUid) {
        this.noteBody = noteBody;
        this.tripUid = tripUid;
    }
}
