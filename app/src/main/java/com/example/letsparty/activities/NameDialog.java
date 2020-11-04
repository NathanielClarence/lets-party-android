package com.example.letsparty.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.letsparty.PlayerUtil;
import com.example.letsparty.R;
import com.example.letsparty.entities.Player;

public class NameDialog extends DialogFragment {
    private EditText playerNameText;

    public interface NameDialogListener {
        void onNameDialogPositiveClick(DialogFragment dialog, Player player);
        void onNameDialogNegativeClick(DialogFragment dialog);
    }
    NameDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (NameDialogListener) context;
        } catch (ClassCastException e) {
            listener = null;
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        playerNameText = new EditText(getActivity());
        playerNameText.setHint(R.string.name);

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        SharedPreferences prefs = getContext().getSharedPreferences("TOKEN_PREF", getContext().MODE_PRIVATE);
        final String token = prefs.getString("token", "");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view =inflater.inflate(R.layout.activity_name_dialog, null);
        builder.setView(view)
                // Add action buttons=
                .setPositiveButton(R.string.submit, (dialog, id) -> {
                    playerNameText =  view.findViewById(R.id.name);
                    String playerName = playerNameText.getText().toString();
                    Player player = new Player(PlayerUtil.getPlayerId(), playerName, token);
                    /* roomCreated = new Room("Waiting", player); */
                    Log.d("NAME", playerName);
                    dialog.dismiss();
                    if (listener != null){
                        listener.onNameDialogPositiveClick(NameDialog.this, player);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());
        return builder.create();
    }

}
