package com.dozuki.ifixit.login.ui;

import android.content.Context;
import android.content.DialogInterface;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;

import org.holoeverywhere.app.AlertDialog;

public class LogoutDialog {
   public static AlertDialog create(Context context) {
      return createLogoutDialog(
         context,
         R.string.logout_title,
         R.string.logout_messege,
         R.string.logout_confirm,
         R.string.logout_cancel
      );
   }

   private static AlertDialog createLogoutDialog(final Context context,
    int titleRes, int messageRes, int buttonConfirm, int buttonCancel) {
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder
         .setTitle(context.getString(titleRes))
         .setMessage(context.getString(messageRes))
         .setPositiveButton(context.getString(buttonConfirm),
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  MainApplication.get().logout();
                  dialog.dismiss();
               }
            })
      .setNegativeButton(context.getString(buttonCancel),
         new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               dialog.cancel();
            }
         });

      AlertDialog dialog = builder.create();
      dialog.setCancelable(false);
      dialog.setCanceledOnTouchOutside(true);

      return dialog;
   }
}
