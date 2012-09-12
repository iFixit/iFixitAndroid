package com.dozuki.ifixit.view.ui;


import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.model.TopicNode;
import com.dozuki.ifixit.view.model.TopicSelectedListener;
import com.dozuki.ifixit.view.model.User;
import com.dozuki.ifixit.view.model.LoginListener;


public class LoginFragment extends SherlockFragment
 implements  OnClickListener {
   
   static ArrayList<LoginListener> loginListeners = new ArrayList<LoginListener>();

    private Context mContext;
	private Button _login;
	private Button _register;
	private Button _googleLogin;
	private Button _yahooLogin;
	
	static final int OPEN_ID_RESULT_CODE = 4;
   
	private EditText _username;
	private EditText _password;
	

	
	private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			APIService.Result result = (APIService.Result) intent.getExtras()
					.getSerializable(APIService.RESULT);

			if (!result.hasError()) {
				User lUser =  (User)result.getResult();
				((MainApplication)((Activity)mContext).getApplication()).setUser(lUser);
				
				for(LoginListener l : loginListeners)
				{
					l.onLogin(lUser);
				}
				Log.e("Login Activity", "Welcome " + lUser.getUsername());
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
       
      }
      
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
	 Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.login, container,
       false);

  	_username = (EditText) view.findViewById(R.id.edit_username);
	_password = (EditText) view.findViewById(R.id.edit_password);
	_login = (Button) view.findViewById(R.id.signin_button);
	_register = (Button) view.findViewById(R.id.register_button);
	_googleLogin = (Button) view.findViewById(R.id.use_google_login_button);
	_yahooLogin = (Button) view.findViewById(R.id.use_yahoo_login_button);
	
	_login.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			login();
		}

	});
	
	_googleLogin.setOnClickListener(this);
	_yahooLogin.setOnClickListener(this);
	
      return view;
   }
   

	private void login() {
		mContext.startService(APIService.getLoginIntent(mContext, _username.getText().toString(), _password.getText().toString(), null));
	}


   
   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      //outState.putSerializable(CURRENT_TOPIC, mTopic);
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
   	   break;
   }
	
}


@Override
public void onActivityResult (int requestCode, int resultCode, Intent data)
{
	Log.e("LOGIN", requestCode+"");
	Log.e("Login Activity", "Welcome " + requestCode);
	String session = data.getStringExtra("session");
	mContext.startService(APIService.getLoginIntent(mContext, null, null, session));
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

 public void registerOnLoginListener(LoginListener l)
 {
	 LoginFragment.loginListeners.add(l);
 }

}
