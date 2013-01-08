package com.dozuki.ifixit.login.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.util.APICall;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;

public class LoginFragment extends DialogFragment implements OnClickListener {
   private static final int OPEN_ID_RESULT_CODE = 4;

   private Button mLogin;
   private Button mRegister;
   private ImageButton mGoogleLogin;
   //private ImageButton mYahooLogin;
   private EditText mLoginId;
   private EditText mPassword;
   private TextView mErrorText;
   private ProgressBar mLoadingSpinner;
   private APICall mCurAPICall;
   private boolean mHasRegisterBtn = true;

   @Subscribe
   public void onLogin(APIEvent.Login event) {
      handleLogin(event);
   }

   @Subscribe
   public void onUserInfo(APIEvent.UserInfo event) {
      handleLogin(event);
   }

   private void handleLogin(APIEvent<User> event) {
      if (!event.hasError()) {
         User user = event.getResult();
         ((MainApplication)getActivity().getApplication()).login(user);

         dismiss();
      } else {
         enable(true);
         APIError error = event.getError();

         if (error.mType == APIError.ErrorType.CONNECTION ||
          error.mType == APIError.ErrorType.PARSE) {
            APIService.getErrorDialog(getActivity(), error, mCurAPICall).show();
         }

         mLoadingSpinner.setVisibility(View.GONE);

         // Show input fields
         mLoginId.setVisibility(View.VISIBLE);
         mPassword.setVisibility(View.VISIBLE);

         mErrorText.setVisibility(View.VISIBLE);
         mErrorText.setText(error.mMessage);
      }
   }

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

      MainApplication.get().setIsLoggingIn(true);
      
      mHasRegisterBtn = ((MainApplication)getActivity().getApplication())
       .getSite().mPublicRegistration;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      
      View view = inflater.inflate(R.layout.login_fragment, container, false);

      mLoginId = (EditText)view.findViewById(R.id.edit_email);
      mPassword = (EditText)view.findViewById(R.id.edit_password);
      mPassword.setTypeface(Typeface.DEFAULT);

      mLogin = (Button)view.findViewById(R.id.signin_button);
      mRegister = (Button)view.findViewById(R.id.register_button);      
      mGoogleLogin = (ImageButton)view.findViewById(R.id.use_google_login_button);
      //mYahooLogin = (ImageButton)view.findViewById(R.id.use_yahoo_login_button);

      mLogin.setOnClickListener(this);  
      
      if (mHasRegisterBtn) {
         mRegister.setOnClickListener(this);
         mGoogleLogin.setOnClickListener(this);
         //mYahooLogin.setOnClickListener(this);
      } else {
         mRegister.setVisibility(View.GONE);
         mGoogleLogin.setVisibility(View.GONE);
         //mYahooLogin.setVisibility(View.GONE);
      }

      mErrorText = (TextView)view.findViewById(R.id.login_error_text);
      mErrorText.setVisibility(View.GONE);

      mLoadingSpinner = (ProgressBar)view.findViewById(R.id.login_loading_bar);
      mLoadingSpinner.setVisibility(View.GONE);
      
      getDialog().setTitle(R.string.login_dialog_title);
      
      return view;
   }

   @Override
   public void onResume() {
      super.onResume();

      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
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
         mCurAPICall = APIService.getLoginAPICall(getActivity(), login, password);
         APIService.call((Activity)getActivity(), mCurAPICall);
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
         //mYahooLogin.setEnabled(enabled);
      }
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
             FragmentManager fragmentManager = getSupportFragmentManager();
           
             fragmentManager.beginTransaction()
              .remove(this)
              .add(new RegisterFragment(), null)
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
      super.onActivityResult(requestCode, resultCode, data);

      if (resultCode == Activity.RESULT_OK && data != null) {
         mLoadingSpinner.setVisibility(View.VISIBLE);
         String session = data.getStringExtra(OpenIDActivity.SESSION);
         enable(false);
         mCurAPICall = APIService.getUserInfoAPICall(getActivity(), session);
         APIService.call((Activity)getActivity(), mCurAPICall);
      }
   }

   @Override
   public void onCancel(DialogInterface dialog) {
      MainApplication.get().cancelLogin();
   }
}
