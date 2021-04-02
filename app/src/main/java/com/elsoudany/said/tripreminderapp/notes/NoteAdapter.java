package com.elsoudany.said.tripreminderapp.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Note;
import com.elsoudany.said.tripreminderapp.room.NoteDao;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHold>
{
    private Context context;
    List<Note> notes;

    public NoteAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public ViewHold onCreateViewHolder(@NonNull ViewGroup recycleView, int viewType)

    {
        LayoutInflater inflater =LayoutInflater.from(recycleView.getContext());
        View v=inflater.inflate(R.layout.note_row,recycleView,false);
        ViewHold vh=new ViewHold(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHold holder, int position)
    {
        holder.noteValue.setText(notes.get(position).noteBody);
        holder.deleteNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note note = notes.get(position);
                notes.remove(position);
                notifyDataSetChanged();
                new Thread() {
                    @Override
                    public void run() {
                        AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,"DataBase-name").build();
                        NoteDao noteDao = db.noteDao();
                        noteDao.delete(note);
                    }
                }.start();
            }
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class ViewHold extends RecyclerView.ViewHolder
    {
        TextView noteValue;
        ImageView deleteNoteBtn;
        public View layout;


        public ViewHold(@NonNull View itemView)
        {
            super(itemView);
            layout=itemView;
           noteValue=itemView.findViewById(R.id.txt_noteValue);
           deleteNoteBtn=itemView.findViewById(R.id.btn_deleteNote);
        }
    }
}
