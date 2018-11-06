package com.example.candidatescorner.details;


import java.util.ArrayList;

import android.os.Bundle;
import android.content.Intent;
import androidx.fragment.app.FragmentActivity;

import com.example.candidatescorner.R;
import com.example.candidatescorner.listing.model.CandidateParcelable;

/**
 * Created by chevelle on 11/5/17.
 */

public class CandidateDetailsActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.candidate_detail_view);

        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                .add(R.id.candidateViewer, new CandidateDetailsFragment())
                .commit();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        String candidateToView = null;
        Intent detailIntent = getIntent();
        ArrayList<CandidateParcelable> parcelables = null;
        String detailKey = getString(R.string.candidates_detail_key);
        String selectedKey = getString(R.string.candidate_selected_key);

        if (detailIntent != null) {
            candidateToView = detailIntent.getStringExtra(selectedKey);
            parcelables = detailIntent.getParcelableArrayListExtra(detailKey);

            showCandidateDetails(candidateToView, parcelables);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void showCandidateDetails(String candidateToView,
                    ArrayList<CandidateParcelable> allOfficeCandidates) {

        CandidateDetailsFragment detailsFragment =
            (CandidateDetailsFragment)getSupportFragmentManager()
                .findFragmentById(R.id.candidateViewer);

        detailsFragment.loadCandidateDetails(candidateToView, allOfficeCandidates);
    }

}
