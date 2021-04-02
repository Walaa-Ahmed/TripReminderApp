package com.elsoudany.said.tripreminderapp.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Note;
import com.elsoudany.said.tripreminderapp.room.NoteDao;
import com.elsoudany.said.tripreminderapp.room.TripNoteDao;

import java.util.ArrayList;
import java.util.List;

public class AddNoteActivity extends AppCompatActivity {
    Button  btnAddNoteDialog,btnCancelNoteDialog;
    ImageButton backButton,btnAddNotes;
    AlertDialog alertDialog;
    LayoutInflater inflater;
    EditText addNoteText;
    NoteHandler noteHandler;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    List<Note> notes;
    long tripUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        noteHandler = new NoteHandler();
        Intent intent = getIntent();
        tripUid = intent.getLongExtra("TripUid",0);
        notes=new ArrayList<>();
        recyclerView=findViewById(R.id.noteRecyclerView);
        adapter=new NoteAdapter(AddNoteActivity.this,notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(AddNoteActivity.this));
        recyclerView.setAdapter(adapter);
        btnAddNotes=findViewById(R.id.btn_addNewNote);
        backButton=findViewById(R.id.back_btn);
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"DataBase-name").build();
        new Thread()
        {
            @Override
            public void run() {
                TripNoteDao tripNoteDao = db.tripNoteDao();
                notes.addAll(tripNoteDao.getAllNotes(tripUid).get(0).noteList);
                noteHandler.sendEmptyMessage(1);
            }
        }.start();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnAddNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                alertDialog = new AlertDialog.Builder(AddNoteActivity.this).create();
                inflater = LayoutInflater.from(getApplicationContext());
                View dialogView = inflater.inflate(R.layout.add_note_dialog, null);
                btnAddNoteDialog= dialogView.findViewById(R.id.btn_addNoteDialog);
                btnCancelNoteDialog=dialogView.findViewById(R.id.btn_cancelNoteDialog);
                addNoteText=dialogView.findViewById(R.id.txt_addNoteDialog);
                btnAddNoteDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(AddNoteActivity.this, ""+addNoteText.getText(), Toast.LENGTH_SHORT).show();
                        Note addedNote = new Note(addNoteText.getText().toString(),tripUid);
                        new Thread()
                        {
                            @Override
                            public void run() {

                                NoteDao noteDao = db.noteDao();
                                noteDao.insertAll(addedNote);
                            }
                        }.start();
                        notes.add(addedNote);
                        adapter.notifyDataSetChanged();
                        alertDialog.cancel();
                    }
                });
                btnCancelNoteDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        alertDialog.cancel();
                    }
                });
                alertDialog.setView(dialogView);
                alertDialog.show();

            }
        });

    }

    private class NoteHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            adapter.notifyDataSetChanged();
        }
    }
}