package com.example.pixFrame.dialogs;


import android.app.AlertDialog;

public class DialogBuilder {

    public DialogBuilder() {
    }

    public static void createDialog(AlertDialog.Builder builder) {
        builder.create().show();
    }

}
