package com.dozuki.ifixit.view.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.HTTPRequestHelper;
import com.dozuki.ifixit.view.model.LoginListener;
import com.dozuki.ifixit.view.model.User;

public class LoginFragment extends SherlockFragment implements OnClickListener {
   private static ArrayList<LoginListener> loginListeners =
    new ArrayList<LoginListener>();
   private static final int OPEN_ID_RESULT_CODE = 4;
   private static final String LOGIN_STATE = "LOGIN_STATE";

   private Context mContext;
   private ImageButton mLogin;
   private ImageButton mRegister;
   private ImageButton mCancelRegister;
   private ImageButton mGoogleLogin;
   private ImageButton mYahooLogin;

   private EditText mLoginId;
   private EditText mPassword;
   private EditText mConfirmPassword;
   private EditText mName;
   private TextView mConfirmPasswordTag;
   private TextView mUsernameTag;
   private TextView mErrorText;
   private TextView mCreateAccountText;
   private CheckBox mTermsAgreeCheckBox;

   private ProgressBar mLoadingSpinner;
   private LinearLayout mRegisterAgreement;

   private boolean mReadyForRegisterState;

   private APIReceiver mApiReceiver = new APIReceiver() {
      public void onSuccess(Object result, Intent intent) {
         User user = (User)result;
         ((MainApplication)getActivity().getApplication()).login(user);

         for (LoginListener loginListener : loginListeners) {
            loginListener.onLogin(user);
         }
      }

      public void onFailure(APIService.Error error, Intent intent) {
         enable(true);
         mLoadingSpinner.setVisibility(View.GONE);
         mErrorText.setVisibility(View.VISIBLE);
      }
   };

   /**
    * Required for restoring fragments
    */
   public LoginFragment() {
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null) {
         mReadyForRegisterState = savedInstanceState.getBoolean(LOGIN_STATE);
      } else {
         mReadyForRegisterState = false;
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.login_fragment, container, false);

      mLoginId = (EditText)view.findViewById(R.id.edit_login_id);
      mPassword = (EditText)view.findViewById(R.id.edit_password);
      mLogin = (ImageButton)view.findViewById(R.id.signin_button);
      mRegister = (ImageButton)view.findViewById(R.id.register_button);
      mCancelRegister = (ImageButton)view.findViewById(R.id.cancel_register_button);
      mGoogleLogin = (ImageButton)view.findViewById(R.id.use_google_login_button);
      mYahooLogin = (ImageButton)view.findViewById(R.id.use_yahoo_login_button);

      mConfirmPasswordTag = (TextView)view.findViewById(R.id.confirm_password);
      mUsernameTag = (TextView)view.findViewById(R.id.login_username);
      mConfirmPassword = (EditText)view.findViewById(R.id.edit_confirm_password);
      mName = (EditText)view.findViewById(R.id.edit_login_username);
      mErrorText = (TextView)view.findViewById(R.id.login_error_text);
      mErrorText.setVisibility(View.GONE);

      mRegisterAgreement = (LinearLayout)view.findViewById(R.id.login_agreement_terms_layout);
      TextView termsAgree = (TextView)view.findViewById(R.id.login_agreement_terms_textview);

      termsAgree.setMovementMethod(LinkMovementMethod.getInstance());

      mTermsAgreeCheckBox = (CheckBox)view.findViewById(R.id.login_agreement_terms_checkbox);

      mLoadingSpinner = (ProgressBar)view.findViewById(R.id.login_loading_bar);
      mLoadingSpinner.setVisibility(View.GONE);

      mCreateAccountText = (TextView)view.findViewById(R.id.create_account_header);
      mCreateAccountText.setVisibility(View.GONE);

      setState();

      mLogin.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            InputMethodManager in = (InputMethodManager)mContext
             .getSystemService(Context.INPUT_METHOD_SERVICE);

