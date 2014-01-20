package com.dozuki.ifixit.model.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.User;

public class Authenticator extends AbstractAccountAuthenticator {
   public static final String AUTH_TOKEN_TYPE_FULL_ACCESS = "Full access";
   public static final String ACCOUNT_TYPE = "com.dozuki.dozuki";
   private static final String USER_DATA_SITE_NAME = "USER_DATA_SITE_NAME";
   private static final String USER_DATA_USER_NAME = "USER_DATA_USER_NAME";
   private static final String USER_DATA_USERID = "USER_DATA_USERID";
   private static final String USER_DATA_AUTH_TOKEN = "USER_DATA_AUTH_TOKEN";
   private static final String USER_DATA_EMAIL = "USER_DATA_EMAIL";

   private final Context mContext;
   private final AccountManager mAccountManager;

   public Authenticator(Context context) {
      super(context);

      mContext = context;
      mAccountManager = AccountManager.get(mContext);
   }

   @Override
   public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
    String authTokenType, String[] requiredFeatures, Bundle options)
    throws NetworkErrorException {
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

      String authToken = mAccountManager.peekAuthToken(account, authTokenType);

      if (authToken == null) {
         String password = mAccountManager.getPassword(account);
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

   public Account onAccountAuthenticated(Site site, String email, String userName,
    int userid, String password, String authToken) {
      Account account = new Account(userName, ACCOUNT_TYPE);

      Bundle userData = new Bundle();
      userData.putString(USER_DATA_SITE_NAME, site.mName);
      userData.putString(USER_DATA_EMAIL, email);
      userData.putString(USER_DATA_USER_NAME, userName);
      userData.putString(USER_DATA_USERID, "" + userid);

      // TODO: This is already stored in the AccountManager. This isn't strictly
      // necessary but it makes it easier... Decide to remove or not remove it.
      userData.putString(USER_DATA_AUTH_TOKEN, authToken);

      mAccountManager.addAccountExplicitly(account, password, userData);
      mAccountManager.setAuthToken(account, AUTH_TOKEN_TYPE_FULL_ACCESS, authToken);

      return account;
   }

   public Account getAccountForSite(Site site) {
      String siteName = site.mName;

      for (Account account : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
         if (mAccountManager.getUserData(account, USER_DATA_SITE_NAME).equals(siteName)) {
            return account;
         }
      }

      return null;
   }

   public User createUser(Account account) {
      User user = new User();

      user.setAuthToken(mAccountManager.getUserData(account, USER_DATA_AUTH_TOKEN));
      user.setUsername(mAccountManager.getUserData(account, USER_DATA_USER_NAME));
      user.setUserid(Integer.parseInt(mAccountManager.getUserData(account, USER_DATA_USERID)));
      user.mEmail = mAccountManager.getUserData(account, USER_DATA_EMAIL);

      return user;
   }

   public void removeAccount(Account account) {
      mAccountManager.removeAccount(account, null, null);
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
