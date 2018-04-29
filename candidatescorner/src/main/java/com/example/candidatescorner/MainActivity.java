package com.example.candidatescorner;

import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import com.example.candidatescorner.data.CandidatesDBUtil;
import com.example.candidatescorner.listing.model.CandidateParcelable;
import com.example.candidatescorner.service.CandidatesService;
import com.example.candidatescorner.listing.CandidatesFragment;
import com.example.candidatescorner.details.CandidateDetailsFragment;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>,
        CandidatesFragment.CandidateSelectedListener,
        RunForOfficeFragmentDialog.RunForOfficeDialogListener {

    /*
    NOTE: Use the common intent for email to send a message to the chair
    if a member is interesting in running for office
     */
    // Candidates Menu Options
    private static final int ALL_CANDIDATES = 10;
    private static final int TOP_RATED_CANDIDATES = 11;

    // Minimum Top Candidate Rating
    private static final float TOP_MIN_RATING = 3.3f;

    // Current Candidate Menu Option
    private int candidatesOption;

    private boolean multiPaned;
    private SharedPreferences ratings;
    private List<String> topRatedCandidates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent;
        String key;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Start service to get all candidates.
        intent = new Intent(this, CandidatesService.class);
        startService(intent);

        // Create top-rated list;
        topRatedCandidates = new ArrayList<>();


        // Set view status.
        setMultiPanedState();

        // Load the candidates fragment.
        if (savedInstanceState == null) {

            // Set default menu option.
            candidatesOption = ALL_CANDIDATES;

            // Add user interface if this is a single view.
            if (!multiPaned) {
                getSupportFragmentManager().beginTransaction()
                    .add(R.id.mainContainer, new CandidatesFragment())
                    .commit();
            }
        }
        else {
            key = getString(R.string.candidates_menu_key);
            candidatesOption = savedInstanceState.getInt(key, ALL_CANDIDATES);
        }

        // Configure the preference file, CandidatesRatings.
        configureCandidateRatingsPrefs();

    }

    @Override
    public void onResume() {
        super.onResume();

        invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String key = getString(R.string.candidates_menu_key);

        outState.putInt(key, candidatesOption);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem topRated = menu.findItem(R.id.topCandidates);

        topRated.setEnabled(hasTopCandidates());

        return super.onPrepareOptionsMenu(menu);
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
                    getSupportLoaderManager().restartLoader(0, null, this);
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
        CandidatesFragment candidatesFrag = getCandidatesListingView();

        candidatesFrag.showCandidatesListing(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        CandidatesFragment candidatesFrag = getCandidatesListingView();

        candidatesFrag.resetCandidatesListing();
    }

    @Override
    public void onCandidateSelected(String candidateToView,
                        ArrayList<CandidateParcelable> officeCandidates) {

        CandidateDetailsFragment detailsFrag = (CandidateDetailsFragment)getSupportFragmentManager()
                .findFragmentById(R.id.candidateInfo);

        if (detailsFrag != null) {
            detailsFrag.loadCandidateDetails(candidateToView, officeCandidates);
        }
    }

    @Override
    public void onOfficeSelected(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onOfficeCancelled(DialogFragment dialog) {
        dialog.dismiss();
    }

    public boolean isMultiPaned() {
        return multiPaned;
    }

    private void showRunForOfficeDialog() {
        String dlgTag = getString(R.string.dlgTag);

        RunForOfficeFragmentDialog officeDlg = new RunForOfficeFragmentDialog();
        officeDlg.show(getSupportFragmentManager(), dlgTag);
    }

    private String buildTopRatedCandidatesClause() {
        List<Integer> ids = null;
        StringBuilder idsList = new StringBuilder();
        StringBuilder topRatedClause = new StringBuilder();
        String curElectionYear = CandidatesDBUtil.getElectionYear();
        CandidatesFragment candidatesFrag = getCandidatesListingView();

        ids = candidatesFrag.findCandidatesIds(topRatedCandidates);

        // Convert IDs to a comma-separated list.
        for (int idx = 0; idx < ids.size(); idx++) {

            if (idx > 0) {
                idsList.append(", ");
            }

            idsList.append(ids.get(idx));
        }

        // Build WHERE clause.
        topRatedClause.append("election_year = '" + curElectionYear + "' and ");
        topRatedClause.append("_ID in (" + idsList.toString() + ")");

        return topRatedClause.toString();
    }

    private void configureCandidateRatingsPrefs() {
        String storedElectionYear = null;
        SharedPreferences.Editor editor = null;
        String prefFile = getString(R.string.ratings_pref_file);
        String electionYearPref = getString(R.string.election_year);
        String curElectionYear = CandidatesDBUtil.getElectionYear();

        ratings = this.getSharedPreferences(prefFile, 0);
        storedElectionYear = ratings.getString(electionYearPref, null);
        editor = ratings.edit();

        if (storedElectionYear != null) {

            if (!storedElectionYear.equals(curElectionYear)) {
                removeCandidatesRatings(editor, electionYearPref);

                editor.putString(electionYearPref, curElectionYear);
                editor.commit();
            }
        }
        else {
            editor.putString(electionYearPref, curElectionYear);
            editor.commit();
        }
    }

    private void removeCandidatesRatings(SharedPreferences.Editor editor, String ignorePref) {
        Set<String> prefKeys = null;
        Map<String, ?> allPrefs = null;

        allPrefs = ratings.getAll();
        prefKeys = allPrefs.keySet();

        for (String prefKey : prefKeys) {

            if (prefKey.equals(ignorePref)) {
                continue;
            }

            editor.remove(prefKey);
        }
    }

    private boolean hasTopCandidates() {
        float rating;
        int totalTopCandidates = 0;
        Set<String> prefKeys = null;
        Map<String, ?> allPrefs = null;
        String electionYearPref = getString(R.string.election_year);

        allPrefs = ratings.getAll();
        prefKeys = allPrefs.keySet();

        topRatedCandidates.clear();

        for (String prefKey : prefKeys) {

            if (prefKey.equalsIgnoreCase(electionYearPref)) {
                continue;
            }

            rating = (Float) allPrefs.get(prefKey);

            if (rating >= TOP_MIN_RATING) {
                ++totalTopCandidates;

                topRatedCandidates.add(prefKey);
            }

        }

        return (totalTopCandidates > 0);
    }

    private void setMultiPanedState() {
        Fragment listFragment = getSupportFragmentManager()
                .findFragmentById(R.id.candidateListing);

        Fragment detailsFragment = getSupportFragmentManager()
                .findFragmentById(R.id.candidateInfo);

        multiPaned = ((listFragment != null) && (detailsFragment != null));
    }

    private CandidatesFragment getCandidatesListingView() {
        int fragId = (multiPaned) ? R.id.candidateListing : R.id.mainContainer;

        CandidatesFragment listingView = (CandidatesFragment) getSupportFragmentManager()
                    .findFragmentById(fragId);

        return listingView;
    }
}
