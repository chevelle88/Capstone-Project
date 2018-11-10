package com.example.candidatescorner.listing;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

import com.example.candidatescorner.R;
import com.example.candidatescorner.MainActivity;
import com.example.candidatescorner.listing.model.Candidate;
import com.example.candidatescorner.listing.model.CandidateParcelable;

/**
 * Created by chevelle on 2/19/18.
 */

public class CandidatesFragment extends Fragment {

    public interface CandidateSelectedListener {
        public void onCandidateSelected(String candidateToView,
                            ArrayList<CandidateParcelable> officeCandidates);
    }

    private boolean multiPaned;
    private int lastCandidateInView;

    private CandidatesDataObserver dataObserver;
    private CandidateSelectedListener candidateListener;

    private static final String TAG = "CandidatesFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            candidateListener = (CandidateSelectedListener) activity;
        }
        catch (ClassCastException err) {
            throw new ClassCastException(activity.toString()
                + "must implement CandidateSelectedListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.candidates_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);

        MainActivity activity = (MainActivity)candidateListener;

        lastCandidateInView = (int)RecyclerView.NO_ID;

        // Get the ID of last candidate viewed it it exists.
        if (saveInstanceState != null) {
            String key = getString(R.string.candidate_id_key);
            lastCandidateInView = saveInstanceState.getInt(key);
        }

        // Set flag to indicate if view is multi-paned.
        multiPaned = activity.isMultiPaned();

        // Get the toolbar.
        Toolbar toolbar = activity.findViewById(R.id.aacdstToolbar);
        activity.setSupportActionBar(toolbar);

        // Create a data observer.
        dataObserver = new CandidatesDataObserver();
        dataObserver.setCandidatesFragment(this);

        // Get recyclerview instance and configure its layout manager and adapter.
        RecyclerView candidates = activity.findViewById(R.id.candidates_list);
        candidates.setLayoutManager(new LinearLayoutManager(activity));
        candidates.setAdapter(new CandidatesAdapter(activity));

        if (activity.isRestartLoader()) {
            activity.getSupportLoaderManager().restartLoader(0, null, activity);
        }
        else {
            activity.getSupportLoaderManager().initLoader(0, null, activity);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity activity = (MainActivity)candidateListener;

        if (!activity.isMultiPaned()) {
            activity.getSupportLoaderManager().restartLoader(0, null, activity);
        }
    }

    public void showCandidatesListing(Cursor data) {
        CandidatesAdapter adapter = null;
        MainActivity activity = (MainActivity)candidateListener;
        TextView emptySlate = activity.findViewById(R.id.emptySlate);
        RecyclerView candidates = activity.findViewById(R.id.candidates_list);

        if ((data != null) && data.getCount() > 0) {

            adapter = (CandidatesAdapter)candidates.getAdapter();

            if (multiPaned && !activity.detailsInMultiPanedLoaded()) {
                adapter.registerAdapterDataObserver(dataObserver);
            }

            adapter.loadCandidates(data);
            adapter.notifyDataSetChanged();

            emptySlate.setVisibility(View.GONE);
            candidates.setVisibility(View.VISIBLE);


        }
        else {
            candidates.setVisibility(View.GONE);
            emptySlate.setVisibility(View.VISIBLE);
        }
    }

    public void resetCandidatesListing() {
        MainActivity activity = (MainActivity)candidateListener;
        RecyclerView candidates = (RecyclerView)activity.findViewById(R.id.candidates_list);

        ((CandidatesAdapter)candidates.getAdapter()).clearCandidates();
    }

    public List<Integer> findCandidatesIds(List<String> candidates) {
        MainActivity activity = (MainActivity)candidateListener;
        RecyclerView recyclerView = activity.findViewById(R.id.candidates_list);

        return ((CandidatesAdapter)recyclerView.getAdapter()).getCandidatesIds(candidates);
    }

    public void showCandidateDetails() {
        MainActivity activity = (MainActivity)candidateListener;
        RecyclerView candidates = activity.findViewById(R.id.candidates_list);
        CandidatesAdapter candidateAdapter = (CandidatesAdapter)candidates.getAdapter();
        String candidateToView;
        Candidate candidate;
        List<Candidate> officeCandidates;
        ArrayList<CandidateParcelable> parcelables;

        candidate = candidateAdapter.findCandidate(lastCandidateInView);
        officeCandidates = candidateAdapter.findOfficeCandidates(candidate.getOffice());

        parcelables = new ArrayList<>();
        for (Candidate officeCandidate : officeCandidates) {
            parcelables.add(new CandidateParcelable(officeCandidate));
        }
        candidateToView = candidate.getFirstName() + " " + candidate.getLastName();

        activity.onCandidateSelected(candidateToView, parcelables);

        candidateAdapter.unregisterAdapterDataObserver(dataObserver);
    }

}