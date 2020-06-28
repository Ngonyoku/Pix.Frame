package com.example.pixframe.dialogs;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogBuilder {

    public DialogBuilder() {
    }

    public static void createDialog(AlertDialog.Builder builder) {
        builder.create().show();
    }

}
