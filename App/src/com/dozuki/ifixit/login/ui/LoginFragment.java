package com.dozuki.ifixit.login.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.MediaFragment;
import com.dozuki.ifixit.login.model.LoginListener;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;

public class LoginFragment extends DialogFragment implements OnClickListener {
   private static final int OPEN_ID_RESULT_CODE = 4;
   private static final String LOGIN_STATE = "LOGIN_STATE";
   public static final String LOGIN_NO_REGISTER = "LOGIN_NO_REGISTER";

   private Button mLogin;
   private Button mRegister;
   private ImageButton mGoogleLogin;
   private ImageButton mYahooLogin;

   private EditText mLoginId;
   private EditText mPassword;
   private TextView mErrorText;

   private ProgressBar mLoadingSpinner;
   private Intent mCurIntent;

   private boolean mReadyForRegisterState;
   private boolean mHasRegisterBtn = true;

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

         // Show input fields
         mLoginId.setVisibility(View.VISIBLE);
         mPassword.setVisibility(View.VISIBLE);

         mErrorText.setVisibility(View.VISIBLE);
         mErrorText.setText(error.mMessage);
      }
   };

   /**
    * Required for restoring fragments
    */
   public LoginFragment() {}
   
   public static LoginFragment newInstance() {
      return new LoginFragment();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Bundle bundle = this.getArguments();

      if (bundle != null) {
         mHasRegisterBtn = !bundle.getBoolean(LOGIN_NO_REGISTER);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      
      View view = inflater.inflate(R.layout.login_fragment, container, false);

      mLoginId = (EditText)view.findViewById(R.id.edit_username);
      mPassword = (EditText)view.findViewById(R.id.edit_password);
      mPassword.setTypeface(Typeface.DEFAULT);

      mLogin = (Button)view.findViewById(R.id.signin_button);
      mRegister = (Button)view.findViewById(R.id.register_button);      
      mGoogleLogin = (ImageButton)view.findViewById(R.id.use_google_login_button);
      mYahooLogin = (ImageButton)view.findViewById(R.id.use_yahoo_login_button);

      mLogin.setOnClickListener(this);  
      
      if (mHasRegisterBtn) {
         mRegister.setOnClickListener(this);
         mGoogleLogin.setOnClickListener(this);
         mYahooLogin.setOnClickListener(this);
      } else {
         mRegister.setVisibility(View.INVISIBLE);
         mGoogleLogin.setVisibility(View.INVISIBLE);
         mYahooLogin.setVisibility(View.INVISIBLE);
      }

      mErrorText = (TextView)view.findViewById(R.id.login_error_text);
      mErrorText.setVisibility(View.GONE);

      mLoadingSpinner = (ProgressBar)view.findViewById(R.id.login_loading_bar);
      mLoadingSpinner.setVisibility(View.GONE);
      
      getDialog().setTitle(R.string.login_dialog_title);
      
      return view;
   }

   private void login() {
      String login = mLoginId.getText().toString();
      String password = mPassword.getText().toString();

      if (login.length() > 0 && password.length() > 0 ) {
         // Hide input fields
         mLoginId.setVisibility(View.GONE);
         mPassword.setVisibility(View.GONE);
         
         mLoadingSpinner.setVisibility(View.VISIBLE);
         enable(false);
         mCurIntent = APIService.getLoginIntent(getActivity(), login, password);
         getActivity().startService(mCurIntent);
      } else {
         if (login.length() < 1) {
            mLoginId.requestFocus();
            showKeyboard();
         } else {
            mPassword.requestFocus();
            showKeyboard();
         }
         mErrorText.setText(R.string.empty_field_error);
         mErrorText.setVisibility(View.VISIBLE);
      }
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
      mLogin.setEnabled(enabled);
      if (mHasRegisterBtn) {
         mRegister.setEnabled(enabled);
         mGoogleLogin.setEnabled(enabled);
         mYahooLogin.setEnabled(enabled);
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(LOGIN_STATE, mReadyForRegisterState);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
   }

   @Override
   public void onClick(View v) {
      Intent intent;
      switch (v.getId()) {
          case R.id.use_google_login_button:
             intent = new Intent(getActivity(), OpenIDActivity.class);
             intent.putExtra(OpenIDActivity.LOGIN_METHOD, OpenIDActivity.GOOGLE_LOGIN);
             startActivityForResult(intent, OPEN_ID_RESULT_CODE);
             break;
    
          case R.id.use_yahoo_login_button:
             intent = new Intent(getActivity(), OpenIDActivity.class);
             intent.putExtra(OpenIDActivity.LOGIN_METHOD, OpenIDActivity.YAHOO_LOGIN);
             startActivityForResult(intent, OPEN_ID_RESULT_CODE);
             break;
    
          case R.id.register_button:
             FragmentManager fragmentManager = getFragmentManager();
           
             fragmentManager.beginTransaction()
              .add(new RegisterFragment(), MainApplication.REGISTER_FRAGMENT)
              .remove(fragmentManager.findFragmentByTag(
                      MainApplication.LOGIN_FRAGMENT))
              .addToBackStack(MainApplication.LOGIN_FRAGMENT)
              .setTransition(android.R.anim.slide_in_left)
              .commit();
              
             break;
             
          case R.id.signin_button:
             InputMethodManager in = (InputMethodManager)getActivity()
              .getSystemService(Context.INPUT_METHOD_SERVICE);

             in.hideSoftInputFromWindow(mLoginId.getApplicationWindowToken(),
              InputMethodManager.HIDE_NOT_ALWAYS);
             login();
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
      filter.addAction(APIEndpoint.LOGIN.mAction);
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
      return createLogoutDialog(context, 
              R.string.logout_title,    // Title Text
              R.string.logout_messege,  // Message Text
              R.string.logout_confirm,  // Confirm Button Text
              R.string.logout_cancel);  // Cancel Button Text
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
      dialog.setCanceledOnTouchOutside(true);

      return dialog;
   }

   @Override
   public void onCancel(DialogInterface dialog) {
      ((LoginListener)getActivity()).onCancel();
   }
}
