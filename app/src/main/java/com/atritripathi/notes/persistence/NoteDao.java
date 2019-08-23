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

//    @Query("UPDATE notes SET id = (CASE WHEN id = :idOne THEN :idTwo ELSE :idOne END) WHERE id in (:idOne,:idTwo)")
//    int swapNotes(long idOne, long idTwo);

    @Query("SELECT * FROM notes WHERE (title LIKE :searchQuery OR content LIKE :searchQuery)")
    LiveData<List<Note>> searchFor(String searchQuery);
}
