package com.example.candidatescorner.details;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.squareup.picasso.Picasso;

import com.example.candidatescorner.R;
import com.example.candidatescorner.listing.model.Candidate;
import com.example.candidatescorner.listing.model.CandidateParcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by chevelle on 2/19/18.
 */

public class CandidateDetailsFragment extends Fragment {

    private static final String TAG = "DetailsFragmentActivity";

    private static final String HEADER_TITLE = "Select a candidate";

    private int candidateInView = -1;
    private String lastViewedCandidate = null;

    private RatingBar ratingBar;
    //private MaterialSheetFab candidatesSheet;

    private Map<String, Candidate> officeCandidates;
    private Map<String, Float> candidatesRatings;
    private SharedPreferences candidatesPref;
    private ArrayList<CandidateParcelable> parcelables;
    private boolean detailsRestored = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.candidate_detail_fragment, container, false);
    }

    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);

        String prefFile = getString(R.string.candidates_pref_file);

        // Initialize rating bar.
        initRatingBar();

        // Get preferences file, CandidatesRatings.
        candidatesPref = getActivity().getSharedPreferences(prefFile, 0);
    }

    public void onPause() {
        super.onPause();

        Set<String> keys = officeCandidates.keySet();
        SharedPreferences.Editor editor = candidatesPref.edit();

        for (String key : keys) {
            editor.putFloat(key, candidatesRatings.get(key));
        }

        editor.commit();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String candidateIdKey = getActivity().getString(R.string.candidate_id_key);
        String lastViewedKey = getActivity().getString(R.string.candidate_last_view_key);
        String officeListKey = getActivity().getString(R.string.candidates_list_key);

        outState.putInt(candidateIdKey, candidateInView);
        outState.putString(lastViewedKey, lastViewedCandidate);
        outState.putParcelableArrayList(officeListKey, parcelables);
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        int fragId = getId();
        boolean multiPaned = (fragId == R.id.candidateInfo);

        if (savedInstanceState != null) {
            ArrayList<CandidateParcelable> candidates;
            String lastViewKey = getActivity().getString(R.string.candidate_last_view_key);
            String officeListKey = getActivity().getString(R.string.candidates_list_key);

            lastViewedCandidate = savedInstanceState.getString(lastViewKey);
            candidates = savedInstanceState.getParcelableArrayList(officeListKey);

            // Restore content.
            detailsRestored = true;
            loadCandidateDetails(lastViewedCandidate, candidates, multiPaned);
        }
    }

    public void loadCandidateDetails(String candidateToView,
                                 ArrayList<CandidateParcelable> parcelables) {

        loadCandidateDetails(candidateToView, parcelables, false);
    }

    public void loadCandidateDetails(String candidateToView,
                         ArrayList<CandidateParcelable> parcelables, boolean multiPaned) {


        loadOfficeCandidates(parcelables);

        if (multiPaned) {
            createMultiPanedCandidatesListingView();
        }
        else {
            createCandidatesListingView();
        }

        populateCandidateView(candidateToView);
    }

    public boolean isDetailsRestored() {
        return detailsRestored;
    }

    private void createCandidatesListingView() {
        boolean show = false;

        if (officeCandidates.size() > 1) {
            show = true;
            createBottomSheetList();
        }

        showCandidatesListingView(show);
    }

    private void createMultiPanedCandidatesListingView() {
        boolean show = false;
        ListView listing = getActivity().findViewById(R.id.officeCandidates);

        if (officeCandidates.size() > 1) {
            show = true;

            if (listing == null) {
                createBottomSheetList();
            }
            else {
                updateBottomSheetList();
            }

            showCandidatesListingView(true);
        }

        showCandidatesListingView(show);
    }

    private void showCandidatesListingView(boolean show) {
        //FloatingActionButton fab = getActivity().findViewById(R.id.candidatesFab);
        LinearLayout candidatesSheet = getActivity().findViewById(R.id.viewLayout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)candidatesSheet.getLayoutParams();
        BottomSheetBehavior behavior = (BottomSheetBehavior)params.getBehavior();

        if (show) {
            //fab.show();
            behavior.setPeekHeight(200);
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else {
            //fab.hide();
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void createBottomSheetList() {
        TextView header = new TextView(getActivity());
        ArrayAdapter<String> nameAdapter = null;
        String [] candidatesNames = getCandidatesNames();
        ListView listing = getActivity().findViewById(R.id.officeCandidates);
        int backgroundColor = getResources().getColor(R.color.colorPrimary);

        nameAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.office_candidates_view, R.id.office_candidate_name);
        nameAdapter.addAll(candidatesNames);

        listing.setAdapter(nameAdapter);
        listing.setOnItemClickListener(new OfficeCandidateListener());

        // Configure and add the a header to the list.
        if (listing.getHeaderViewsCount() == 0) {
            header.setText(HEADER_TITLE);
            header.setHeight(90);
            header.setPadding(16, 9, 0,0);
            header.setTextAppearance(R.style.CandidatesHeader);
            header.setBackgroundColor(backgroundColor);
            listing.addHeaderView(header);
        }

    }

    private void updateBottomSheetList() {
        String [] candidatesNames = getCandidatesNames();
        ListView listing = (ListView) getActivity().findViewById(R.id.officeCandidates);
        HeaderViewListAdapter hdrAdapter = (HeaderViewListAdapter)listing.getAdapter();
        ArrayAdapter<String> namesAdapter = (ArrayAdapter<String>)hdrAdapter.getWrappedAdapter();

        namesAdapter.clear();
        namesAdapter.addAll(candidatesNames);
    }

    private void initRatingBar() {
        ratingBar = (RatingBar) getActivity().findViewById(R.id.candidateRating);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Candidate candidate = null;

                if (officeCandidates == null) {
                    return;
                }

                Set<String> keys = officeCandidates.keySet();

                for (String key : keys) {
                    candidate = officeCandidates.get(key);

                    if (candidate.getId() == candidateInView) {
                        candidatesRatings.put(key, new Float(rating));
                        break;
                    }
                }
            }
        });
    }

    private void loadOfficeCandidates(ArrayList<CandidateParcelable> parcelables) {
        String memberKey = null;
        Candidate candidate = null;
        float rating;

        this.parcelables = parcelables;

        officeCandidates = new HashMap<String, Candidate>();
        candidatesRatings = new HashMap<String, Float>();

        for (CandidateParcelable parcelable : parcelables) {
            candidate = parcelable.getCandidate();
            memberKey = candidate.getFirstName() + " " + candidate.getLastName();

            officeCandidates.put(memberKey, candidate);

            rating = candidatesPref.getFloat(memberKey, (float)0);
            candidatesRatings.put(memberKey, rating);
        }
    }

    private void populateCandidateView(String candidateToView) {
        Candidate candidate = officeCandidates.get(candidateToView);

        if (candidateInView == candidate.getId()) {
            return;
        }

        candidateInView = candidate.getId();
        lastViewedCandidate = candidate.getFirstName() + " " + candidate.getLastName();

        loadPhoto(lastViewedCandidate, candidate.getPhotoUrl());
        loadCandidateOffice(candidate.getOffice());
        loadCandidateName(lastViewedCandidate);
        loadCandidateProfile(candidate.getProfile());
        loadCandidateRating(lastViewedCandidate);
    }

    private void loadPhoto(String candidateName, String photoUrl) {
        boolean loaded = false;
        ImageView img = (ImageView) getActivity().findViewById(R.id.candidatePhoto);

        try {

            if ((photoUrl != null) && !photoUrl.isEmpty()) {
                Picasso.with(getActivity()).load(photoUrl).into(img);
                loaded = true;
            }
        }
        catch(Exception uriErr) { }
        finally {

            if (!loaded) {
                Picasso.with(getActivity()).load(R.drawable.aacdst_avatar).into(img);
            }
        }

        img.setContentDescription(candidateName);

    }

    private void loadCandidateOffice(String office) {
        String officeTitle = "Candidate for \n" + office;
        TextView officeView = (TextView) getActivity().findViewById(R.id.chapterPosition);

        officeView.setText(officeTitle);
    }

    private void loadCandidateName(String candidateName) {
        TextView nameView = (TextView) getActivity().findViewById(R.id.memberName);

        nameView.setText(candidateName);
    }

    private void loadCandidateProfile(String profile) {
        TextView profileView = (TextView) getActivity().findViewById(R.id.scrolledProfile);
        profileView.setText(profile);
    }

    private void loadCandidateRating(String candidateName) {
        float rating = candidatesRatings.get(candidateName);

        ratingBar = (RatingBar) getActivity().findViewById(R.id.candidateRating);
        ratingBar.setRating(rating);
    }

    private String [] getCandidatesNames() {
        SortedSet<String> names = new TreeSet<>();
        String [] namesArray = new String[officeCandidates.size()];

        names.addAll(officeCandidates.keySet());
        names.toArray(namesArray);

        return namesArray;
    }

    private class OfficeCandidateListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parentView, View nameView, int position, long id) {
            String selectedCandidate = (String) parentView.getItemAtPosition(position);

            populateCandidateView(selectedCandidate);
        }
    }

}