            in.hideSoftInputFromWindow(mLoginId.getApplicationWindowToken(),
             InputMethodManager.HIDE_NOT_ALWAYS);
            login();
         }
      });

      mGoogleLogin.setOnClickListener(this);
      mYahooLogin.setOnClickListener(this);
      mRegister.setOnClickListener(this);
      mCancelRegister.setOnClickListener(this);

      return view;
   }

   private void setState() {
      /**
       * TODO This is super ugly. Can you wrap all of these elements in a single
       * layout that you hide/show?
       */
      mErrorText.setVisibility(View.GONE);
      if (mReadyForRegisterState) {
         mErrorText.setVisibility(View.GONE);
         mCreateAccountText.setVisibility(View.VISIBLE);
         mConfirmPasswordTag.setVisibility(View.VISIBLE);
         mUsernameTag.setVisibility(View.VISIBLE);
         mConfirmPassword.setVisibility(View.VISIBLE);
         mName.setVisibility(View.VISIBLE);
         mCancelRegister.setVisibility(View.VISIBLE);
         mRegisterAgreement.setVisibility(View.VISIBLE);

         mGoogleLogin.setVisibility(View.GONE);
         mYahooLogin.setVisibility(View.GONE);
         mLogin.setVisibility(View.GONE);
         mCreateAccountText.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mUsernameTag.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mName.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mConfirmPasswordTag.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mConfirmPassword.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mRegisterAgreement.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
      } else {
         mCreateAccountText.clearAnimation();
         mCreateAccountText.setVisibility(View.GONE);
         mConfirmPasswordTag.clearAnimation();
         mConfirmPasswordTag.setVisibility(View.GONE);
         mUsernameTag.clearAnimation();
         mUsernameTag.setVisibility(View.GONE);
         mConfirmPassword.clearAnimation();
         mConfirmPassword.setVisibility(View.GONE);
         mName.clearAnimation();
         mName.setVisibility(View.GONE);
         mRegisterAgreement.clearAnimation();
         mRegisterAgreement.setVisibility(View.GONE);
         // _registerAgreement.clearAnimation();
         // _registerAgreement.setVisibility(View.GONE);
         mCancelRegister.clearAnimation();
         mCancelRegister.setVisibility(View.INVISIBLE);
         mGoogleLogin.setVisibility(View.VISIBLE);
         mYahooLogin.setVisibility(View.VISIBLE);
         mLogin.setVisibility(View.VISIBLE);
      }
   }

   private void login() {
      String login = mLoginId.getText().toString();
      String password = mPassword.getText().toString();

      if (login.length() > 1 && password.length() > 1 && login.contains("@")) {
         mLoadingSpinner.setVisibility(View.VISIBLE);
         enable(false);
         mContext.startService(APIService.getLoginIntent(mContext, login, password));
      } else {
         mErrorText.setVisibility(View.VISIBLE);
         if (!login.contains("@") || login.length() <= 1) {
            mLoginId.requestFocus();
            showKeyboard();
         } else {
            mPassword.requestFocus();
            showKeyboard();
         }
      }
   }

   private void showKeyboard() {
      InputMethodManager in = (InputMethodManager)mContext
       .getSystemService(Context.INPUT_METHOD_SERVICE);

      in.toggleSoftInput(InputMethodManager.SHOW_FORCED,
       InputMethodManager.HIDE_IMPLICIT_ONLY);
   }

   private void enable(boolean enabled) {
      mLoginId.setEnabled(enabled);
      mPassword.setEnabled(enabled);
      mLogin.setEnabled(enabled);
      mRegister.setEnabled(enabled);
      mCancelRegister.setEnabled(enabled);
      mGoogleLogin.setEnabled(enabled);
      mYahooLogin.setEnabled(enabled);
      mName.setEnabled(enabled);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(LOGIN_STATE, mReadyForRegisterState);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      mContext = activity;
   }

   public static LoginFragment newInstance() {
      return new LoginFragment();
   }

   @Override
   public void onClick(View v) {
      Intent intent;
      switch (v.getId()) {
      case R.id.use_google_login_button:
         intent = new Intent(mContext, OpenIDActivity.class);
         intent.putExtra(OpenIDActivity.LOGIN_METHOD, OpenIDActivity.GOOGLE_LOGIN);
         startActivityForResult(intent, OPEN_ID_RESULT_CODE);
         break;

      case R.id.use_yahoo_login_button:
         intent = new Intent(mContext, OpenIDActivity.class);
         intent.putExtra(OpenIDActivity.LOGIN_METHOD, OpenIDActivity.YAHOO_LOGIN);
         startActivityForResult(intent, OPEN_ID_RESULT_CODE);
         break;

      case R.id.register_button:
         if (!mReadyForRegisterState) {
            mReadyForRegisterState = true;
            setState();
         } else {
            String login = mLoginId.getText().toString();
            String name = mName.getText().toString();
            String password = mPassword.getText().toString();
            String confirmPassword = mConfirmPassword.getText().toString();

            if (password.equals(confirmPassword) &&
             login.length() > 1 && login.contains("@") &&
             name.length() > 1 && mTermsAgreeCheckBox.isChecked()) {
               enable(false);
               mContext.startService(APIService.getRegisterIntent(mContext, login,
                password, name));
            } else {
               mErrorText.setVisibility(View.VISIBLE);
               if (!login.contains("@") || login.length() <= 1) {
                  mLoginId.requestFocus();
                  showKeyboard();
               } else if (password.length() <= 1) {
                  mPassword.requestFocus();
                  showKeyboard();
               } else if (name.length() <= 1) {
                  mName.requestFocus();
                  showKeyboard();
               }
            }
         }
         break;

      case R.id.cancel_register_button:
         mReadyForRegisterState = false;
         setState();
         break;
      }

   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (resultCode == Activity.RESULT_OK) {
         if (data != null) {
            mLoadingSpinner.setVisibility(View.VISIBLE);
            String session = data.getStringExtra("session");
            enable(false);
            mContext.startService(APIService.getLoginIntent(mContext, session));
         }
      }
   }

   @Override
   public void onResume() {
      super.onResume();

      IntentFilter filter = new IntentFilter();
      filter.addAction(APIEndpoint.LOGIN.mAction);
      filter.addAction(APIEndpoint.REGISTER.mAction);
      mContext.registerReceiver(mApiReceiver, filter);
   }

   @Override
   public void onPause() {
      super.onPause();

      try {
         mContext.unregisterReceiver(mApiReceiver);
      } catch (IllegalArgumentException e) {
         // Do nothing. This happens in the unlikely event that
         // unregisterReceiver has been called already.
      }
   }

   public static void registerOnLoginListener(LoginListener l) {
      LoginFragment.loginListeners.add(l);
   }

   static AlertDialog getLogoutDialog(final Context context) {
      return createLogoutDialog(context, R.string.logout_title,
       R.string.logout_messege, R.string.logout_confirm, R.string.logout_cancel);
   }

   private static AlertDialog createLogoutDialog(final Context context,
    int titleRes, int messageRes, int buttonConfirm, int buttonCancel) {
      HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
      builder
         .setTitle(context.getString(titleRes))
         .setMessage(context.getString(messageRes))
         .setPositiveButton(context.getString(buttonConfirm),
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  HTTPRequestHelper.clearCookies(context);

                  for (LoginListener loginListener : loginListeners) {
                     loginListener.onLogout();
                  }

                  MediaFragment.showingLogout = false;
                  dialog.cancel();
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

   public static void clearLoginListeners() {
      loginListeners.clear();
   }
}
