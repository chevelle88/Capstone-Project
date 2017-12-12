package com.example.candidatescorner.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chevelle on 7/9/17.
 */

public class CandidatesDBHelper extends SQLiteOpenHelper {

    private static String CREATE_CANDIDATES_TBL =
        "create table if not exists candidates(" +
        "_ID integer primary key, " +
        "first_name text not null, " +
        "last_name text not null, " +
        "election_year text not null, " +
        "office text not null, " +
        "photo_url text, " +
        "profile text, " +
        "elected integer, " +
        "created_date text, " +
        "modified_date text, " +
        "constraint elect_member_year unique(first_name, last_name, election_year))";


    public CandidatesDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CANDIDATES_TBL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int curVersion, int newVersion) {

    }
}
