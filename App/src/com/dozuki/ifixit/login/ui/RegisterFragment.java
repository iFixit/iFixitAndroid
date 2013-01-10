package com.dozuki.ifixit.login.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;

public class RegisterFragment extends DialogFragment implements OnClickListener {
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
   private APICall mCurAPICall;

   @Subscribe
   public void onRegister(APIEvent.Register event) {
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
       
         mLoginId.setVisibility(View.VISIBLE);
         mPassword.setVisibility(View.VISIBLE);
         mConfirmPassword.setVisibility(View.VISIBLE);
         mName.setVisibility(View.VISIBLE);

         mErrorText.setVisibility(View.VISIBLE);
         mErrorText.setText(error.mMessage);
      }
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
      mTermsAgreeText.setText(R.string.register_agreement);
      mTermsAgreeText.setMovementMethod(LinkMovementMethod.getInstance());
      
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
        
            if (password.equals(confirmPassword) && login.length() > 0 &&
             name.length() > 0 && mTermsAgreeCheckBox.isChecked()) {
               enable(false);
               mLoginId.setVisibility(View.GONE);
               mPassword.setVisibility(View.GONE);
               mConfirmPassword.setVisibility(View.GONE);
               mName.setVisibility(View.GONE);

               mErrorText.setVisibility(View.GONE);
               mLoadingSpinner.setVisibility(View.VISIBLE);
               mCurAPICall = APIService.getRegisterAPICall(login, password, name);
               APIService.call((Activity)getActivity(), mCurAPICall);
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
             FragmentManager fragmentManager = getSupportFragmentManager();

              // Go back to login.
             fragmentManager.beginTransaction()
              .remove(this)
              .add(new LoginFragment(), null)
              .commit();
               
              break;
       }
   }

   @Override
   public void onCancel(DialogInterface dialog) {
      MainApplication.get().cancelLogin();
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
}
