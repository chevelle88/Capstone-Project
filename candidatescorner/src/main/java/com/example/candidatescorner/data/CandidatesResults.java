package com.example.candidatescorner.data;

import java.util.List;
import java.util.ArrayList;

import android.database.Cursor;

import com.example.candidatescorner.listing.model.Candidate;

public class CandidatesResults {

    public static List<Candidate> processResults(Cursor results) {

        if ((results == null) || (results.getCount() == 0)) {
            return new ArrayList<Candidate>();
        }

        int columnIdx;
        Candidate candidate = null;
        List<Candidate> candidates = new ArrayList<>();
        String [] dbColumns = CandidatesDBUtil.getCandidateListCols();


        while (results.moveToNext()) {
            candidate = new Candidate();

            // Populate candidate data.
            for (String dbColumn : dbColumns) {
                columnIdx = results.getColumnIndex(dbColumn);

                if (dbColumn.equals(CandidatesDBUtil.ID_FLD)) {
                    candidate.setId(results.getInt(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.FIRST_NAME_FLD)) {
                    candidate.setFirstName(results.getString(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.LAST_NAME_FLD)) {
                    candidate.setLastName(results.getString(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.OFFICE_FLD)) {
                    candidate.setOffice(results.getString(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.PHOTO_URL_FLD)) {
                    candidate.setPhotoUrl(results.getString(columnIdx));
                }

                if (dbColumn.equals(CandidatesDBUtil.PROFILE_FLD)) {
                    candidate.setProfile(results.getString(columnIdx));
                }
            }

            candidates.add(candidate);
        }

        return candidates;
    }
}
