package com.example.riley.piplace.MainActivity.LoadingDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.example.riley.piplace.R;

/**
 * Constructs a dialog for a loading screen
 */
public class LoadingDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.update_spinner, null));
        return builder.create();
    }

}
