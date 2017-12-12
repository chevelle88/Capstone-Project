package com.example.candidatescorner.listing;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import com.example.candidatescorner.R;
import com.example.candidatescorner.listing.model.Candidate;

public class CandidatesView extends RecyclerView.ViewHolder {

    private static String OFFICE_TITLE =  "Office of ";

    private Context context;
    private Candidate candidate;

    public CandidatesView(View view) {
        super(view);

        view.setOnClickListener(new CandidateListener(this));
    }

    public void setCandidate(Context context, Candidate candidate) {
        this.context = context;
        this.candidate = candidate;

        loadImage();
        setCandidateName();
        setCandidateOffice();
        setCandidateId();
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public Context getContext() {
        return context;
    }

    private void loadImage() {
        ImageView img = (ImageView) itemView.findViewById(R.id.candidateImg);

        Picasso.with(context).load(R.drawable.violet).into(img);
    }

    private void setCandidateName() {
        TextView name =  (TextView) itemView.findViewById(R.id.candidateName);
        String candidateName = candidate.getFirstName() + " " + candidate.getLastName();

        name.setText(candidateName);
    }

    private void setCandidateOffice() {
        TextView officeView = (TextView) itemView.findViewById(R.id.candidateOffice);
        String officeTitle = OFFICE_TITLE + candidate.getOffice();

        officeView.setText(officeTitle);
    }

    private void setCandidateId() {
        TextView candidateId = (TextView) itemView.findViewById(R.id.candidateId);
        String id = Integer.toString(candidate.getId());

        candidateId.setText(id);
    }
}
