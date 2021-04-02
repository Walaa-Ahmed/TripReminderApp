package com.elsoudany.said.tripreminderapp.room;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TripNote {
    @Embedded
    public Trip trip;
    @Relation(
            parentColumn = "uid",
            entityColumn = "tripUid"
    )
    public List<Note> noteList;
}
