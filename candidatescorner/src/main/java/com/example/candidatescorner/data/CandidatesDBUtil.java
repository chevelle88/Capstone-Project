package com.example.candidatescorner.data;

import android.net.Uri;

import java.util.Calendar;

public class CandidatesDBUtil {

    // Candidates Authority & URI
    public static final String CANDIDATES_AUTHORITY = "com.example.candidatescorner.provider";
    public static final String CANDIDATES_TABLE = "candidates";

    // Candidates Table Fields
    public static final String ID_FLD = "_ID";
    public static final String FIRST_NAME_FLD = "first_name";
    public static final String LAST_NAME_FLD = "last_name";
    public static final String OFFICE_FLD = "office";
    public static final String PHOTO_URL_FLD = "photo_url";
    public static final String PROFILE_FLD = "profile";
    public static final String ELECTION_YEAR_FLD = "election_year";
    public static final String ELECTED_FLD = "elected";
    public static final String CREATED_DATE_FLD = "created_date";
    public static final String MODIFIED_DATE_FLD = "modified_date";

    // Number years in a election cycle
    private static final int ELECTION_CYCLE = 2;

    private static final String [] candidateListCols = new String[] { ID_FLD,
            FIRST_NAME_FLD, LAST_NAME_FLD, OFFICE_FLD, PHOTO_URL_FLD, PROFILE_FLD };

    public static String getElectionYear() {
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        boolean electionYear = ((curYear % ELECTION_CYCLE) == 0);

        if (!electionYear) {
            ++curYear;
        }

        return Integer.toString(curYear);
    }

    public static String [] getCandidateListCols() {
        return candidateListCols;
    }

    public static Uri getCandidatesUri() {
        Uri contentUri = new Uri.Builder().scheme("content").authority(CANDIDATES_AUTHORITY)
                .appendPath(CANDIDATES_TABLE).build();

        return contentUri;
    }
}
