package com.example.candidatescorner.details;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import com.example.candidatescorner.R;
import com.example.candidatescorner.listing.model.Candidate;
import com.example.candidatescorner.listing.model.CandidateParcelable;

/**
 * Created by chevelle on 11/5/17.
 */

public class CandidateDetailsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.candidate_detail_view);
        populateCandidateView(getIntent());
    }

    @Override
    public void onPause() {
        super.onPause();

        // save the rating in the preference
    }

    private void populateCandidateView(Intent detailIntent) {
        String detailKey = getString(R.string.candidate_detail_key);
        CandidateParcelable parcelable = detailIntent.getParcelableExtra(detailKey);
        Candidate candidate = parcelable.getCandidate();

        loadPhoto(candidate.getPhotoUrl());
        loadCandidateOffice(candidate.getOffice());
        loadCandidateName(candidate.getFirstName(), candidate.getLastName());
        loadCandidateProfile(candidate.getProfile());
    }

    private void loadPhoto(String photoUrl) {
        boolean loaded = false;
        ImageView img = (ImageView) findViewById(R.id.candidatePhoto);

        try {

            Picasso.with(this).load(photoUrl).into(img);
            loaded = true;
        }
        catch(Exception uriErr) {}
        finally {

            if (!loaded) {
                Picasso.with(this).load(R.drawable.dst_torch).into(img);
            }
        }

    }

    private void loadCandidateOffice(String office) {
        String officeTitle = "Candidate for " + office;
        TextView officeView = (TextView) findViewById(R.id.chapterPosition);

        officeView.setText(officeTitle);
    }

    private void loadCandidateName(String firstName, String lastName) {
        String candidateName = firstName + " " + lastName;
        TextView nameView = (TextView) findViewById(R.id.memberName);

        nameView.setText(candidateName);
    }

    private void loadCandidateProfile(String profile) {
        TextView profileView = (TextView) findViewById(R.id.scrolledProfile);
        profileView.setText(profile);
    }

}
