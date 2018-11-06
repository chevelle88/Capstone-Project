package com.example.candidatescorner;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import androidx.loader.content.Loader;
import androidx.loader.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;

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

    // Candidates Menu Options
    private static final int ALL_CANDIDATES = 10;
    private static final int TOP_RATED_CANDIDATES = 11;

    // Minimum Top Candidate Rating
    private static final float TOP_MIN_RATING = 3.3f;

    // Current Candidate Menu Option
    private int candidatesOption;

    private boolean multiPaned;
    private String savedIds;
    private int orientationState;
    private SharedPreferences candidatesPref;
    private List<String> topRatedCandidates;
    private boolean restartLoader = false;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;
        String key;
        int lastOrientationState;

        setContentView(R.layout.activity_main);

        // Start service to get all candidates.
        intent = new Intent(this, CandidatesService.class);
        startService(intent);

        // Create top-rated list;
        topRatedCandidates = new ArrayList<>();

        // Set view status.
        setMultiPanedState();

        orientationState = getResources().getConfiguration().orientation;

        // Load the candidates fragment.
        if (savedInstanceState == null) {

            Log.i(TAG,"has no savedInstanceState");
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

            Log.i(TAG,"has savedInstanceState");
            //Get current menu option.
            key = getString(R.string.candidates_menu_key);
            candidatesOption = savedInstanceState.getInt(key, ALL_CANDIDATES);

            if (candidatesOption == TOP_RATED_CANDIDATES) {
                key = getString(R.string.top_rated_ids);
                savedIds = savedInstanceState.getString(key);
            }

            // Get last orientation state.
            key = getString(R.string.last_orientation_state);
            lastOrientationState = savedInstanceState.getInt(key);

            // Set restartLoader based upon last orientation state.
            restartLoader = (lastOrientationState != orientationState);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Configure the preference file, CandidatesRatings.
        configureCandidateRatingsPrefs();

        invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String key = getString(R.string.candidates_menu_key);
        outState.putInt(key, candidatesOption);

        key = getString(R.string.last_orientation_state);
        outState.putInt(key, orientationState);

        if (candidatesOption == TOP_RATED_CANDIDATES) {
            savedIds = buildTopCandidateIdsList();

            key = getString(R.string.top_rated_ids);
            outState.putString(key, savedIds);
        }
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

            case R.id.signOut:
                signOut();
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
            detailsFrag.loadCandidateDetails(candidateToView, officeCandidates, isMultiPaned());
        }

    }

    @Override
    public void onOfficeSelected(DialogFragment dialog) {
        Intent emailIntent;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String emailTo = getString(R.string.email_to);
        String [] receivers = emailTo.split(",");
        String emailSubject = getString(R.string.email_subject);
        String selectedOffice = ((RunForOfficeFragmentDialog)dialog).getSelectedChapterOffice();
        String emailBody = getString(R.string.email_body,
                user.getDisplayName(), user.getEmail(), selectedOffice);
        String dlgMsg = getString(R.string.dlg_msg);

        // Build the email intent.
        emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, receivers);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
        emailIntent.setType("text/plain");


        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);

        }

        dialog.dismiss();

        Toast.makeText(this, dlgMsg,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOfficeCancelled(DialogFragment dialog) {
        dialog.dismiss();
    }

    public boolean isMultiPaned() {
        return multiPaned;
    }

    public boolean isRestartLoader() {
        Log.i(TAG, "restartLoader: " + restartLoader);
        return restartLoader;
    }

    public boolean detailsInMultiPanedLoaded() {
        boolean loaded = false;
        CandidateDetailsFragment detailsFrag;

        if (multiPaned) {
            detailsFrag = (CandidateDetailsFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.candidateInfo);

            loaded = detailsFrag.isDetailsRestored();
        }

        return loaded;
    }

    private void showRunForOfficeDialog() {
        String dlgTag = getString(R.string.dlgTag);

        RunForOfficeFragmentDialog officeDlg = new RunForOfficeFragmentDialog();
        officeDlg.show(getSupportFragmentManager(), dlgTag);
    }

    private String buildTopRatedCandidatesClause() {
        String idsInList;
        StringBuilder topRatedClause = new StringBuilder();
        String curElectionYear = CandidatesDBUtil.getElectionYear();

        idsInList = buildTopCandidateIdsList();

        // Build WHERE clause.
        topRatedClause.append("election_year = '" + curElectionYear + "' and ");
        topRatedClause.append("_ID in (" + idsInList + ")");

        return topRatedClause.toString();
    }

    private String buildTopCandidateIdsList() {
        List<Integer> ids;
        StringBuilder idsList = new StringBuilder();
        CandidatesFragment candidatesFrag = getCandidatesListingView();

        ids = candidatesFrag.findCandidatesIds(topRatedCandidates);

        if (ids == null) {
            return savedIds;
        }

        // Convert IDs to a comma-separated list.
        for (int idx = 0; idx < ids.size(); idx++) {

            if (idx > 0) {
                idsList.append(", ");
            }

            idsList.append(ids.get(idx));
        }

        return idsList.toString();
    }

    private void configureCandidateRatingsPrefs() {
        String storedElectionYear = null;
        SharedPreferences.Editor editor = null;
        String prefFile = getString(R.string.candidates_pref_file);
        String electionYearPref = getString(R.string.election_year);
        String curElectionYear = CandidatesDBUtil.getElectionYear();

        candidatesPref = this.getSharedPreferences(prefFile, 0);
        storedElectionYear = candidatesPref.getString(electionYearPref, null);
        editor = candidatesPref.edit();

        if (storedElectionYear != null) {

            if (!storedElectionYear.equals(curElectionYear)) {
                removeCandidatesRatings(editor);

                editor.putString(electionYearPref, curElectionYear);
                editor.commit();
            }
        }
        else {
            editor.putString(electionYearPref, curElectionYear);
            editor.commit();
        }
    }

    private void removeCandidatesRatings(SharedPreferences.Editor editor) {
        Set<String> prefKeys = null;
        Map<String, ?> allPrefs = null;
        String electionYearPref = getString(R.string.election_year);

        allPrefs = candidatesPref.getAll();
        prefKeys = allPrefs.keySet();

        for (String prefKey : prefKeys) {

            if (prefKey.equals(electionYearPref)) {
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

        allPrefs = candidatesPref.getAll();
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

    private void signOut() {

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        startActivity(new Intent(MainActivity.this,
                                MembersLoginActivity.class));
                        finish();
                    }
                });
    }

}
