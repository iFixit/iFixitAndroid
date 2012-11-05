package com.dozuki.ifixit.login.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.MediaFragment;
import com.dozuki.ifixit.login.model.LoginListener;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;

public class RegisterFragment extends SherlockDialogFragment implements OnClickListener {
   private static final String REGISTER_STATE = "REGISTER_STATE";

   private Button mRegister;
   private Button mCancelRegister;

   private EditText mLoginId;
   private EditText mPassword;
   private EditText mConfirmPassword;
   private EditText mName;
   private TextView mErrorText;
   private CheckBox mTermsAgreeCheckBox;

   private ProgressBar mLoadingSpinner;
   private Intent mCurIntent;

   private boolean mReadyForRegisterState;

   private APIReceiver mApiReceiver = new APIReceiver() {
      public void onSuccess(Object result, Intent intent) {
         User user = (User)result;
         ((MainApplication)getActivity().getApplication()).login(user);

         ((LoginListener)getActivity()).onLogin(user);
         dismiss();
      }

      public void onFailure(APIError error, Intent intent) {
         enable(true);
         if (error.mType == APIError.ErrorType.CONNECTION ||
          error.mType == APIError.ErrorType.PARSE) {
            APIService.getErrorDialog(getActivity(), error, mCurIntent).show();
         }
         mLoadingSpinner.setVisibility(View.GONE);
         mErrorText.setVisibility(View.VISIBLE);
         mErrorText.setText(error.mMessage);
      }
   };

   /**
    * Required for restoring fragments
    */
   public RegisterFragment() {}

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.register_fragment, container, false);

      mLoginId = (EditText)view.findViewById(R.id.edit_login_id);
      mPassword = (EditText)view.findViewById(R.id.edit_password);

      mConfirmPassword = (EditText)view.findViewById(R.id.edit_confirm_password);
      mName = (EditText)view.findViewById(R.id.edit_login_username);
    
      mRegister = (Button)view.findViewById(R.id.register_button);
      mCancelRegister = (Button)view.findViewById(R.id.cancel_register_button);

      mErrorText = (TextView)view.findViewById(R.id.login_error_text);
      mErrorText.setVisibility(View.GONE);

      mTermsAgreeCheckBox = (CheckBox)view.findViewById(R.id.login_agreement_terms_checkbox);

      mLoadingSpinner = (ProgressBar)view.findViewById(R.id.login_loading_bar);
      mLoadingSpinner.setVisibility(View.GONE);

      mRegister.setOnClickListener(this);
      mCancelRegister.setOnClickListener(this);
      getDialog().setTitle(R.string.register_dialog_title);
      
      return view;
   }

   private void showKeyboard() {
      InputMethodManager in = (InputMethodManager)getActivity()
       .getSystemService(Context.INPUT_METHOD_SERVICE);

      in.toggleSoftInput(InputMethodManager.SHOW_FORCED,
       InputMethodManager.HIDE_IMPLICIT_ONLY);
   }

   private void enable(boolean enabled) {
      mLoginId.setEnabled(enabled);
      mPassword.setEnabled(enabled);
      mConfirmPassword.setEnabled(enabled);
      mRegister.setEnabled(enabled);
      mCancelRegister.setEnabled(enabled);
      mName.setEnabled(enabled);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(REGISTER_STATE, mReadyForRegisterState);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

   }

   public static RegisterFragment newInstance() {
      return new RegisterFragment();
   }

   @Override
   public void onClick(View v) {
      switch (v.getId()) {
         case R.id.register_button:
            String login = mLoginId.getText().toString();
            String name = mName.getText().toString();
            String password = mPassword.getText().toString();
            String confirmPassword = mConfirmPassword.getText().toString();
        
            if (password.equals(confirmPassword) &&
               login.length() > 0 &&
               name.length() > 0 && mTermsAgreeCheckBox.isChecked()) {
               enable(false);
               mErrorText.setVisibility(View.INVISIBLE);
               mLoadingSpinner.setVisibility(View.VISIBLE);
               mCurIntent = APIService.getRegisterIntent(getActivity(), login,
                password, name);
               getActivity().startService(mCurIntent);
            } else {
               if (login.length() <= 0) {
                  mErrorText.setText(R.string.empty_field_error);
                  mLoginId.requestFocus();
                  showKeyboard();
               } else if (password.length() <= 0) {
                  mErrorText.setText(R.string.empty_field_error);
                  mPassword.requestFocus();
                  showKeyboard();
               } else if (name.length() <= 0) {
                  mErrorText.setText(R.string.empty_field_error);
                  mName.requestFocus();
                  showKeyboard();
               } else if (!password.equals(confirmPassword)) {
                  mErrorText.setText(R.string.passwords_do_not_match_error);
               } else if (!mTermsAgreeCheckBox.isChecked()) {
                  mErrorText.setText(R.string.terms_unchecked_error);
                  mConfirmPassword.requestFocus();
                  showKeyboard();
               }
               mErrorText.setVisibility(View.VISIBLE);
            }     
            break;

          case R.id.cancel_register_button:
              FragmentManager fragmentManager = getFragmentManager();
              
              fragmentManager.beginTransaction()
               .add(new LoginFragment(), MainApplication.LOGIN_FRAGMENT)
               .remove(fragmentManager.findFragmentByTag(
                       MainApplication.REGISTER_FRAGMENT))
               .addToBackStack(MainApplication.REGISTER_FRAGMENT)
               .setTransition(android.R.anim.slide_out_right)
               .commit();
               
              break;

          }

   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (resultCode == Activity.RESULT_OK && data != null) {
         mLoadingSpinner.setVisibility(View.VISIBLE);
         String session = data.getStringExtra("session");
         enable(false);
         mCurIntent = APIService.getLoginIntent(getActivity(), session);
         getActivity().startService(mCurIntent);
      }
   }

   @Override
   public void onResume() {
      super.onResume();

      IntentFilter filter = new IntentFilter();

      filter.addAction(APIEndpoint.REGISTER.mAction);
      getActivity().registerReceiver(mApiReceiver, filter);
   }

   @Override
   public void onPause() {
      super.onPause();

      try {
         getActivity().unregisterReceiver(mApiReceiver);
      } catch (IllegalArgumentException e) {
         // Do nothing. This happens in the unlikely event that
         // unregisterReceiver has been called already.
      }
   }


   public static AlertDialog getLogoutDialog(final Context context) {
      return createLogoutDialog(context, R.string.logout_title,
       R.string.logout_messege, R.string.logout_confirm, R.string.logout_cancel);
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
                  ((LoginListener)context).onLogout();
                  dialog.dismiss();
               }
            })
      .setNegativeButton(context.getString(buttonCancel),
         new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               MediaFragment.showingLogout = false;
               dialog.cancel();
            }
         });

      AlertDialog dialog = builder.create();
      dialog.setCancelable(false);

      return dialog;
   }

   @Override
   public void onCancel(DialogInterface dialog) {
      ((LoginListener)getActivity()).onCancel();
   }
}
