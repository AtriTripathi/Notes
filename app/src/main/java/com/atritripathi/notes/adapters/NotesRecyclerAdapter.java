package com.atritripathi.notes.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atritripathi.notes.R;
import com.atritripathi.notes.models.Note;
import com.atritripathi.notes.utils.TimestampUtil;

import java.util.ArrayList;

public class NotesRecyclerAdapter extends RecyclerView.Adapter<NotesRecyclerAdapter.NotesViewHolder> {

    private static final String TAG = "NotesRecyclerAdapter";

    private ArrayList<Note> mNotes;
    private OnNoteClickListener mOnNoteClickListener;

    public NotesRecyclerAdapter(ArrayList<Note> notes, OnNoteClickListener onNoteClickListener) {
        this.mNotes = notes;
        this.mOnNoteClickListener = onNoteClickListener;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_new_list_item,
                parent,false);
        return new NotesViewHolder(view, mOnNoteClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        try {
            String month = mNotes.get(position).getTimeStamp().substring(0, 2);
            month = TimestampUtil.getMonthFromNumber(month);
            String year = mNotes.get(position).getTimeStamp().substring(3);
            String timestamp = month + " " + year;

            holder.title.setText(mNotes.get(position).getTitle());
            holder.timeStamp.setText(timestamp);
            holder.content.setText(mNotes.get(position).getContent());

        } catch (NullPointerException e) {
            Log.d(TAG, "onBindViewHolder: NullPointerException" + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    public class NotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, timeStamp, content;
        OnNoteClickListener onNoteClickListener;

        public NotesViewHolder(@NonNull View itemView, OnNoteClickListener onNoteClickListener) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            timeStamp = itemView.findViewById(R.id.note_timestamp);
            content = itemView.findViewById(R.id.note_content);
            this.onNoteClickListener = onNoteClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.onNoteClickListener.onNoteClick(getAdapterPosition());
        }
    }

    public interface OnNoteClickListener {
        void onNoteClick(int position);
    }
}
