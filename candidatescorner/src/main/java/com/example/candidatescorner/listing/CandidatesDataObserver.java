package com.example.candidatescorner.listing;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by chevelle on 7/1/18.
 */

class CandidatesDataObserver extends RecyclerView.AdapterDataObserver {

    private CandidatesFragment candidatesFrag;

    public void setCandidatesFragment(CandidatesFragment candidatesFrag) {
        this.candidatesFrag = candidatesFrag;
    }

    @Override
    public void onChanged() {
        candidatesFrag.showCandidateDetails();
    }
}
