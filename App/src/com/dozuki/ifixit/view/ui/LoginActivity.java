package com.dozuki.ifixit.view.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.R;

public class LoginActivity extends SherlockFragmentActivity {

	private Button _login;
	private Button _register;

	private EditText _username;
	private EditText _password;

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
		Intent i = new Intent(this, MediaActivity.class);
		startActivity(i);
	}
}
