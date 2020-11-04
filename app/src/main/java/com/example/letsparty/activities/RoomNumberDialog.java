package com.example.letsparty.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.letsparty.R;

public class RoomNumberDialog extends DialogFragment {

    public interface RoomNumberListener{
        void onRoomNumberEntered(String roomNumber);
    }

    RoomNumberListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (RoomNumberListener) context;
        } catch (ClassCastException e) {
            listener = null;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        EditText roomNumberText = new EditText(getActivity());
        roomNumberText.setHint(R.string.enter_room_number);

        builder.setView(roomNumberText)
                .setPositiveButton(R.string.dialog_join, (dialog, id) -> {
                    String roomNumber = roomNumberText.getText().toString();
                    dismiss();
                    if (listener != null){
                        listener.onRoomNumberEntered(roomNumber);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());

        return builder.create();
    }
}
