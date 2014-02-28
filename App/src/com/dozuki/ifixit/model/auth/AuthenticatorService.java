package com.dozuki.ifixit.model.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
   @Override
   public IBinder onBind(Intent intent) {
      Authenticator authenticator = new Authenticator(this);
      return authenticator.getIBinder();
   }
}
