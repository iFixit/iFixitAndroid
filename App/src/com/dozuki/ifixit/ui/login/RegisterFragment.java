package com.dozuki.ifixit.ui.login;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.method.LinkMovementMethod;
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
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.BaseDialogFragment;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.otto.Subscribe;

public class RegisterFragment extends BaseDialogFragment implements OnClickListener {
   private Button mRegister;
   private Button mCancelRegister;
   private EditText mLoginId;
   private EditText mPassword;
   private EditText mConfirmPassword;
   private EditText mName;
   private TextView mErrorText;
   private TextView mTermsAgreeText;
   private CheckBox mTermsAgreeCheckBox;
   private ProgressBar mLoadingSpinner;

   @Subscribe
   public void onRegister(APIEvent.Register event) {
      if (!event.hasError()) {
         User user = event.getResult();
         ((MainApplication)getActivity().getApplication()).login(user);

         dismiss();
      } else {
         enable(true);
         APIError error = event.getError();
         if (error.mType == APIError.Type.CONNECTION ||
          error.mType == APIError.Type.PARSE) {
            APIService.getErrorDialog(getActivity(), event).show();
         }
         mLoadingSpinner.setVisibility(View.GONE);

         mLoginId.setVisibility(View.VISIBLE);
         mPassword.setVisibility(View.VISIBLE);
         mConfirmPassword.setVisibility(View.VISIBLE);
         mName.setVisibility(View.VISIBLE);
         mTermsAgreeText.setVisibility(View.VISIBLE);
         mTermsAgreeCheckBox.setVisibility(View.VISIBLE);

         mErrorText.setVisibility(View.VISIBLE);
         mErrorText.setText(error.mMessage);
      }
   }

   public static RegisterFragment newInstance() {
      RegisterFragment frag = new RegisterFragment();
      frag.setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
      return frag;
   }

   /**
    * Required for restoring fragments
    */
   public RegisterFragment() {}

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.register_fragment, container, false);

      mLoginId = (EditText)view.findViewById(R.id.edit_login_id);
      mPassword = (EditText)view.findViewById(R.id.edit_password);
      mConfirmPassword = (EditText)view.findViewById(R.id.edit_confirm_password);

      // Password fields default to a courier typeface (very annoying) and
      // setting the font-family in xml does nothing, so we have to set it
      // explicitly here
      mPassword.setTypeface(Typeface.DEFAULT);
      mConfirmPassword.setTypeface(Typeface.DEFAULT);
      mName = (EditText)view.findViewById(R.id.edit_login_username);

      mRegister = (Button)view.findViewById(R.id.register_button);
      mCancelRegister = (Button)view.findViewById(R.id.cancel_register_button);

      mErrorText = (TextView)view.findViewById(R.id.login_error_text);
      mErrorText.setVisibility(View.GONE);

      mTermsAgreeCheckBox = (CheckBox)view.findViewById(R.id.login_agreement_terms_checkbox);
      mTermsAgreeText = (TextView)view.findViewById(R.id.login_agreement_terms_textview);
      if (MainApplication.get().getSite().isIfixit()) {
         mTermsAgreeText.setText(R.string.register_agreement);
         mTermsAgreeText.setMovementMethod(LinkMovementMethod.getInstance());
      } else {
         mTermsAgreeCheckBox.setVisibility(View.GONE);
         mTermsAgreeText.setVisibility(View.GONE);
      }

      mLoadingSpinner = (ProgressBar)view.findViewById(R.id.login_loading_bar);
      mLoadingSpinner.setVisibility(View.GONE);

      mRegister.setOnClickListener(this);
      mCancelRegister.setOnClickListener(this);

      return view;
   }

   @Override
   public void onStart() {
      super.onStart();

      Tracker tracker = MainApplication.getGaTracker();
      tracker.set(Fields.SCREEN_NAME, "/register");

      tracker.send(MapBuilder.createAppView().build());
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
   public void onClick(View v) {
      switch (v.getId()) {
         case R.id.register_button:
            String login = mLoginId.getText().toString();
            String name = mName.getText().toString();
            String password = mPassword.getText().toString();
            String confirmPassword = mConfirmPassword.getText().toString();

            if (password.equals(confirmPassword) && login.length() > 0 &&
             name.length() > 0 && (!MainApplication.get().getSite().isIfixit() || mTermsAgreeCheckBox.isChecked())) {
               enable(false);
               mLoginId.setVisibility(View.GONE);
               mPassword.setVisibility(View.GONE);
               mConfirmPassword.setVisibility(View.GONE);
               mName.setVisibility(View.GONE);
               mTermsAgreeText.setVisibility(View.GONE);
               mTermsAgreeCheckBox.setVisibility(View.GONE);

               mErrorText.setVisibility(View.GONE);
               mLoadingSpinner.setVisibility(View.VISIBLE);
               APIService.call(getActivity(),
                APIService.getRegisterAPICall(login, password, name));
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
             FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

              // Go back to login.
             fragmentManager.beginTransaction()
              .remove(this)
              .add(LoginFragment.newInstance(), null)
              .commit();

              break;
       }
   }

   @Override
   public void onCancel(DialogInterface dialog) {
      MainApplication.get().cancelLogin();
   }
}
