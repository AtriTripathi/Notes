package com.atritripathi.notes.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.atritripathi.notes.R;
import com.atritripathi.notes.models.Note;
import com.atritripathi.notes.persistence.NoteRepository;
import com.atritripathi.notes.utils.LinedEditText;
import com.atritripathi.notes.utils.TimestampUtil;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener {

    // Constants
    public static final String KEY_SELECTED_NOTE = "com.atritripathi.notes.selected.note";
    public static final String KEY_CURRENT_MODE = "com.atritripathi.notes.current.mode";
    private static final int EDIT_MODE_ENABLED = 1;
    private static final int EDIT_MODE_DISABLED = 0;

    // UI Components
    private LinedEditText mLinedEditText;
    private EditText mEditTitle;
    private TextView mViewTitle;
    private RelativeLayout mCheckContainer, mBackArrowContainer;
    private ImageButton mCheck, mBackArrow;

    // Variables
    private boolean mIsNewNote;
    private Note mInitialNote;
    private Note mFinalNote;
    private int mMode;
    private NoteRepository mNoteRepository;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        mLinedEditText = findViewById(R.id.note_text);
        mEditTitle = findViewById(R.id.note_edit_title);
        mViewTitle = findViewById(R.id.note_text_title);
        mCheckContainer = findViewById(R.id.check_container);
        mBackArrowContainer = findViewById(R.id.back_arrow_container);
        mCheck = findViewById(R.id.toolbar_check);
        mBackArrow = findViewById(R.id.toolbar_back_arrow);

        mNoteRepository = new NoteRepository(this);

        setListeners();

        if (getIncomingIntent()) {
            // this is a new note (EDIT MODE)
            setNewNoteProperties();
            enableEditMode();
        } else {
            // this is NOT a new note (VIEW MODE)
            setOldNoteProperties();
            disableContentInteraction();
        }
    }

    /**
     * Setup all the listeners for the note
     */
    private void setListeners() {
        setDoubleTapListener();
        mViewTitle.setOnClickListener(this);
        mCheck.setOnClickListener(this);
        mBackArrow.setOnClickListener(this);
    }


    /**
     * Get the incoming Intent and check if it contains an already existing note
     * from the database. If 'YES' copy it to a proxy holder 'mFinalNote' for
     * further processing, and show the note in View Mode, else show the note
     * in Edit Mode.
     *
     * @return True if it is a new note, False otherwise
     */
    private boolean getIncomingIntent() {
        if (getIntent().hasExtra(KEY_SELECTED_NOTE)) {
            mInitialNote = getIntent().getParcelableExtra(KEY_SELECTED_NOTE);

            mFinalNote = new Note();
            mFinalNote.setId(mInitialNote.getId());
            mFinalNote.setTitle(mInitialNote.getTitle());
            mFinalNote.setContent(mInitialNote.getContent());
            mFinalNote.setTimeStamp(mInitialNote.getTimeStamp());

            mMode = EDIT_MODE_DISABLED;
            mIsNewNote = false;
            return false;
        }
        mMode = EDIT_MODE_ENABLED;
        mIsNewNote = true;
        return true;
    }

    /**
     * Setup the view for taking details of a new note from the user.
     */
    private void setNewNoteProperties() {
        mViewTitle.setText(R.string.note_title);
        mEditTitle.setText(R.string.note_title);

        mInitialNote = new Note();
        mFinalNote = new Note();
        mInitialNote.setTitle("Note Title");
    }

    /**
     * Initialise the view with details of the already existing note.
     */
    private void setOldNoteProperties() {
        mViewTitle.setText(mInitialNote.getTitle());
        mEditTitle.setText(mInitialNote.getTitle());
        mLinedEditText.setText(mInitialNote.getContent());
    }


    /**
     * Enable editing of the current note in view
     */
    private void enableEditMode() {
        // Hide the Back Arrow, show the Tick Mark
        mBackArrowContainer.setVisibility(View.GONE);
        mCheckContainer.setVisibility(View.VISIBLE);

        // Allow editing of Title
        mViewTitle.setVisibility(View.GONE);
        mEditTitle.setVisibility(View.VISIBLE);

        mMode = EDIT_MODE_ENABLED;
        enableContentInteraction();
    }

    /**
     * Disable editing of the current note in view
     */
    private void disableEditMode() {
        // Show the Back Arrow, hide the Tick mark
        mBackArrowContainer.setVisibility(View.VISIBLE);
        mCheckContainer.setVisibility(View.GONE);

        // Only show the Title, do NOT allow editing.
        mViewTitle.setText(mEditTitle.getText().toString());
        mViewTitle.setVisibility(View.VISIBLE);
        mEditTitle.setVisibility(View.GONE);

        mMode = EDIT_MODE_DISABLED;
        disableContentInteraction();

        String noteContent = mLinedEditText.getText().toString().trim();
        if (noteContent.length() > 0) {
            mFinalNote.setTitle(mEditTitle.getText().toString().trim());
            mFinalNote.setContent(mLinedEditText.getText().toString().trim());
            mFinalNote.setTimeStamp(TimestampUtil.getCurrentTimestamp());

            // Only save the changes and commit to Database if user has modified
            // either the Title or the Content of the Note.
            if (!mFinalNote.getContent().equals(mInitialNote.getContent())
                    || !mFinalNote.getTitle().equals(mInitialNote.getTitle())) {
                saveChanges();
            }
        }
    }


    /**
     * Hide the top part of keyboard to properly allow note content editing in horizontal orientation
     */
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /**
     * Allow the content of the note to be edited
     */
    private void enableContentInteraction() {
        mLinedEditText.setKeyListener(new EditText(this).getKeyListener());
        mLinedEditText.setFocusable(true);
        mLinedEditText.setFocusableInTouchMode(true);
        mLinedEditText.setCursorVisible(true);
        mLinedEditText.requestFocus();
    }

    /**
     * Do NOT allow the content of the note to be edited
     */
    private void disableContentInteraction() {
        mLinedEditText.setKeyListener(null);
        mLinedEditText.setFocusable(false);
        mLinedEditText.setFocusableInTouchMode(false);
        mLinedEditText.setCursorVisible(false);
        mLinedEditText.clearFocus();
    }


    /**
     * Save the note into database if new, otherwise update and save
     */
    private void saveChanges() {
        if (mIsNewNote) {
            mNoteRepository.insertNoteTask(mFinalNote);
        } else {
            updateNote();
        }
    }

    /**
     * Save the updated note into the database
     */
    private void updateNote() {
        mNoteRepository.updateNoteTask(mFinalNote);
    }


    /**
     * Enable Edit Mode if the user double taps on the screen
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setDoubleTapListener() {
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                enableEditMode();
                return true;
            }
        });

        mLinedEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });
    }


    /**
     * Handle the clicks for various Toolbar items
     *
     * @param view Represents the Toolbar view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            // Hide keyboard and disable Edit Mode when Tick mark is clicked
            case R.id.toolbar_check: {
                hideSoftKeyboard();
                disableEditMode();
                break;
            }

            // Enable Edit Mode when Title of the note is clicked
            case R.id.note_text_title: {
                enableEditMode();
                mEditTitle.requestFocus();
                mEditTitle.setSelection(mEditTitle.length());
                break;
            }

            // Close the NoteActivity and start the activity transition animation.
            case R.id.toolbar_back_arrow: {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            }
        }
    }

    /**
     * If user is in Edit Mode, clicking the Back button denotes completion of editing and
     * changes the mode to View Mode, otherwise the mode was View Mode and Back button
     * brings the user to NoteListActivity
     */
    @Override
    public void onBackPressed() {
        if (mMode == EDIT_MODE_ENABLED) {
            onClick(mCheck);
        } else {
            // super.onBackPressed();    // Commented inorder to prevent incorrect transition animations

            // Start a new NoteListActivity instead of going back to the one already in back stack.
            // NOTE: Used 'singleTop' launch mode to prevent activity misuse.
            Intent intent = new Intent(NoteActivity.this, NotesListActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    /**
     * Save the current Mode, the user is currently in, to handle lifecycle changes
     *
     * @param outState Bundle to store data
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_MODE, mMode);
    }

    /**
     * Restore the Mode, the user was working in, to handle lifecycle changes
     *
     * @param savedInstanceState Bundle to retrieve data from
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMode = savedInstanceState.getInt(KEY_CURRENT_MODE);
        if (mMode == EDIT_MODE_ENABLED) {
            enableEditMode();
        }
    }
}
