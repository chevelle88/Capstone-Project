package com.example.candidatescorner;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.candidatescorner.data.CandidatesDBUtil;
import com.example.candidatescorner.service.CandidatesService;
import com.example.candidatescorner.listing.CandidatesAdapter;

public class MainActivity extends AppCompatActivity
        implements LoaderCallbacks<Cursor> {

    /*
    NOTE: Use the common intent for email to send a message to the chair
    if a member is interesting in running for office
     */
    // Candidates Menu Options
    private static final int ALL_CANDIDATES = 10;
    private static final int TOP_RATED_CANDIDATES = 11;

    private int candidatesOption;

    private TextView emptySlate;
    private RecyclerView candidates;
    private CandidatesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         1. Start service to get candidates (Done)
         2. setup the menu (Done)
         3. setup shared preferences
         4. setup and start loader (Done)
         */

        Intent intent = new Intent(this, CandidatesService.class);
        startService(intent);

        Toolbar toolbar = (Toolbar) findViewById(R.id.aacdstToolbar);
        setSupportActionBar(toolbar);

        candidatesOption = ALL_CANDIDATES;

        emptySlate = (TextView) findViewById(R.id.emptySlate);

        adapter = new CandidatesAdapter(this);

        candidates = (RecyclerView) findViewById(R.id.candidates_list);
        candidates.setLayoutManager(new LinearLayoutManager(this));
        candidates.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Use this to enable or disable the options menu if there are any ratings

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean selected = true;

        switch(item.getItemId()) {
            case R.id.allCandidates:
            case R.id.topCandidates:
                int selectedOption = (item.getItemId() == R.id.allCandidates) ? ALL_CANDIDATES
                        : TOP_RATED_CANDIDATES;

                if (selectedOption != candidatesOption) {
                    candidatesOption = selectedOption;
                    getLoaderManager().restartLoader(0, null, this);
                }

                break;

            case R.id.nominee:
                showRunForOfficeDialog();
                break;

            default:
                selected = super.onOptionsItemSelected(item);
                break;
        }

        return selected;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = null;

        if (candidatesOption == TOP_RATED_CANDIDATES) {
            selection = buildTopRatedCandidatesClause();
        }

        return new CursorLoader(this, CandidatesDBUtil.getCandidatesUri(),
                CandidatesDBUtil.getCandidateListCols(), selection, null,
                CandidatesDBUtil.LAST_NAME_FLD);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if ((data != null) && data.getCount() > 0) {
            emptySlate.setVisibility(View.GONE);
            candidates.setVisibility(View.VISIBLE);

            adapter.loadCandidates(data);
        }
        else {
            candidates.setVisibility(View.GONE);
            emptySlate.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.clearCandidates();
    }

    private void showRunForOfficeDialog() {
        // Display run for office dialog
    }

    private String buildTopRatedCandidatesClause() {
        // Use the shared preferences to get top rated candidates
        return null;
    }
}
