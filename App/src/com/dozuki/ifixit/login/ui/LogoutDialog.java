package com.dozuki.ifixit.login.ui;

import android.content.DialogInterface;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIService;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;

/**
 * Creates a logout dialog that logs the user out.
 */
public class LogoutDialog {
   public static AlertDialog create(Activity activity) {
      return createLogoutDialog(
         activity,
         R.string.logout_title,
         R.string.logout_messege,
         R.string.logout_confirm,
         R.string.logout_cancel
      );
   }

   private static AlertDialog createLogoutDialog(final Activity activity,
    int titleRes, int messageRes, int buttonConfirm, int buttonCancel) {
      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder
         .setTitle(activity.getString(titleRes))
         .setMessage(activity.getString(messageRes))
         .setPositiveButton(activity.getString(buttonConfirm),
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  APIService.call(activity, APIService.getLogoutAPICall());
                  MainApplication.get().logout();
                  dialog.dismiss();
               }
            })
      .setNegativeButton(activity.getString(buttonCancel),
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
