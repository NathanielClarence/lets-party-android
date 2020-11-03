package com.example.letsparty.activities;

import com.example.letsparty.R;
import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.android.gms.tasks.Tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class NameDialog extends DialogFragment {
    private EditText playerNameText;
    private Room roomCreated;
    private Activity activity;
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
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        playerNameText = new EditText(getActivity());
        playerNameText.setHint(R.string.name);

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        ServerConnector sc = ServerUtil.getServerConnector();
        SharedPreferences prefs = getContext().getSharedPreferences("TOKEN_PREF", getContext().MODE_PRIVATE);
        final String token = prefs.getString("token", "");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view =inflater.inflate(R.layout.activity_name_dialog, null);
        builder.setView(view)
                // Add action buttons=
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        playerNameText =  view.findViewById(R.id.name);
                        String playerName = playerNameText.getText().toString();
                        Player player = new Player("1", playerName, token);
                        /* roomCreated = new Room("Waiting", player); */
                        Log.d("NAME", playerName);
                        dialog.dismiss();
                        if (listener != null){
                            listener.onNameDialogPositiveClick(NameDialog.this, player);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NameDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
    private void goToLobby(Room room) {
        Intent intent = new Intent(getActivity(), Lobby.class);
        intent.putExtra(MainActivity.ROOM, room);
        startActivity(intent);
    }
}
