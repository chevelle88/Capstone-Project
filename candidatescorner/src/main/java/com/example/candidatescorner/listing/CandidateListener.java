package com.example.candidatescorner.listing;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.candidatescorner.R;
import com.example.candidatescorner.listing.model.Candidate;
import com.example.candidatescorner.details.CandidateDetailsActivity;
import com.example.candidatescorner.listing.model.CandidateParcelable;

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
        Context context = candidatesView.getContext();
        Candidate candidate = candidatesView.getCandidate();
        String dataKey = context.getString(R.string.candidate_detail_key);
        Intent detailsIntent = new Intent(context, CandidateDetailsActivity.class);

        // Add the candidate's detail to the Intent instance.
        detailsIntent.putExtra(dataKey, new CandidateParcelable(candidate));

        // Get the intent to start the details activity
        context.startActivity(detailsIntent);
    }
}
