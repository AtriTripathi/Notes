package com.atritripathi.notes.async;

import android.os.AsyncTask;

import com.atritripathi.notes.models.Note;
import com.atritripathi.notes.persistence.NoteDao;

public class DeleteAsyncTask extends AsyncTask<Note, Void, Void> {

    private NoteDao mNoteDao;

    public DeleteAsyncTask(NoteDao dao) {
        mNoteDao = dao;
    }

    @Override
    protected Void doInBackground(Note... notes) {
        mNoteDao.deleteNote(notes);
        return null;
    }
}
