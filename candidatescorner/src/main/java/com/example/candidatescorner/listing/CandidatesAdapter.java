package com.example.candidatescorner.listing;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.support.v7.widget.RecyclerView;

import com.example.candidatescorner.R;
import com.example.candidatescorner.data.CandidatesDBUtil;
import com.example.candidatescorner.listing.model.Candidate;

public class CandidatesAdapter extends RecyclerView.Adapter<CandidatesView> {

    private Context context;
    private LayoutInflater layoutInflater;

    private String [] dbColumns;
    private List<Candidate> candidates;

    public CandidatesAdapter(Context context) {
        this.context = context;
        layoutInflater = ((Activity)context).getLayoutInflater();

        candidates = new ArrayList<>();
        dbColumns = CandidatesDBUtil.getCandidateListCols();
    }

    public void loadCandidates(Cursor cursor) {
        int columnIdx;
        Candidate candidate = null;

        while (cursor.moveToNext()) {
            candidate = new Candidate();

            // Populate candidate data.
            for (String dbColumn : dbColumns) {
                columnIdx = cursor.getColumnIndex(dbColumn);

                if (dbColumn.equals(CandidatesDBUtil.ID_FLD)) {
                    candidate.setId(cursor.getInt(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.FIRST_NAME_FLD)) {
                    candidate.setFirstName(cursor.getString(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.LAST_NAME_FLD)) {
                    candidate.setLastName(cursor.getString(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.OFFICE_FLD)) {
                    candidate.setOffice(cursor.getString(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.PHOTO_URL_FLD)) {
                    candidate.setPhotoUrl(cursor.getString(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.PROFILE_FLD)) {
                    candidate.setProfile(cursor.getString(columnIdx));
                }
            }

            candidates.add(candidate);
        }
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

        candidatesView.setCandidate(context, candidate);
    }

    @Override
    public int getItemCount() {
        return candidates.size();
    }
}
