package com.example.candidatescorner.listing;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;

import com.example.candidatescorner.R;
import com.example.candidatescorner.MainActivity;
import com.example.candidatescorner.listing.model.Candidate;
import com.example.candidatescorner.details.CandidateDetailsActivity;
import com.example.candidatescorner.listing.model.CandidateParcelable;
import com.example.candidatescorner.listing.CandidatesAdapter;

/**
 * Created by chevelle on 12/9/17.
 */

public class CandidateListener implements View.OnClickListener {

    private CandidatesView candidatesView;

    public CandidateListener(CandidatesView candidatesView) {
        this.candidatesView = candidatesView;
    }

    @Override
    public void onClick(View view) {
        Intent detailsIntent = null;
        String candidateToView = null;
        ArrayList<CandidateParcelable> candidates = null;
        Context context = candidatesView.getContext();
        Candidate candidate = candidatesView.getCandidate();
        CandidatesAdapter adapter = candidatesView.getAdapter();
        String dataKey = context.getString(R.string.candidates_detail_key);
        String selectedKey = context.getString(R.string.candidate_selected_key);
        boolean multiPaned = ((MainActivity) context).isMultiPaned();

        // Add the selected candidate to the Intent instance.
        candidateToView = candidate.getFirstName() + " " + candidate.getLastName();


        // Add candidates who are running detail to the Intent instance.
        candidates = getOfficeCandidates(candidate.getOffice(), adapter);


        // Get the intent to start the details activity
        if (multiPaned) {
            ((MainActivity) context).onCandidateSelected(candidateToView, candidates);
        }
        else {
            detailsIntent = new Intent(context, CandidateDetailsActivity.class);
            detailsIntent.putExtra(selectedKey, candidateToView);
            detailsIntent.putParcelableArrayListExtra(dataKey, candidates);

            context.startActivity(detailsIntent);
        }

    }

    private ArrayList<CandidateParcelable> getOfficeCandidates(String office, CandidatesAdapter adapter) {
        ArrayList<CandidateParcelable> parcelables = new ArrayList<CandidateParcelable>();
        List<Candidate> candidates = adapter.findOfficeCandidates(office);

        for (Candidate candidate : candidates) {
            parcelables.add(new CandidateParcelable(candidate));
        }

        return parcelables;
    }
}
