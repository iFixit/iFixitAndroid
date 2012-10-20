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
import android.widget.AdapterView;
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
import com.dozuki.ifixit.view.model.AuthenicationPackage;
import com.dozuki.ifixit.view.model.LoginListener;
import com.dozuki.ifixit.view.model.User;

public class LoginFragment extends SherlockFragment implements OnClickListener {

   static ArrayList<LoginListener> loginListeners = new ArrayList<LoginListener>();

   /**
    * TODO: All of these need to start with 'm' e.g. 'mLogin'
    */
   private Context mContext;
   private ImageButton mLogin;
   private ImageButton mRegister;
   private ImageButton mCancelRegister;
   private ImageButton mGoogleLogin;
   private ImageButton mYahooLogin;

   static final int OPEN_ID_RESULT_CODE = 4;
   static final String LOGIN_STATE = "LOGIN_STATE";

   private EditText mLoginId;
   private EditText mPassword;
   private EditText mConfirmPassword;
   private EditText mName;
   private TextView mConfirmPasswordTag;
   private TextView mUsernameTag;
   private TextView mErrorText;
   private TextView mCreateAccountText;

   private ProgressBar _loadingSpinner;
   private LinearLayout _registerAgreement;

   boolean readyForRegisterState;

   private APIReceiver mApiReceiver = new APIReceiver() {
      public void onSuccess(Object result, Intent intent) {
         User lUser = (User) result;
         ((MainApplication) ((Activity) mContext).getApplication()).login(lUser);

         for (LoginListener l : loginListeners) {
            l.onLogin(lUser);
         }
      }

      public void onFailure(APIService.Error error, Intent intent) {
         enable(true);
         _loadingSpinner.setVisibility(View.GONE);
         mErrorText.setVisibility(View.VISIBLE);
      }
   };

   private CheckBox _agreementCheckBox;

