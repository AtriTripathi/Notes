/*
 * Copyright 2019 Atri Tripathi. All rights reserved.
 */

package com.atritripathi.notes.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.atritripathi.notes.models.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long[] insertNote(Note... notes);

    @Query("SELECT * FROM notes;")
    LiveData<List<Note>> getNotes();

    @Delete
    int deleteNote(Note... notes);

    @Update
    int updateNote(Note... notes);

    @Query("SELECT * FROM notes WHERE (title LIKE :searchQuery OR content LIKE :searchQuery)")
    LiveData<List<Note>> searchFor(String searchQuery);
}
