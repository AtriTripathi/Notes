/*
 * Copyright 2019 Atri Tripathi. All rights reserved.
 */

package com.atritripathi.notes.async;

import android.os.AsyncTask;

import com.atritripathi.notes.models.Note;
import com.atritripathi.notes.persistence.NoteDao;

public class UpdateAsyncTask extends AsyncTask<Note, Void, Void> {

    private NoteDao mNoteDao;

    public UpdateAsyncTask(NoteDao dao) {
        mNoteDao = dao;
    }

    @Override
    protected Void doInBackground(Note... notes) {
        mNoteDao.updateNote(notes);
        return null;
    }
}
