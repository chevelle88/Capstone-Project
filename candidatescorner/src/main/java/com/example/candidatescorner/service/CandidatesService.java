package com.example.candidatescorner.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.ContentValues;
import android.net.Uri;

import java.io.Reader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;

import com.example.candidatescorner.R;
import com.example.candidatescorner.data.CandidatesDBUtil;

import static com.example.candidatescorner.data.CandidatesDBUtil.FIRST_NAME_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.LAST_NAME_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.OFFICE_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.ELECTION_YEAR_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.PHOTO_URL_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.PROFILE_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.ELECTED_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.CREATED_DATE_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.MODIFIED_DATE_FLD;

public class CandidatesService extends IntentService {

    private static final String CANDIDATES_JSON_KEY = "candidatess";
    private static final String CANDIDATE_UPDATE_CLAUSE =
            "first_name = ? and last_name = ? and election_year = ? and modified_date < ?";

    private List<ContentValues> candidatesToAdd;
    private List<ContentValues> candidatesToUpdate;

    public CandidatesService() {
        super("CandidatesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Request request;
        Response results = null;
        OkHttpClient client = new OkHttpClient();
        HttpUrl restSqlUrl = buildRestSqlUrl();
        String acceptedType = getString(R.string.acceptedContentType);

        try {

            request = new Request.Builder().url(restSqlUrl)
                    .addHeader("Accept", acceptedType).build();

            results = client.newCall(request).execute();

            if (results.isSuccessful()) {
                parseResults(results.body().charStream());
                addCandidates();
                updateCandidates();
            }

        }
        catch (Exception anyErr) { }
        finally {
            if (results != null) {
                results.close();
            }
        }
    }

    private HttpUrl buildRestSqlUrl() {
        String electionYr = CandidatesDBUtil.getElectionYear();
        String restSqlHost = getString(R.string.restSqlHost);
        String restSqlPath = getString(R.string.restSqlPath);

        HttpUrl restSqlUrl = new HttpUrl.Builder()
                .scheme("http")
                .host(restSqlHost)
                .addPathSegments(restSqlPath)
                .addQueryParameter(CandidatesDBUtil.ELECTION_YEAR_FLD, electionYr)
                .build();

        return restSqlUrl;
    }

    private void addCandidates() {
        Uri candidatesUri = CandidatesDBUtil.getCandidatesUri();

        if (!candidatesToAdd.isEmpty()) {
            ContentValues[] rows = new ContentValues[candidatesToAdd.size()];
            rows = candidatesToAdd.toArray(rows);

            getContentResolver().bulkInsert(candidatesUri, rows);
        }

    }

    private void updateCandidates() {
        String [] args;
        ContentValues updates = new ContentValues();
        Uri candidatesUri = CandidatesDBUtil.getCandidatesUri();

        for (ContentValues candidate : candidatesToUpdate) {

            // Add fields to update.
            updates.put(OFFICE_FLD, candidate.getAsString(OFFICE_FLD));
            updates.put(PROFILE_FLD, candidate.getAsString(PROFILE_FLD));
            updates.put(ELECTED_FLD, candidate.getAsInteger(ELECTED_FLD));
            updates.put(MODIFIED_DATE_FLD, candidate.getAsString(MODIFIED_DATE_FLD));

            if (candidate.containsKey(PHOTO_URL_FLD)) {
                updates.put(PHOTO_URL_FLD, candidate.getAsString(PHOTO_URL_FLD));
            }

            // Create arguments list for WHERE clause.
            args = new String[] { candidate.getAsString(FIRST_NAME_FLD),
                    candidate.getAsString(LAST_NAME_FLD),
                    candidate.getAsString(ELECTION_YEAR_FLD),
                    candidate.getAsString(MODIFIED_DATE_FLD)};

            getContentResolver().update(candidatesUri, updates, CANDIDATE_UPDATE_CLAUSE, args);

            updates.clear();
        }
    }

    private void parseResults(Reader reader) throws Exception {
        boolean elected;
        JSONObject row = null;
        String createDate = null;
        ContentValues candidate = null;
        StringBuilder jsonResults = readResults(reader);

        JSONObject jsonObj = new JSONObject(jsonResults.toString());
        JSONArray rows = jsonObj.getJSONArray(CANDIDATES_JSON_KEY);
        int totalRows = rows.length();

        candidatesToAdd = new ArrayList<ContentValues>();
        candidatesToUpdate = new ArrayList<ContentValues>();

        if (totalRows > 0) {
            for (int idx = 0; idx < totalRows; idx++) {
                row = rows.getJSONObject(idx);

                candidate = new ContentValues();
                candidate.put(ELECTION_YEAR_FLD, row.getString(ELECTION_YEAR_FLD));
                candidate.put(FIRST_NAME_FLD, row.getString(FIRST_NAME_FLD));
                candidate.put(LAST_NAME_FLD, row.getString(LAST_NAME_FLD));
                candidate.put(OFFICE_FLD, row.getString(OFFICE_FLD));
                candidate.put(PROFILE_FLD, row.getString(PROFILE_FLD));

                /*
                 * Sqlite does not support boolean values. Therefore, convert the
                 * boolean value to an integer.
                 */
                elected = row.getBoolean(ELECTED_FLD);
                candidate.put(ELECTED_FLD, convertBooleanToInteger(elected));

                if (!row.isNull(PHOTO_URL_FLD)) {
                    candidate.put(PHOTO_URL_FLD, row.getString(PHOTO_URL_FLD));
                }

                createDate = row.getString(CREATED_DATE_FLD);
                candidate.put(CREATED_DATE_FLD, createDate);

                if (row.isNull(MODIFIED_DATE_FLD)) {
                    candidate.put(MODIFIED_DATE_FLD, createDate);
                    candidatesToAdd.add(candidate);
                }
                else {
                    candidate.put(MODIFIED_DATE_FLD, row.getString(MODIFIED_DATE_FLD));
                    candidatesToUpdate.add(candidate);
                }
            }
        }
    }

    private StringBuilder readResults(Reader reader) throws Exception {
        String line;
        StringBuilder results = new StringBuilder();
        BufferedReader bufReader = new BufferedReader(reader);

        line  = bufReader.readLine();

        while (line != null) {
            results.append(line);
            line = bufReader.readLine();
        }

        return results;
    }

    private Integer convertBooleanToInteger(boolean boolValue) {
        Integer intValue;

        intValue = (boolValue) ? new Integer(1) : new Integer(0);

        return intValue;
    }

}
