package com.dozuki.ifixit.view.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.model.TopicNode;
import com.dozuki.ifixit.view.model.User;

public class LoginActivity extends SherlockFragmentActivity implements OnClickListener {

	private static final int OPEN_ID_RESULT_CODE = 0;
	private Button _login;
	private Button _register;
	private Button _googleLogin;
	private Button _yahooLogin;

	private EditText _username;
	private EditText _password;
	
	
	
	private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			APIService.Result result = (APIService.Result) intent.getExtras()
					.getSerializable(APIService.RESULT);

			if (!result.hasError()) {
				User user =  (User)result.getResult();
				Log.e("Login Activity", "Welcome " + user.getUsername());
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		_username = (EditText) findViewById(R.id.edit_username);
		_password = (EditText) findViewById(R.id.edit_password);
		_login = (Button) findViewById(R.id.signin_button);
		_register = (Button) findViewById(R.id.register_button);
		_googleLogin = (Button) findViewById(R.id.use_google_login_button);
		_yahooLogin = (Button) findViewById(R.id.use_yahoo_login_button);

		_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}

		});
		
		_googleLogin.setOnClickListener(this);
		_yahooLogin.setOnClickListener(this);
		
	}

	private void login() {
		startService(APIService.getLoginIntent(this, _username.getText().toString(), _password.getText().toString(), null));
	}
	
	   @Override
	   public void onResume() {
	      super.onResume();

	      IntentFilter filter = new IntentFilter();
	      filter.addAction(APIService.ACTION_LOGIN);
	      registerReceiver(mApiReceiver, filter);
	   }
	   
	   
	   @Override
	   public void onPause() {
	      super.onPause();

	      try {
	         unregisterReceiver(mApiReceiver);
	      } catch (IllegalArgumentException e) {
	         // Do nothing. This happens in the unlikely event that
	         // unregisterReceiver has been called already.
	      }
	   }

	@Override
	public void onClick(View v) {

		Intent i;
       switch(v.getId())
       {
          case R.id.use_google_login_button:
        	 i = new Intent(this, OpenIDActivity.class);
        	  i.putExtra(OpenIDActivity.LOGIN_METHOD, OpenIDActivity.GOOGLE_LOGIN);
    	      startActivityForResult(i, OPEN_ID_RESULT_CODE);
    	   break;
    	   
    	   
          case R.id.use_yahoo_login_button:
        	  i = new Intent(this, OpenIDActivity.class);
        	  i.putExtra(OpenIDActivity.LOGIN_METHOD, OpenIDActivity.YAHOO_LOGIN);
        	  startActivityForResult(i, OPEN_ID_RESULT_CODE);
       	   break;
       }
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	   
	    if(resultCode == Activity.RESULT_OK)
	    {

	    	String session = data.getStringExtra("session");
	    	startService(APIService.getLoginIntent(this, null, null, session));
	    }
	}
	   

}
