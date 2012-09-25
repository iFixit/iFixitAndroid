package com.dozuki.ifixit.view.ui; 


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.model.AuthenicationPackage;
import com.dozuki.ifixit.view.model.User;
import com.dozuki.ifixit.view.model.LoginListener;


public class LoginFragment extends SherlockFragment
 implements  OnClickListener {
   
   static ArrayList<LoginListener> loginListeners = new ArrayList<LoginListener>();

    private Context mContext;
	private Button _login;
	private Button _register;
	private Button _cancelRegister;
	private Button _googleLogin;
	private Button _yahooLogin;
	
	static final int OPEN_ID_RESULT_CODE = 4;
	static final String LOGIN_STATE = "LOGIN_STATE";
	static final String PREFERENCE_FILE = "PREFERENCE_FILE";
	static final String SESSION_KEY = "SESSION_KEY";
	static final String USERNAME_KEY = "USERNAME_KEY";
   
	private EditText _loginId;
	private EditText _password;
	private EditText _confirmPassword;
	private EditText _name;
	private TextView _confirmPasswordTag;
	private TextView _usernameTag;
	
	boolean readyForRegisterState;
	
	private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			APIService.Result result = (APIService.Result) intent.getExtras()
					.getSerializable(APIService.RESULT);

			if (!result.hasError()) {
				User lUser =  (User)result.getResult();
				((MainApplication)((Activity)mContext).getApplication()).setUser(lUser);
				
				final SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
				Editor editor = prefs.edit();
				editor.putString(SESSION_KEY, lUser.getSession());
				editor.putString(USERNAME_KEY, lUser.getUsername());
				editor.commit();
				for(LoginListener l : loginListeners)
				{
					l.onLogin(lUser);
				}
			}
		}
	};

   /**
    * Required for restoring fragments
    */
   public LoginFragment() {}


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null) {
    	  //readyForRegisterState
    	  readyForRegisterState = savedInstanceState.getBoolean(LOGIN_STATE);
      }else
      {
    	  readyForRegisterState=false;
      }
      
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
	 Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.login_fragment, container,
       false);

  	_loginId = (EditText) view.findViewById(R.id.edit_login_id);
	_password = (EditText) view.findViewById(R.id.edit_password);
	_login = (Button) view.findViewById(R.id.signin_button);
	_register = (Button) view.findViewById(R.id.register_button);
	_cancelRegister = (Button) view.findViewById(R.id.cancel_register_button);
	_googleLogin = (Button) view.findViewById(R.id.use_google_login_button);
	_yahooLogin = (Button) view.findViewById(R.id.use_yahoo_login_button);
	
	_loginId.setText("aaron@fireswing.com");
	_password.setText("LN8-7bT2");
	
	_confirmPasswordTag =  (TextView) view.findViewById(R.id.confirm_password);
	_usernameTag = (TextView) view.findViewById(R.id.login_username);
	_confirmPassword = (EditText) view.findViewById(R.id.edit_confirm_password);	
	_name = (EditText) view.findViewById(R.id.edit_login_username); 
	
	 setState();

	
	_login.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			login();
		}

	});
	
	_googleLogin.setOnClickListener(this);
	_yahooLogin.setOnClickListener(this);
	_register.setOnClickListener(this);
	_cancelRegister.setOnClickListener(this);
	
      return view;
   }
   

	private void setState() {
		if (readyForRegisterState) {
			_confirmPasswordTag.setVisibility(View.VISIBLE);
			_usernameTag.setVisibility(View.VISIBLE);
			_confirmPassword.setVisibility(View.VISIBLE);
			_name.setVisibility(View.VISIBLE);
			_cancelRegister.setVisibility(View.VISIBLE);

			_googleLogin.setVisibility(View.GONE);
			_yahooLogin.setVisibility(View.GONE);
			_login.setVisibility(View.GONE);

			_usernameTag.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.fade_in));
			_name.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.fade_in));
			_confirmPasswordTag.startAnimation(AnimationUtils.loadAnimation(
					mContext, R.anim.fade_in));
			_confirmPassword.startAnimation(AnimationUtils.loadAnimation(
					mContext, R.anim.fade_in));
		} else {
			_confirmPasswordTag.clearAnimation();
			_confirmPasswordTag.setVisibility(View.GONE);
			_usernameTag.clearAnimation();
			_usernameTag.setVisibility(View.GONE);
			_confirmPassword.clearAnimation();
			_confirmPassword.setVisibility(View.GONE);
			_name.clearAnimation();
			_name.setVisibility(View.GONE);
			_cancelRegister.clearAnimation();
			_cancelRegister.setVisibility(View.GONE);
			_googleLogin.setVisibility(View.VISIBLE);
			_yahooLogin.setVisibility(View.VISIBLE);
			_login.setVisibility(View.VISIBLE);
		}

    }


	private void login() {
		
		if( _loginId.getText().toString().length() > 1 &&  _password.getText().toString().length() > 1)
		{
			AuthenicationPackage authenicationPackage = new AuthenicationPackage();
			authenicationPackage.login = _loginId.getText().toString();
			authenicationPackage.password =  _password.getText().toString();
		   mContext.startService(APIService.getLoginIntent(mContext, authenicationPackage));
		}
	}


   
   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

       outState.putSerializable(LOGIN_STATE, readyForRegisterState);
   }

   public void onItemClick(AdapterView<?> adapterView, View view,
    int position, long id) {
     // mTopicAdapter.onItemClick(null, view, position, id);
   }


   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      try {
        // topicSelectedListener = (TopicSelectedListener)activity;
         mContext = (Context)activity;
      } catch (ClassCastException e) {
         throw new ClassCastException(activity.toString() +
          " must implement TopicSelectedListener");
      }
   }

   
   public static LoginFragment newInstance() {
	   LoginFragment mFrgment = new LoginFragment();
       return mFrgment;
   }



