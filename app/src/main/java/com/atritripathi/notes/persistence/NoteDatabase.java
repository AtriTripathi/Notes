/*
 * Copyright 2019 Atri Tripathi. All rights reserved.
 */

package com.atritripathi.notes.persistence;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.atritripathi.notes.models.Note;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "notes_db";

    public static NoteDatabase instance;

    static synchronized NoteDatabase getInstance(final Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context,
                    NoteDatabase.class,
                    DATABASE_NAME
            ).build();
        }
        return instance;
    }

    public abstract NoteDao getNotesDao();
}
