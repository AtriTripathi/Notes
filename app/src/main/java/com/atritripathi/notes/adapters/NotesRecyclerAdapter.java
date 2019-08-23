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

import java.util.List;

public class NotesRecyclerAdapter extends RecyclerView.Adapter<NotesRecyclerAdapter.NotesViewHolder> {

    private static final String TAG = "NotesRecyclerAdapter";

    private List<Note> mNotes;
    private OnNoteClickListener mOnNoteClickListener;
    private OnNoteLongClickListener mOnNoteLongClickListener;

    public NotesRecyclerAdapter(List<Note> notes, OnNoteClickListener onNoteClickListener,
                                OnNoteLongClickListener onNoteLongClickListener) {
        this.mNotes = notes;
        this.mOnNoteClickListener = onNoteClickListener;
        this.mOnNoteLongClickListener = onNoteLongClickListener;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_new_list_item,
                parent,false);
        return new NotesViewHolder(view, mOnNoteClickListener, mOnNoteLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        try {
            String day = mNotes.get(position).getTimeStamp().substring(0, 2);
            String month = mNotes.get(position).getTimeStamp().substring(3, 5);
            month = TimestampUtil.getMonthFromNumber(month);
            String year = mNotes.get(position).getTimeStamp().substring(6);
            String timestamp = day + " " + month + " " + year;

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


    public class NotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView title, timeStamp, content;
        OnNoteClickListener onNoteClickListener;
        OnNoteLongClickListener onNoteLongClickListener;

        public NotesViewHolder(@NonNull View itemView, OnNoteClickListener onNoteClickListener,
                               OnNoteLongClickListener onNoteLongClickListener) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            timeStamp = itemView.findViewById(R.id.note_timestamp);
            content = itemView.findViewById(R.id.note_content);
            this.onNoteClickListener = onNoteClickListener;
            this.onNoteLongClickListener = onNoteLongClickListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.onNoteClickListener.onNoteClick(getAdapterPosition());
        }


        @Override
        public boolean onLongClick(View view) {
            this.onNoteLongClickListener.onNoteLongClick(getAdapterPosition());
            return true;
        }
    }

    public interface OnNoteClickListener {
        void onNoteClick(int position);
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClick(int position);
    }
}
