package com.example.candidatescorner.widget;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.example.candidatescorner.R;
import com.example.candidatescorner.data.CandidatesDBUtil;
import com.example.candidatescorner.data.CandidatesResults;
import com.example.candidatescorner.details.CandidateDetailsActivity;
import com.example.candidatescorner.listing.model.Candidate;
import com.example.candidatescorner.listing.model.CandidateParcelable;

/**
 * Created by chevelle on 4/7/18.
 */

public class CandidatesAppWidgetProvider extends AppWidgetProvider {

    private static final int NO_CANDIDATE = -1;
    private static final String EMPTY_CANDIDATE_NAME = "";

    private Random selector;

    public CandidatesAppWidgetProvider() {
        selector = new Random();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appManager, int [] appWidgetIds) {
        String key;
        Intent intent;
        int appWidgetId;
        int candidateIdx;
        String candidateName;
        RemoteViews candidateView;
        PendingIntent pendingIntent;
        int totalIds = appWidgetIds.length;
        ArrayList<CandidateParcelable> officeCandidates;
        List<Candidate> candidates = getCandidates(context);

        candidateIdx = selectCandidateToView(candidates);
        candidateName = getCandidateName(candidateIdx, candidates);
        officeCandidates = findOfficeCandidates(candidateIdx, candidates);


        for (int idx = 0; idx < totalIds; idx++) {
            appWidgetId = appWidgetIds[idx];

            candidateView = createCandidateView(context, candidateIdx, candidates);

            if (candidateIdx != NO_CANDIDATE) {
                intent = new Intent(context, CandidateDetailsActivity.class);

                key = context.getString(R.string.candidate_selected_key);
                intent.putExtra(key, candidateName);

                key = context.getString(R.string.candidates_detail_key);
                intent.putParcelableArrayListExtra(key, officeCandidates);

                pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                candidateView.setOnClickPendingIntent(R.id.appCandidateInfo, pendingIntent);

            }

            appManager.updateAppWidget(appWidgetId, candidateView);
        }

    }

    private List<Candidate> getCandidates(Context context) {
        Cursor results;
        String whereClause;
        List<Candidate> candidates;
        Uri uri = CandidatesDBUtil.getCandidatesUri();
        String electionYr = CandidatesDBUtil.getElectionYear();
        String [] resultCols = CandidatesDBUtil.getCandidateListCols();

        whereClause = CandidatesDBUtil.ELECTION_YEAR_FLD + "= '" + electionYr + "'";

        results = context.getContentResolver().query(uri, resultCols, whereClause,
                null, null);

        candidates = CandidatesResults.processResults(results);

        return candidates;
    }

    private RemoteViews createCandidateView(Context context, int selectedIndex,
                                        List<Candidate> candidates) {
        Candidate candidate;
        String candidateName;
        String candidateInfo;
        RemoteViews candidateView = new RemoteViews(context.getPackageName(),
                            R.layout.candidates_app_widget);

        if (selectedIndex != NO_CANDIDATE) {
            candidate = candidates.get(selectedIndex);
            candidateName = buildNameForDisplay(candidate);

            candidateInfo = context.getString(R.string.widgetTitle) + "\n" + candidateName;
        }
        else {
            candidateInfo = context.getString(R.string.noCandidates);
        }

        candidateView.setTextViewText(R.id.appCandidateInfo, candidateInfo);

        return candidateView;
    }


    private int selectCandidateToView(List<Candidate> candidates) {
        int index = NO_CANDIDATE;

        if (!candidates.isEmpty()) {
            index = selector.nextInt(100) % candidates.size();
        }

        return index;
    }

    private String getCandidateName(int selectedCandidate, List<Candidate> candidates) {
        Candidate candidate;
        String candidateName = EMPTY_CANDIDATE_NAME;

        if (selectedCandidate != NO_CANDIDATE) {
            candidate = candidates.get(selectedCandidate);
            candidateName = buildNameForDisplay(candidate);
        }

        return candidateName;
    }

    private String buildNameForDisplay(Candidate candidate) {
        String name = candidate.getFirstName() + " " + candidate.getLastName();

        return name;
    }

    private ArrayList<CandidateParcelable> findOfficeCandidates(int selectedCandidate,
                       List<Candidate> candidates) {

        String selectedOffice;
        String candidateOffice;
        CandidateParcelable data;
        ArrayList<CandidateParcelable> officeCandidates = new ArrayList<>();

        if (selectedCandidate != NO_CANDIDATE) {

            selectedOffice = candidates.get(selectedCandidate).getOffice();

            for (Candidate candidate : candidates) {
                candidateOffice = candidate.getOffice();

                if (candidateOffice.equals(selectedOffice)) {
                    data = new CandidateParcelable(candidate);
                    officeCandidates.add(data);
                }

            }
        }


        return officeCandidates;
    }
}
