package com.solforge.carddbforsolforge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class SortByDialog extends DialogFragment {

    private sortByDialogListener dialogListener;

    public SortByDialog() {
        // Required empty public constructor
    }

    public static SortByDialog newInstance (int itemSelected) {
        SortByDialog sortByDialog = new SortByDialog();
        Bundle args = new Bundle();
        args.putInt("selection", itemSelected);
        sortByDialog.setArguments(args);
        return sortByDialog;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        int itemSelected = getArguments().getInt("selection");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.sort_dialog)
                .setSingleChoiceItems(R.array.sort, itemSelected,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogListener = (sortByDialogListener) getTargetFragment();
                                dialogListener.onClickSortMethod(which);
                                dismiss();
                            }
                        });
        return builder.create();
    }

    public interface sortByDialogListener {
        public void onClickSortMethod(int i);
    }
}
