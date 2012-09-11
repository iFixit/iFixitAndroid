package com.dozuki.ifixit.view.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.view.model.TopicNode;
import com.dozuki.ifixit.view.model.TopicSelectedListener;


public class LoginFragment extends SherlockFragment
 implements TopicSelectedListener, OnItemClickListener {
   private static final String CURRENT_TOPIC = "CURRENT_TOPIC";

   private Context mContext;
	private Button _login;
	private Button _register;
	private Button _googleLogin;
	private Button _yahooLogin;

	private EditText _username;
	private EditText _password;
	

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
     

      return view;
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

   
   public static SherlockFragment newInstance() {
	   LoginFragment mFrgment = new LoginFragment();
       return mFrgment;
   }

@Override
public void onTopicSelected(TopicNode topic) {
	// TODO Auto-generated method stub
	
}




}
