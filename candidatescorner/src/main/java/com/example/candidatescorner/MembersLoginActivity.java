package com.example.candidatescorner;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

public class MembersLoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1947;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isUserSignedIn()) {
            signIn();
        }
        else {
            showCandidatesCorner();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            if (resultCode == RESULT_OK) {
                showCandidatesCorner();
            }
            else {
                Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT);
            }
        }
    }

    private boolean isUserSignedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean signedIn = (auth.getCurrentUser() != null);

        return signedIn;
    }

    private void signIn() {

        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setTheme(R.style.AnnapolisDstTheme)
                .setLogo(R.drawable.aactoolbar_logo)
                .build(), RC_SIGN_IN);
    }

    private void showCandidatesCorner() {
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
        finish();
    }
}
