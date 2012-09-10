package com.dozuki.ifixit.view.ui;

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

public class LoginActivity extends SherlockFragmentActivity {

	private Button _login;
	private Button _register;

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

		_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}

		});
	}

	private void login() {
		startService(APIService.getLoginIntent(this, _username.getText().toString(), _password.getText().toString()));
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
	   

}
