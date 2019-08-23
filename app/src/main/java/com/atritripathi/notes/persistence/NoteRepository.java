package com.atritripathi.notes.persistence;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.atritripathi.notes.async.DeleteAsyncTask;
import com.atritripathi.notes.async.InsertAsyncTask;
import com.atritripathi.notes.async.UpdateAsyncTask;
import com.atritripathi.notes.models.Note;

import java.util.List;

public class NoteRepository {

    private NoteDatabase mNoteDatabase;

    public NoteRepository(Context context) {
        mNoteDatabase = NoteDatabase.getInstance(context);
    }

    public void insertNoteTask(Note note) {
        new InsertAsyncTask(mNoteDatabase.getNotesDao()).execute(note);
    }

    public void updateNoteTask(Note note) {
        new UpdateAsyncTask(mNoteDatabase.getNotesDao()).execute(note);
    }

    public LiveData<List<Note>> retrieveNotesTask() {
        return mNoteDatabase.getNotesDao().getNotes();
    }

    public LiveData<List<Note>> searchQuery(String query) {
        return mNoteDatabase.getNotesDao().searchFor(query);
    }

//    public void swapNotesTask(Note noteOne, Note noteTwo) {
//        new SwapAsyncTask(mNoteDatabase.getNotesDao()).execute(noteOne,noteTwo);
//    }

    public void deleteNoteTask(Note note) {
        new DeleteAsyncTask(mNoteDatabase.getNotesDao()).execute(note);
    }
}
