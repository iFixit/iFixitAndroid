package com.dozuki.ifixit.login.model;


public interface LoginListener {
   public void onLogin(User user);
   public void onCancel();
   public void onLogout();
}
