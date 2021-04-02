package com.elsoudany.said.tripreminderapp.room;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class UserTrip {
    @Embedded
    public User user;
    @Relation(
            parentColumn = "userId",
            entityColumn = "userId"
    )
    public List<Trip> tripList;

    @Override
    public String toString() {
        return "UserTrip{" +
                "user=" + user.toString() +
                ", tripList=" + tripList.toString() +
                '}';
    }
}
