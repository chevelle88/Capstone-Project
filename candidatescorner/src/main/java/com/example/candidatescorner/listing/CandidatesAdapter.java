package com.example.candidatescorner.listing;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candidatescorner.R;
import com.example.candidatescorner.data.CandidatesResults;
import com.example.candidatescorner.listing.model.Candidate;

public class CandidatesAdapter extends RecyclerView.Adapter<CandidatesView> {

    private Context context;
    private LayoutInflater layoutInflater;

    private List<Candidate> candidates;

    public CandidatesAdapter(Context context) {
        this.context = context;
        layoutInflater = ((Activity)context).getLayoutInflater();
    }

    public void loadCandidates(Cursor cursor) {
        candidates = CandidatesResults.processResults(cursor);
    }

    public void clearCandidates() {
        candidates.clear();
    }

    @Override
    public CandidatesView onCreateViewHolder(ViewGroup parent, int viewType) {
        View candidateView = layoutInflater.inflate(R.layout.candidates_list_view,
                parent, false);

        return new CandidatesView(candidateView);
    }

    @Override
    public void onBindViewHolder(CandidatesView candidatesView, int position) {
        Candidate candidate = candidates.get(position);

        candidatesView.setCandidate(context, candidate, this);
    }

    @Override
    public int getItemCount() {
        int totalCandidates = (candidates != null) ? candidates.size() : 0;

        return totalCandidates;
    }

    public List<Candidate> findOfficeCandidates(String office) {
        String candidateOffice;
        List<Candidate> others = new ArrayList<>();

        for (Candidate candidate : candidates) {
            candidateOffice = candidate.getOffice();

            if (candidateOffice.equals(office)) {
                others.add(candidate);
            }
        }

        return others;
    }

    public List<Integer> getCandidatesIds(List<String> members) {
        String name;
        List<Integer> ids = new ArrayList<>();

        if (candidates == null) {
            return null;
        }

        for (Candidate candidate : candidates) {
            name = candidate.getFirstName() + " " + candidate.getLastName();

            for (String member : members) {

                if (name.equalsIgnoreCase(member)) {
                    ids.add(candidate.getId());
                    break;
                }
            }
        }

        return ids;
    }

    public Candidate findCandidate(int candidateId) {

        if (candidateId == RecyclerView.NO_ID) {
            return candidates.get(0);
        }

        Candidate candidate = null;

        for (int idx = 0; idx < candidates.size(); idx++) {
            candidate = candidates.get(idx);

            if (candidate.getId() == candidateId) {
                break;
            }
        }


        return candidate;
    }

}
