package com.example.candidatescorner.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.database.sqlite.SQLiteDatabase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by chevelle on 7/7/17.
 */

public class CandidatesProvider extends ContentProvider {


    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "nominations";

    private static final int CANDIDATES = 1;
    private static final int CANDIDATE_ID = 2;

    private CandidatesDBHelper dbHelper;

    private static final UriMatcher tblMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        tblMatcher.addURI(CandidatesDBUtil.CANDIDATES_AUTHORITY, "candidates", CANDIDATES);
        tblMatcher.addURI(CandidatesDBUtil.CANDIDATES_AUTHORITY, "candidates/#", CANDIDATE_ID);
    }

    @Override
    public boolean onCreate() {

        dbHelper = new CandidatesDBHelper(getContext(), DB_NAME, null, DB_VERSION);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor results = db.query(CandidatesDBUtil.CANDIDATES_TABLE, projection, selection,
                selectionArgs, null, null, sortOrder);

        return results;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long rowId;
        Uri rowUri = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        rowId = db.insertWithOnConflict(CandidatesDBUtil.CANDIDATES_TABLE, null,
                contentValues, SQLiteDatabase.CONFLICT_IGNORE);

        if (rowId != -1) {
            rowUri = ContentUris.withAppendedId(uri, rowId);
        }

        return rowUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String whereClause,
                      @Nullable String[] args) {
        int rowsUpdated;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        rowsUpdated = db.update(CandidatesDBUtil.CANDIDATES_TABLE, contentValues, whereClause, args);

        return rowsUpdated;
    }
}
