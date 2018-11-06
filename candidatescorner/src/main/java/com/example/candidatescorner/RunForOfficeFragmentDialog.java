package com.example.candidatescorner;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

/**
 * Created by chevelle on 3/18/18.
 */

public class RunForOfficeFragmentDialog extends DialogFragment {

    public interface RunForOfficeDialogListener {
        public void onOfficeSelected(DialogFragment dlg);
        public void onOfficeCancelled(DialogFragment dlg);
    }

    private CharSequence selectedChapterOffice;
    private RunForOfficeDialogListener officeListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        TextView titleView = createDlgTitle();
        String submitBtnText = getString(R.string.submitBtn);
        String cancelBtnText = getString(R.string.cancelBtn);
        LayoutInflater inflater = ((MainActivity)officeListener).getLayoutInflater();
        AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
        View dlgView = inflater.inflate(R.layout.run_for_office, null);

        initSpinner(dlgView);

        dlg.setCustomTitle(titleView);
        dlg.setView(dlgView);
        dlg.setPositiveButton(submitBtnText, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                officeListener.onOfficeSelected(RunForOfficeFragmentDialog.this);
            }
        });

        dlg.setNegativeButton(cancelBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                officeListener.onOfficeCancelled(RunForOfficeFragmentDialog.this);
            }
        });

        return dlg.create();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            officeListener = (RunForOfficeDialogListener) activity;
        }
        catch(ClassCastException err) {
            throw new ClassCastException(activity.toString()
                    + "must implement RunForOfficeDialogListener");
        }
    }

    public String getSelectedChapterOffice() {
        return (String)selectedChapterOffice;
    }

    private void initSpinner(View dlgView) {
        Spinner offices = (Spinner)dlgView.findViewById(R.id.chapterOffices);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.chapter_offices,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        offices.setAdapter(adapter);
        offices.setOnItemSelectedListener(new OfficeListener());
    }

    private TextView createDlgTitle() {
        String dlgTitle = getString(R.string.dlgTitle);
        TextView titleView = new TextView(getContext());

        titleView.setText(dlgTitle);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        titleView.setTextColor(getResources().getColor(R.color.others_list_color));
        titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        return titleView;
    }

    private class OfficeListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            selectedChapterOffice = ((TextView)view).getText();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
