package com.dozuki.ifixit.model.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.dozuki.ifixit.BuildConfig;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.api.ApiContentProvider;

/**
 * This is the authenticator for accounts associated with this particular app.
 * Accounts are not shared between white-labelled apps e.g. iFixit vs. Dozuki.
 * Many of the "important" methods aren't implemented because they are only used
 * when sharing accounts between apps and retrieving auth tokens in a consistent
 * manner. Since we already had all of this functionality, the main benefit of
 * using the AccountManager system is for background syncing. The SyncService
 * API requires an Account so it makes sense to store all of the relevant user
 * data (email, auth token, userid, etc.) in this system rather than in
 * SharedPreferences.
 *
 * This class also has lots of helper methods for interacting with
 * AccountManager so our application doesn't need to use it directly.
 */
public class Authenticator extends AbstractAccountAuthenticator {
   public static final String AUTH_TOKEN_TYPE_FULL_ACCESS = "Full access";
   private static final String USER_DATA_SITE_NAME = "USER_DATA_SITE_NAME";
   private static final String USER_DATA_USER_NAME = "USER_DATA_USER_NAME";
   private static final String USER_DATA_USERID = "USER_DATA_USERID";
   private static final String USER_DATA_EMAIL = "USER_DATA_EMAIL";

   private final Context mContext;
   private final AccountManager mAccountManager;

   public Authenticator(Context context) {
      super(context);

      mContext = context;
      mAccountManager = AccountManager.get(mContext);
   }

   public static String getAccountType() {
      return "com.dozuki." + BuildConfig.SITE_NAME;
   }

   /**
    * Call whenever an account has been authenticated so it can be added to the
    * AccountManager with all of the expected fields. Removes any accounts that
    * are associated with the same site and updates the account if we suspect
    * it's the same user.
    */
   public Account onAccountAuthenticated(Site site, String email, String userName,
    int userid, String password, String authToken) {
      Bundle userData = getUserDataBundle(site, email, userName, userid);

      Account existingAccount = getAccountForSite(site);
      if (existingAccount != null) {
         if (email.equals(mAccountManager.getUserData(existingAccount, USER_DATA_EMAIL))) {
            return updateAccount(existingAccount, password, authToken, userData);
         } else {
            // Remove the existing account because we will make a new one below. We only
            // allow at most 1 account per site.
            removeAccount(existingAccount);
         }
      }

      // Accounts cannot share the same name so we must prefix the username with the site
      // name if this is the dozuki app.
      String accountName = userName;
      if (BuildConfig.SITE_NAME.equals("dozuki")) {
         accountName = site.mTitle + ": " + userName;
      }

      Account newAccount = new Account(accountName, getAccountType());

      mAccountManager.addAccountExplicitly(newAccount, password, userData);
      mAccountManager.setAuthToken(newAccount, AUTH_TOKEN_TYPE_FULL_ACCESS, authToken);

      // By default, automatically sync user's data.
      mContext.getContentResolver().setSyncAutomatically(newAccount,
       ApiContentProvider.getAuthority(), true);

      return newAccount;
   }

   private Account updateAccount(Account account, String password, String authToken,
    Bundle userData) {
      // Unfortunately you can't set a bundle on an existing account so we
      // must iterate over the keys and set the data one by one.
      for (String key : userData.keySet()) {
         mAccountManager.setUserData(account, key, userData.getString(key));
      }

      mAccountManager.setPassword(account, password);
      mAccountManager.setAuthToken(account, AUTH_TOKEN_TYPE_FULL_ACCESS, authToken);

      return account;
   }

   private Bundle getUserDataBundle(Site site, String email, String userName, int userid) {
      Bundle userData = new Bundle();
      userData.putString(USER_DATA_SITE_NAME, site.mName);
      userData.putString(USER_DATA_EMAIL, email);
      userData.putString(USER_DATA_USER_NAME, userName);
      userData.putString(USER_DATA_USERID, "" + userid);

      return userData;
   }

   public void invalidateAuthToken(String authToken) {
      mAccountManager.invalidateAuthToken(AUTH_TOKEN_TYPE_FULL_ACCESS, authToken);
   }

   public Account getAccountForSite(Site site) {
      String siteName = site.mName;

      for (Account account : mAccountManager.getAccountsByType(getAccountType())) {
         if (mAccountManager.getUserData(account, USER_DATA_SITE_NAME).equals(siteName)) {
            return account;
         }
      }

      return null;
   }

   public String getPassword(Account account) {
      return mAccountManager.getPassword(account);
   }

   /**
    * Factory method for creating a User from an Account.
    */
   public User createUser(Account account) {
      User user = new User();

      /**
       * The auth token will be invalidated if the user's auth token expired and
       * the stored credentials could not successfully reauthenticate. In this case
       * we pretend that the user is still signed in with a valid account. The
       * next request that requires authentication will trigger the login dialog.
       */
      String authToken = mAccountManager.peekAuthToken(account, AUTH_TOKEN_TYPE_FULL_ACCESS);
      user.setAuthToken(authToken == null ? "invalid" : authToken);

      user.setUsername(mAccountManager.getUserData(account, USER_DATA_USER_NAME));
      user.setUserid(Integer.parseInt(mAccountManager.getUserData(account, USER_DATA_USERID)));
      user.mEmail = mAccountManager.getUserData(account, USER_DATA_EMAIL);
      user.mSiteName = mAccountManager.getUserData(account, USER_DATA_SITE_NAME);

      return user;
   }

   public void removeAccount(Account account) {
      mAccountManager.removeAccount(account, null, null);
   }

   /**
    * Unimplemented methods. Turns out we don't really need to implement any of
    * these methods unless we plan on sharing accounts outside of our app.
    */

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
      Log.w("Authenticator", "getAuthToken not implemented");
      return null;
   }

   @Override
   public String getAuthTokenLabel(String authTokenType) {
      return authTokenType;
   }

   @Override
   public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
    String[] features) throws NetworkErrorException {
      Log.w("Authenticator", "hasFeatures not implemented");
      return null;
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
