package com.dozuki.ifixit.model.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class Authenticator extends AbstractAccountAuthenticator {
   public static final String AUTH_TOKEN_TYPE_FULL_ACCESS = "Full access";
   public static final String ACCOUNT_TYPE = "com.dozuki.dozuki";

   private final Context mContext;

   public Authenticator(Context context) {
      super(context);

      mContext = context;
   }

   @Override
   public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
    String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
      Log.w("Authenticator", "addAccount not implemented");
      // Creates an Intent to start the authentication activity.
      return null;
   }

   @Override
   public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
    String authTokenType, Bundle options) throws NetworkErrorException {
      Log.w("Authenticator", "getAuthToken");

      if (!authTokenType.equals(AUTH_TOKEN_TYPE_FULL_ACCESS)) {
         Log.w("Authenticator", "Invalid auth token type");
         return null;
      }

      AccountManager accountManager = AccountManager.get(mContext);

      String authToken = accountManager.peekAuthToken(account, authTokenType);

      if (authToken == null) {
         String password = accountManager.getPassword(account);
         if (password != null) {
            // Retry authentication.
         }
      }

      if (authToken != null) {
         Bundle result = new Bundle();
         result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
         result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
         result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
         return result;
      }

      // Create bundle for launching authentication activity.

      return null;
   }

   @Override
   public String getAuthTokenLabel(String authTokenType) {
      // TODO: Create string resource for it.
      return authTokenType;
   }

   @Override
   public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
    String[] features) throws NetworkErrorException {
      Bundle result = new Bundle();
      result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
      return result;
   }

   public void onAccountAuthenticated(String userName, String password, String authToken) {
      AccountManager accountManager = AccountManager.get(mContext);
      Account account = new Account(userName, ACCOUNT_TYPE);

      // TODO: Add useful data in the Bundle.
      accountManager.addAccountExplicitly(account, password, null);
      accountManager.setAuthToken(account, AUTH_TOKEN_TYPE_FULL_ACCESS, authToken);
   }

   @Override
   public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
    Bundle options) throws NetworkErrorException {
      Log.w("Authenticator", "confirmCredentials not implemented");
      return null;
   }

   @Override
   public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
    String authTokenType, Bundle options) throws NetworkErrorException {
      Log.w("Authenticator", "updateCredentials not implemented");
      return null;
   }

   @Override
   public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
      Log.w("Authenticator", "editProperties not implemented");
      return null;
   }
}
