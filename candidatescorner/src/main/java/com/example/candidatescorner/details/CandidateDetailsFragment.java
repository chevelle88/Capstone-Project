package com.example.candidatescorner.details;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.codetail.widget.RevealLinearLayout;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.DimOverlayFrameLayout;

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

    private static final String TAG = "CandidateDetails";
    private static final String HEADER_TITLE = "Select a candidate";

    private int candidateInView = -1;

    private RatingBar ratingBar;
    private MaterialSheetFab candidatesSheet;
    private Map<String, Candidate> officeCandidates;
    private Map<String, Float> candidatesRatings;
    private SharedPreferences ratingsPref;
    private Activity parent;

    private String candidateToSave;
    private ArrayList<CandidateParcelable> candidatesToSave;

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
        String prefFile = getString(R.string.ratings_pref_file);

        super.onActivityCreated(saveInstanceState);

        // Get the parent activity that contains this fragment.
        parent = this.getActivity();

        // Initialize rating bar.
        initRatingBar();

        // Get preferences file, CandidatesRatings.
        ratingsPref = parent.getSharedPreferences(prefFile, 0);
    }

    public void onPause() {
        super.onPause();

        Set<String> keys = officeCandidates.keySet();
        SharedPreferences.Editor editor = ratingsPref.edit();

        for (String key : keys) {
            editor.putFloat(key, candidatesRatings.get(key));
        }

        editor.commit();
    }

    public void onSaveInstanceState(Bundle outState) {
        String selectedCandidateKey = parent.getString(R.string.candidate_selected_key);
        String officeCandidatesKey = parent.getString(R.string.office_candidates_key);

        outState.putString(selectedCandidateKey, candidateToSave);
        outState.putParcelableArrayList(officeCandidatesKey, candidatesToSave);
    }

    public void loadCandidateDetails(String candidateToView,
                         ArrayList<CandidateParcelable> parcelables ) {

        loadOfficeCandidates(parcelables);

        createCandidatesListingView();

        populateCandidateView(candidateToView);
    }

    private void createCandidatesListingView() {

        if (officeCandidates.size() > 1) {
            createMaterialSheetList();
            createMaterialSheetFab();
        }
        else {
            hideCandidatesListingView();
        }

    }

    private void hideCandidatesListingView() {
        CandidateDetailsFab fab = (CandidateDetailsFab) parent.findViewById(R.id.candidatesFab);
        DimOverlayFrameLayout overlay = (DimOverlayFrameLayout) parent.findViewById(R.id.overlay);
        RevealLinearLayout viewLayout = (RevealLinearLayout) parent.findViewById(R.id.viewLayout);

        fab.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);
        viewLayout.setVisibility(View.GONE);
    }

    private void createMaterialSheetFab() {
        View officeView = parent.findViewById(R.id.officeCandidates);
        CandidateDetailsFab fab = (CandidateDetailsFab) parent.findViewById(R.id.candidatesFab);
        DimOverlayFrameLayout overlay = (DimOverlayFrameLayout) parent.findViewById(R.id.overlay);
        int officeViewColor = getResources().getColor(R.color.others_list_color);
        int fabColor = getResources().getColor(R.color.colorAccent);

        candidatesSheet = new MaterialSheetFab<>(fab, officeView,
                overlay, officeViewColor, fabColor);

    }

    private void createMaterialSheetList() {
        TextView header = new TextView(parent);
        ArrayAdapter<String> nameAdapter = null;
        String [] candidatesNames = getCandidatesNames();
        ListView listing = (ListView) parent.findViewById(R.id.officeCandidates);
        int backgroundColor = getResources().getColor(R.color.colorPrimary);

        nameAdapter = new ArrayAdapter<String>(parent,
                R.layout.office_candidates_view, R.id.office_candidate_name);
        nameAdapter.addAll(candidatesNames);

        listing.setAdapter(nameAdapter);
        listing.setOnItemClickListener(new OfficeCandidateListener());

        // Configure and add the a header to the list.
        header.setText(HEADER_TITLE);
        header.setHeight(90);
        header.setPadding(16, 9, 0,0);
        header.setTextAppearance(R.style.CandidatesHeader);
        header.setBackgroundColor(backgroundColor);
        listing.addHeaderView(header);
    }

    private void initRatingBar() {
        ratingBar = (RatingBar) parent.findViewById(R.id.candidateRating);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Candidate candidate = null;
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

        officeCandidates = new HashMap<String, Candidate>();
        candidatesRatings = new HashMap<String, Float>();

        for (CandidateParcelable parcelable : parcelables) {
            candidate = parcelable.getCandidate();
            memberKey = candidate.getFirstName() + " " + candidate.getLastName();

            officeCandidates.put(memberKey, candidate);

            rating = ratingsPref.getFloat(memberKey, (float)0);
            candidatesRatings.put(memberKey, rating);
        }

        // Save the office candidates.
        candidatesToSave = parcelables;
    }

    private void populateCandidateView(String candidateToView) {
        String candidateName = null;
        Candidate candidate = officeCandidates.get(candidateToView);

        if (candidateInView == candidate.getId()) {
            return;
        }

        candidateInView = candidate.getId();
        candidateName = candidate.getFirstName() + " " + candidate.getLastName();

        loadPhoto(candidateName, candidate.getPhotoUrl());
        loadCandidateOffice(candidate.getOffice());
        loadCandidateName(candidateName);
        loadCandidateProfile(candidate.getProfile());
        loadCandidateRating(candidateName);

        // Save the current candidate in view.
        candidateToSave = candidateName;
    }

    private void loadPhoto(String candidateName, String photoUrl) {
        boolean loaded = false;
        ImageView img = (ImageView) parent.findViewById(R.id.candidatePhoto);

        try {

            if ((photoUrl != null) && !photoUrl.isEmpty()) {
                Picasso.with(parent).load(photoUrl).into(img);
                loaded = true;
            }
        }
        catch(Exception uriErr) { }
        finally {

            if (!loaded) {
                Picasso.with(parent).load(R.drawable.aacdst_avatar).into(img);
            }
        }

        img.setContentDescription(candidateName);

    }

    private void loadCandidateOffice(String office) {
        String officeTitle = "Candidate for \n" + office;
        TextView officeView = (TextView) parent.findViewById(R.id.chapterPosition);

        officeView.setText(officeTitle);
    }

    private void loadCandidateName(String candidateName) {
        TextView nameView = (TextView) parent.findViewById(R.id.memberName);

        nameView.setText(candidateName);
    }

    private void loadCandidateProfile(String profile) {
        TextView profileView = (TextView) parent.findViewById(R.id.scrolledProfile);
        profileView.setText(profile);
    }

    private void loadCandidateRating(String candidateName) {
        float rating = candidatesRatings.get(candidateName);

        ratingBar = (RatingBar) parent.findViewById(R.id.candidateRating);
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
        public void onItemClick(AdapterView<?> parent, View nameView, int position, long id) {
            String selectedCandidate = (String) parent.getItemAtPosition(position);

            populateCandidateView(selectedCandidate);

            candidatesSheet.hideSheet();
        }
    }

}
