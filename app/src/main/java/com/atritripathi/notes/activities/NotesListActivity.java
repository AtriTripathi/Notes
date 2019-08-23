package com.atritripathi.notes.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.atritripathi.notes.R;
import com.atritripathi.notes.adapters.NotesRecyclerAdapter;
import com.atritripathi.notes.models.Note;
import com.atritripathi.notes.persistence.NoteRepository;
import com.atritripathi.notes.utils.VerticalSpacingItemDecorator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotesListActivity extends AppCompatActivity implements
        NotesRecyclerAdapter.OnNoteClickListener,
        NotesRecyclerAdapter.OnNoteLongClickListener, View.OnClickListener {

    private static final String TAG = "NotesListActivity";
    public static final int NUM_COLUMNS = 2;
    public static final String KEY_DAY_NIGHT_TOGGLE = "com.atritripathi.notes.day.night.toggle";

    // UI components
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;

    // Variables
    private ArrayList<Note> mNotes = new ArrayList<>();
    private NotesRecyclerAdapter mNotesRecyclerAdapter;
    private NoteRepository mNoteRepository;
    private SharedPreferences mSharedPref;
    private ShareActionProvider mShareActionProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Persisting the current Day/Night mode.
        mSharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int defaultMode = AppCompatDelegate.getDefaultNightMode();
        if (mSharedPref.getInt(KEY_DAY_NIGHT_TOGGLE, defaultMode) == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setContentView(R.layout.activity_notes_list);
        setSupportActionBar((Toolbar) findViewById(R.id.notes_toolbar));
        setTitle("Notes");

        mNoteRepository = new NoteRepository(this);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.recyclerView);
        initRecyclerView();
        retrieveNotes();
    }

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


    private void initRecyclerView() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new
                StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(10);
        mRecyclerView.addItemDecoration(itemDecorator);
//        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mNotesRecyclerAdapter = new NotesRecyclerAdapter(mNotes, this, this);
        mRecyclerView.setAdapter(mNotesRecyclerAdapter);

    }


    @Override
    public void onNoteClick(int position) {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(NoteActivity.KEY_SELECTED_NOTE, mNotes.get(position));
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Used by FAB
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, NoteActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

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

            @Override
            public boolean onQueryTextChange(String newText) {
                getResults(newText);
                return true;
            }

            private void getResults(String query) {
                String queryText = ("%" + query + "%").trim();
                mNoteRepository.searchQuery(queryText).observe(NotesListActivity.this, new Observer<List<Note>>() {
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
            menu.findItem(R.id.night_mode).setTitle(R.string.day_mode);
        } else {
            menu.findItem(R.id.night_mode).setTitle(R.string.menu_night_mode);
        }


//        MenuItem shareItem = menu.findItem(R.id.menu_share);
//        ShareActionProvider shareActionProvider =
//                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.night_mode: {
                // Get the night mode state of the app.
                int nightMode = AppCompatDelegate.getDefaultNightMode();

                //Set the theme mode for the restarted activity
                if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }

                closeOptionsMenu();

                // Recreate the activity for the theme change to take effect.
                recreate();

                int currentMode = AppCompatDelegate.getDefaultNightMode();
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putInt(KEY_DAY_NIGHT_TOGGLE, currentMode);
                editor.apply();
                break;
            }

            case R.id.about: {
                showAboutDialog();
                break;
            }

            case R.id.menu_share: {
                showSharePicker();
            }
        }
        return true;
    }

    @Override
    public void onNoteLongClick(final int position) {
        AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
        deleteAlert.setTitle("Alert");
        deleteAlert.setMessage("Are you sure to delete note?");
        deleteAlert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mNoteRepository.deleteNoteTask(mNotes.get(position));
//                mNotes.remove(position);
                mNotesRecyclerAdapter.notifyItemRemoved(position);
//                mNotesRecyclerAdapter.notifyItemRangeRemoved(position, mNotesRecyclerAdapter.getItemCount());
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

    @Override
    protected void onResume() {
        super.onResume();
        runLayoutAnimation(mRecyclerView);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void showAboutDialog() {

        /*
        aboutDialog.setTitle("About");
        aboutDialog.setMessage("Developed by Atri Tripathi\n\nVersion: 1.0 ");
        aboutDialog.set
        aboutDialog.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        aboutDialog.show();
        */

        Dialog aboutDialog = new Dialog(this, R.style.AboutDialogTheme);
        aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        aboutDialog.setContentView(R.layout.layout_about_dialog);
        aboutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        aboutDialog.show();
    }

    private void showSharePicker() {
        String shareBody = "Install Minimal Notes, now.\n"
                + "\nhttp://play.google.com/store/apps/details?id=com.atritripathi.notes";
        String shareSubject = "Install Minimal Notes";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share Minimal Notes via"));
    }
}
