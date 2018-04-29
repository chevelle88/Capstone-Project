package com.example.candidatescorner.details;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.gordonwong.materialsheetfab.AnimatedFab;

/**
 * Created by chevelle on 1/3/18.
 */

public class CandidateDetailsFab extends FloatingActionButton implements AnimatedFab {

    public CandidateDetailsFab(Context context) {
        super(context);
    }

    public CandidateDetailsFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CandidateDetailsFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void show() {
        show(0, 0);
    }

    @Override
    public void show(float translationX, float translationY) {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(View.INVISIBLE);
    }
}