@Override
public void onClick(View v) {

	Intent i;
   switch(v.getId())
   {
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
    	  if(!readyForRegisterState)
    	  {
    		readyForRegisterState=true;
    		setState();
    	  }else
 {//
				if (_password.getText().toString()
						.equals(_confirmPassword.getText().toString())) {
					// start service for register
					AuthenicationPackage authenicationPackage = new AuthenicationPackage();
					authenicationPackage.login = _loginId.getText().toString();
					authenicationPackage.password = _password.getText()
							.toString();
					authenicationPackage.username = _name.getText().toString();
					mContext.startService(APIService.getRegisterIntent(mContext, authenicationPackage));
					
				}
			}
   	   break;
      case R.id.cancel_register_button:
    	  readyForRegisterState=false;
    	  setState();
    	  break;
   }
	
}


@Override
public void onActivityResult (int requestCode, int resultCode, Intent data)
{
	
	if(resultCode == Activity.RESULT_OK)
	if(data != null)
	{
		displayLoading();
	   
	   String session = data.getStringExtra("session");
	   AuthenicationPackage authenicationPackage = new AuthenicationPackage();
	   authenicationPackage.session = session;
	   mContext.startService(APIService.getLoginIntent(mContext, authenicationPackage));
	}
}

@Override
public void onResume() {
   super.onResume();

   IntentFilter filter = new IntentFilter();
   filter.addAction(APIService.ACTION_LOGIN);
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

 public static void registerOnLoginListener(LoginListener l)
 {
	 LoginFragment.loginListeners.add(l);
 }
 
 private void displayLoading() {
     FragmentTransaction ft = getActivity().getSupportFragmentManager().
      beginTransaction();
     ft.replace(R.id.login_overlay, new LoadingFragment());
     ft.commit();
  }
 
 
 
 static AlertDialog getLogoutDialog(final Context context) {
		      return createLogoutDialog(context, R.string.logout_title,
		       R.string.logout_messege, R.string.logout_confirm, R.string.logout_cancel);
		   }

 
 private static AlertDialog createLogoutDialog(final Context context, int titleRes, int messageRes,
		    int buttonConfirm,  int buttonCancel) {
		      HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
		      builder.setTitle(context.getString(titleRes))
		             .setMessage(context.getString(messageRes))
		             .setPositiveButton(context.getString(buttonConfirm),
		              new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int id) {
		                  
		                //	Log.e("WDWD", loginListeners.size()+"");
		                	for(LoginListener l : loginListeners)
		    				{
		    					l.onLogout();
		    				}
		    
		                   dialog.cancel();
		                }
		             }).setNegativeButton(context.getString(buttonCancel),
		              new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int id) {
		            

		                   dialog.cancel();
		                }
		             });

		      return builder.create();
		   }

}
