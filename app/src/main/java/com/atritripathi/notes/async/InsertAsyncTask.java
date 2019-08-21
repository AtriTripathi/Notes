package com.atritripathi.notes.async;

import android.os.AsyncTask;

import com.atritripathi.notes.models.Note;
import com.atritripathi.notes.persistence.NoteDao;

public class InsertAsyncTask extends AsyncTask<Note, Void, Void> {

    private NoteDao mNoteDao;

    public InsertAsyncTask(NoteDao dao) {
        mNoteDao = dao;
    }

    @Override
    protected Void doInBackground(Note... notes) {
        mNoteDao.insertNote(notes);
        return null;
    }
}
