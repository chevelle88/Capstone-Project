package com.example.candidatescorner.listing;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

import com.example.candidatescorner.R;
import com.example.candidatescorner.MainActivity;
import com.example.candidatescorner.listing.model.CandidateParcelable;

/**
 * Created by chevelle on 2/19/18.
 */

public class CandidatesFragment extends Fragment {

    public interface CandidateSelectedListener {
        public void onCandidateSelected(String candidateToView,
                            ArrayList<CandidateParcelable> officeCandidates);
    }

    private CandidateSelectedListener candidateListener;

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

        Log.i("CandidatesFragment", "Inside onActivityCreated");
        MainActivity activity = (MainActivity)candidateListener;

        // Get the toolbar.
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.aacdstToolbar);
        activity.setSupportActionBar(toolbar);

        // Create an adapter for the list view.
        CandidatesAdapter adapter = new CandidatesAdapter(activity);

        // Get the list view and configure its adapter.
        RecyclerView candidates = (RecyclerView) activity.findViewById(R.id.candidates_list);
        candidates.setLayoutManager(new LinearLayoutManager(activity));
        candidates.setAdapter(adapter);

        activity.getSupportLoaderManager().initLoader(0, null, activity);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void showCandidatesListing(Cursor data) {
        CandidatesAdapter adapter = null;
        MainActivity activity = (MainActivity)candidateListener;
        TextView emptySlate = (TextView) activity.findViewById(R.id.emptySlate);
        RecyclerView candidates = (RecyclerView)activity.findViewById(R.id.candidates_list);

        if ((data != null) && data.getCount() > 0) {
            Log.i("CandidatesFragment", "Got data");

            adapter = new CandidatesAdapter(activity);
            adapter.loadCandidates(data);
            candidates.swapAdapter(adapter, true);

            emptySlate.setVisibility(View.GONE);
            candidates.setVisibility(View.VISIBLE);
        }
        else {
            Log.i("CandidatesFragment", "No data");
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
        RecyclerView recyclerView = (RecyclerView)activity.findViewById(R.id.candidates_list);

        return ((CandidatesAdapter)recyclerView.getAdapter()).getCandidatesIds(candidates);
    }
}
