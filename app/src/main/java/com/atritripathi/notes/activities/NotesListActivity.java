/*
 * Copyright 2019 Atri Tripathi. All rights reserved.
 */

package com.atritripathi.notes.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.atritripathi.notes.R;
import com.atritripathi.notes.adapters.NotesRecyclerAdapter;
import com.atritripathi.notes.models.Note;
import com.atritripathi.notes.persistence.NoteRepository;
import com.atritripathi.notes.utils.TimestampUtil;
import com.atritripathi.notes.utils.VerticalSpacingItemDecorator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotesListActivity extends AppCompatActivity implements NotesRecyclerAdapter.OnNoteClickListener,
        NotesRecyclerAdapter.OnNoteLongClickListener, View.OnClickListener {

    // Constants
    public static final String KEY_FIRST_LAUNCH = "com.atritripathi.notes.first.launch";
    public static final String KEY_DAY_NIGHT_TOGGLE = "com.atritripathi.notes.day.night.toggle";
    public static final int NUM_COLUMNS = 2;

    // UI components
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;

    // Variables
    private ArrayList<Note> mNotes = new ArrayList<>();
    private NotesRecyclerAdapter mNotesRecyclerAdapter;
    private NoteRepository mNoteRepository;
    private SharedPreferences mSharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For persisting the current mode set by user: Day/Night.
        mSharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int defaultMode = AppCompatDelegate.getDefaultNightMode();
        if (mSharedPref.getInt(KEY_DAY_NIGHT_TOGGLE, defaultMode) == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // This is called here, to allow loading of dummy notes during the first app launch
        mNoteRepository = new NoteRepository(this);

        checkFirstLaunch();

        setContentView(R.layout.activity_notes_list);
        setSupportActionBar((Toolbar) findViewById(R.id.notes_toolbar));
        setTitle("Notes");

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.recyclerView);
        initRecyclerView();
        retrieveNotes();
        runLayoutAnimation(mRecyclerView);
    }

    /**
     * Load dummy welcome notes, when the app is launched for the first time on a device
     */
    private void checkFirstLaunch() {
        boolean isFirstLaunch = mSharedPref.getBoolean(KEY_FIRST_LAUNCH, true);
        if (isFirstLaunch) {

            String[] dummyNoteTitles = getResources().getStringArray(R.array.dummy_note_titles);
            String[] dummyNoteContents = getResources().getStringArray(R.array.dummy_note_contents);
            List<Note> dummyNotes = new ArrayList<>();

            for (int i = dummyNoteTitles.length - 1; i >= 0; i--) {
                Note note = new Note();
                note.setTitle(dummyNoteTitles[i]);
                note.setContent(dummyNoteContents[i]);
                note.setTimeStamp(TimestampUtil.getCurrentTimestamp());
                dummyNotes.add(note);
            }
            mNoteRepository.insertNoteTask(dummyNotes.toArray(new Note[0]));
            mSharedPref.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        }
    }

    /**
     * Setup and initialise the RecyclerView with StaggeredGridLayout
     */
    private void initRecyclerView() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new
                StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(10);
        mRecyclerView.addItemDecoration(itemDecorator);

        // To be implemented later, if time permits...
        // new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        mNotesRecyclerAdapter = new NotesRecyclerAdapter(mNotes, this, this);
        mRecyclerView.setAdapter(mNotesRecyclerAdapter);
    }

    /**
     * Utility method to retrieve list of notes (if they exist) from the database.
     */
    private void retrieveNotes() {
        mNoteRepository.retrieveNotesTask().observe(NotesListActivity.this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                if (mNotes.size() > 0) {
                    mNotes.clear();
                }
                if (notes != null) {
                    mNotes.addAll(notes);
                }
                mNotesRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Inflate the Options overflow menu and initialise any fields as required
     *
     * @param menu The actual overflow menu
     * @return True if successfully inflated, false otherwise
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Search your notes");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // Start querying the database as soon as the text changes
            @Override
            public boolean onQueryTextChange(String newText) {
                getResults(newText);
                return true;
            }

            // Since I've wrapped the Note objects with LiveData, I can directly perform
            // a search query in the database, and get the filtered results.
            private void getResults(String query) {
                String queryText = ("%" + query + "%").trim();
                mNoteRepository.searchQuery(queryText)
                        .observe(NotesListActivity.this, new Observer<List<Note>>() {
                            @Override
                            public void onChanged(List<Note> notes) {
                                if (notes != null) {
                                    mNotes.clear();
                                    mNotes.addAll(notes);
                                    mNotesRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        });
            }
        });

        // Change the label of the menu based on the state of the app.
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            menu.findItem(R.id.menu_item_night_mode).setTitle(R.string.day_mode);
        } else {
            menu.findItem(R.id.menu_item_night_mode).setTitle(R.string.menu_night_mode);
        }

        return true;
    }

    /**
     * Click handler when any option in the overflow menu is selected.
     *
     * @param item The actual item in the list, clicked by the user.
     * @return True if successfully clicked an item, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_night_mode: {
                showSearchBar();
                break;
            }

            case R.id.menu_item_about: {
                showAboutDialog();
                break;
            }

            case R.id.menu_item_share: {
                showSharePicker();
                break;
            }

            case R.id.menu_item_exit: {
                finishAffinity();
                break;
            }
        }
        return true;
    }

    /**
     * Utility method to show Search bar in Toolbar when its icon is clicked
     */
    private void showSearchBar() {
        // Get the night mode state of the app.
        int nightMode = AppCompatDelegate.getDefaultNightMode();

        //Set the theme mode for the restarted activity
        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Important: closed the menu to avoid memory leaks
        closeOptionsMenu();

        // Recreate the activity for the theme change to take effect.
        recreate();

        // Save the current mode in SharedPref for future use.
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(KEY_DAY_NIGHT_TOGGLE, currentMode);
        editor.apply();
    }

    /**
     * Utility method to show About dialog with a custom designed view.
     */
    private void showAboutDialog() {
        Dialog aboutDialog = new Dialog(this, R.style.AboutDialogTheme);
        aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        aboutDialog.setContentView(R.layout.layout_about_dialog);
        aboutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        aboutDialog.show();
    }

    /**
     * Utility method to show 'Share via' bottom sheet to share the data with other apps
     */
    private void showSharePicker() {
        String shareBody = "Notes - A clean and minimal note taking app.\n\nInstall now!\n"
                + "\nhttp://bit.ly/notesplaystore";
        String shareSubject = "Notes - A clean and minimal note taking app.";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share Notes via"));
    }


    /**
     * Click handler used by FAB which simply launches NoteActivity to allow adding a new note.
     *
     * @param view The view used by FAB
     */
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, NoteActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Click handler for notes, which launches the NoteActivity and loads it with that note's data.
     *
     * @param position The position of the note in the list
     */
    @Override
    public void onNoteClick(int position) {
        int actualPosition = mNotes.size() - (position + 1);

        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(NoteActivity.KEY_SELECTED_NOTE, mNotes.get(actualPosition));
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    /**
     * Click handler to allow note deletion when long pressed on any note.
     *
     * @param position The position of the note in the list.
     */
    @Override
    public void onNoteLongClick(final int position) {
        AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
        deleteAlert.setTitle(Html.fromHtml("<b>" + "Alert" + "</b>"));
        deleteAlert.setMessage("Are you sure to delete note?");
        deleteAlert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Need to get the actual position because we are showing the list
                // in reverse, so that the latest added note is shown at the top.
                int actualPosition = mNotes.size() - (position + 1);
                mNoteRepository.deleteNoteTask(mNotes.get(actualPosition));
                mNotesRecyclerAdapter.notifyItemRemoved(actualPosition);
            }
        });
        deleteAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        deleteAlert.show();
    }


    /**
     * Utility method to run the RecyclerView animation on demand.
     *
     * @param recyclerView The instance of the RecyclerView in use.
     */
    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.recycler_layout_animation);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    /**
     * Implemented this to completely remove the app from the background task,
     * when back is pressed.
     */
    @Override
    public void onBackPressed() {
        finishAffinity();
    }

}




// EXPERIMENTAL FEATURES: (May contain bugs)
/*
-----------------------------------------------------------------------------------------------
// I may use this if I want to implement Swipe to Delete and Drag features in the RecyclerView

//    private void deleteNote(Note note) {
//        mNotes.remove(note);
//        mNotesRecyclerAdapter.notifyDataSetChanged();
//        mNoteRepository.deleteNoteTask(note);
//    }

//    private ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper
//            .SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//        @Override
//        public boolean onMove(@NonNull RecyclerView recyclerView,
//                              @NonNull RecyclerView.ViewHolder viewHolder,
//                              @NonNull RecyclerView.ViewHolder target) {
////            int from = viewHolder.getAdapterPosition();
////            int to = target.getAdapterPosition();
////
////            Collections.swap(mNotes, from, to);
////            mNotesRecyclerAdapter.notifyItemMoved(from, to);
//            return false;
//        }

//        @Override
//        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//            int position = viewHolder.getAdapterPosition();
//            deleteNote(mNotes.get(position));
//            mNotesRecyclerAdapter.notifyItemRemoved(position);
//        }
//    };

-----------------------------------------------------------------------------------------------
*/

