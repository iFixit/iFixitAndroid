package com.dozuki.ifixit.login.model;

public abstract class LoginEvent {
   public static class Logout extends LoginEvent {}
   public static class Cancel extends LoginEvent {}

   public static class Login extends LoginEvent {
      private User mUser;

      public Login(User user) {
         mUser = user;
      }

      public User getUser() {
         return mUser;
      }
   }
}
