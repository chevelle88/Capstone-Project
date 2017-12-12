package com.example.candidatescorner.listing.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.candidatescorner.R;

import static com.example.candidatescorner.data.CandidatesDBUtil.ID_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.PROFILE_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.OFFICE_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.PHOTO_URL_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.FIRST_NAME_FLD;
import static com.example.candidatescorner.data.CandidatesDBUtil.LAST_NAME_FLD;

/**
 * Created by chevelle on 12/9/17.
 */

public class CandidateParcelable implements Parcelable {

    private Candidate candidate;

    public static final Parcelable.Creator<CandidateParcelable> CREATOR =
            new Parcelable.Creator<CandidateParcelable>() {

        @Override
        public CandidateParcelable createFromParcel(Parcel src) {
            return new CandidateParcelable(src);
        }

        @Override
        public CandidateParcelable[] newArray(int size) {
            return new CandidateParcelable[size];
        }
    };


    public CandidateParcelable(Candidate candidate) {
        this.candidate = candidate;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();

        bundle.putInt(ID_FLD, candidate.getId());
        bundle.putString(OFFICE_FLD, candidate.getOffice());
        bundle.putString(FIRST_NAME_FLD, candidate.getFirstName());
        bundle.putString(LAST_NAME_FLD, candidate.getLastName());
        bundle.putString(PROFILE_FLD, candidate.getProfile());
        bundle.putString(PHOTO_URL_FLD, candidate.getProfile());

        dest.writeBundle(bundle);
    }

    public Candidate getCandidate() {
        return candidate;
    }

    private CandidateParcelable(Parcel src) {

        if (src != null) {
            readFromParcel(src);
        }
    }

    private void readFromParcel(Parcel src) {
        Bundle bundle = src.readBundle();

        candidate = new Candidate();
        candidate.setId(bundle.getInt(ID_FLD));
        candidate.setOffice(bundle.getString(OFFICE_FLD));
        candidate.setFirstName(bundle.getString(FIRST_NAME_FLD));
        candidate.setLastName(bundle.getString(LAST_NAME_FLD));
        candidate.setProfile(bundle.getString(PROFILE_FLD));
        candidate.setPhotoUrl(bundle.getString(PHOTO_URL_FLD));
    }
}