   /**
    * Required for restoring fragments
    */
   public LoginFragment() {
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null) {
         // readyForRegisterState
         readyForRegisterState = savedInstanceState.getBoolean(LOGIN_STATE);
      } else {
         readyForRegisterState = false;
      }

   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.login_fragment, container, false);

      mLoginId = (EditText) view.findViewById(R.id.edit_login_id);
      mPassword = (EditText) view.findViewById(R.id.edit_password);
      mLogin = (ImageButton) view.findViewById(R.id.signin_button);
      mRegister = (ImageButton) view.findViewById(R.id.register_button);
      mCancelRegister = (ImageButton) view.findViewById(R.id.cancel_register_button);
      mGoogleLogin = (ImageButton) view.findViewById(R.id.use_google_login_button);
      mYahooLogin = (ImageButton) view.findViewById(R.id.use_yahoo_login_button);

      mConfirmPasswordTag = (TextView) view.findViewById(R.id.confirm_password);
      mUsernameTag = (TextView) view.findViewById(R.id.login_username);
      mConfirmPassword = (EditText) view.findViewById(R.id.edit_confirm_password);
      mName = (EditText) view.findViewById(R.id.edit_login_username);
      mErrorText = (TextView) view.findViewById(R.id.login_error_text);
      mErrorText.setVisibility(View.GONE);

      _registerAgreement = (LinearLayout) view.findViewById(R.id.login_agreement_terms_layout);
      TextView t = (TextView) view.findViewById(R.id.login_agreement_terms_textview);

      t.setMovementMethod(LinkMovementMethod.getInstance());

      _agreementCheckBox = (CheckBox) view.findViewById(R.id.login_agreement_terms_checkbox);

      _loadingSpinner = (ProgressBar) view.findViewById(R.id.login_loading_bar);
      _loadingSpinner.setVisibility(View.GONE);

      mCreateAccountText = (TextView) view.findViewById(R.id.create_account_header);
      mCreateAccountText.setVisibility(View.GONE);
      // /_registerAgreement.setVisibility(View.GONE);

      setState();

      mLogin.setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View v) {
            InputMethodManager in = (InputMethodManager) mContext
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
      if (readyForRegisterState) {
         mErrorText.setVisibility(View.GONE);
         mCreateAccountText.setVisibility(View.VISIBLE);
         mConfirmPasswordTag.setVisibility(View.VISIBLE);
         mUsernameTag.setVisibility(View.VISIBLE);
         mConfirmPassword.setVisibility(View.VISIBLE);
         mName.setVisibility(View.VISIBLE);
         mCancelRegister.setVisibility(View.VISIBLE);
         _registerAgreement.setVisibility(View.VISIBLE);

         mGoogleLogin.setVisibility(View.GONE);
         mYahooLogin.setVisibility(View.GONE);
         mLogin.setVisibility(View.GONE);
         mCreateAccountText.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mUsernameTag.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mName.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mConfirmPasswordTag.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         mConfirmPassword.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
         _registerAgreement.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
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
         _registerAgreement.clearAnimation();
         _registerAgreement.setVisibility(View.GONE);
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

      if (mLoginId.getText().toString().length() > 1 && mPassword.getText().toString().length() > 1
            && mLoginId.getText().toString().contains("@")) {
         _loadingSpinner.setVisibility(View.VISIBLE);
         AuthenicationPackage authenicationPackage = new AuthenicationPackage();
         authenicationPackage.login = mLoginId.getText().toString();
         authenicationPackage.password = mPassword.getText().toString();
         enable(false);
         mContext.startService(APIService.getLoginIntent(mContext, authenicationPackage));
      } else {
         mErrorText.setVisibility(View.VISIBLE);
         if (!mLoginId.getText().toString().contains("@")
               || mLoginId.getText().toString().length() <= 1) {
            mLoginId.requestFocus();
            showKeyboard();
         } else {
            mPassword.requestFocus();
            showKeyboard();
         }
      }
   }

   public void showKeyboard() {
      InputMethodManager in = (InputMethodManager) mContext
            .getSystemService(Context.INPUT_METHOD_SERVICE);

      in.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
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

      outState.putSerializable(LOGIN_STATE, readyForRegisterState);
   }

   public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      // mTopicAdapter.onItemClick(null, view, position, id);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      try {
         // topicSelectedListener = (TopicSelectedListener)activity;
         mContext = (Context) activity;
      } catch (ClassCastException e) {
         throw new ClassCastException(activity.toString() + " must implement TopicSelectedListener");
      }
   }

   public static LoginFragment newInstance() {
      LoginFragment mFrgment = new LoginFragment();
      return mFrgment;
   }

   @Override
   public void onClick(View v) {

      Intent i;
      switch (v.getId()) {
      case R.id.use_google_login_button:
         i = new Intent(mContext, OpenIDActivity.class);
         i.putExtra(OpenIDActivity.LOGIN_METHOD, OpenIDActivity.GOOGLE_LOGIN);
         startActivityForResult(i, OPEN_ID_RESULT_CODE);
         break;

      case R.id.use_yahoo_login_button:
         i = new Intent(mContext, OpenIDActivity.class);
         i.putExtra(OpenIDActivity.LOGIN_METHOD, OpenIDActivity.YAHOO_LOGIN);
         startActivityForResult(i, OPEN_ID_RESULT_CODE);

      case R.id.register_button:
         if (!readyForRegisterState) {
            readyForRegisterState = true;
            setState();

         } else {//
            if (mPassword.getText().toString().equals(mConfirmPassword.getText().toString())
                  && mLoginId.getText().length() > 1 && mLoginId.getText().toString().contains("@")
                  && mName.getText().length() > 1 && _agreementCheckBox.isChecked()) {
               // start service for register
               AuthenicationPackage authenicationPackage = new AuthenicationPackage();
               authenicationPackage.login = mLoginId.getText().toString();
               authenicationPackage.password = mPassword.getText().toString();
               authenicationPackage.username = mName.getText().toString();
               enable(false);
               mContext.startService(APIService.getRegisterIntent(mContext, authenicationPackage));

            } else {
               mErrorText.setVisibility(View.VISIBLE);
               if (!mLoginId.getText().toString().contains("@")
                     || mLoginId.getText().toString().length() <= 1) {
                  mLoginId.requestFocus();
                  showKeyboard();
               } else if (mPassword.getText().toString().length() <= 1) {
                  mPassword.requestFocus();
                  showKeyboard();
               } else if (mName.getText().toString().length() <= 1) {
                  mName.requestFocus();
                  showKeyboard();
               }
            }
         }
         break;
      case R.id.cancel_register_button:
         readyForRegisterState = false;
         setState();
         break;
      }

   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {

      if (resultCode == Activity.RESULT_OK)
         if (data != null) {
            _loadingSpinner.setVisibility(View.VISIBLE);
            String session = data.getStringExtra("session");
            AuthenicationPackage authenicationPackage = new AuthenicationPackage();
            authenicationPackage.session = session;
            enable(false);
            mContext.startService(APIService.getLoginIntent(mContext, authenicationPackage));
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
      return createLogoutDialog(context, R.string.logout_title, R.string.logout_messege,
            R.string.logout_confirm, R.string.logout_cancel);
   }

   private static AlertDialog createLogoutDialog(final Context context, int titleRes,
         int messageRes, int buttonConfirm, int buttonCancel) {
      HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
      builder
            .setTitle(context.getString(titleRes))
            .setMessage(context.getString(messageRes))
            .setPositiveButton(context.getString(buttonConfirm),
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {

                        HTTPRequestHelper.clearCookies(context);
                        for (LoginListener l : loginListeners) {
                           l.onLogout();
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
      AlertDialog d = builder.create();
      d.setCancelable(false);
      return d;
   }

   public static void clearLoginListeners() {
      loginListeners.clear();

   }
}
