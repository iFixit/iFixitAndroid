package com.ifixit.guidebook;

import java.util.ArrayList;
import java.util.Hashtable;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import android.util.Log;

public class SpeechCommander {
   public static interface SpeechCommand {
      public void performCommand();
   }

   private RecognitionListener mRecognitionListener = new RecognitionListener() {
      @Override
      public void onBeginningOfSpeech() {}
      @Override
      public void onBufferReceived(byte[] arg0) {}
      @Override
      public void onEndOfSpeech() {}
      @Override
      public void onEvent(int arg0, Bundle arg1) {}
      @Override
      public void onPartialResults(Bundle arg0) {}
      @Override
      public void onReadyForSpeech(Bundle arg0) {}
      @Override
      public void onRmsChanged(float arg0) {}

      @Override
      public void onError(int arg0) {
         mSpeechRecognizer.startListening(mRecognizerIntent);
      }

      @Override
      public void onResults(Bundle results) {
         ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
         int depth = Math.min(mDepth, matches.size());
         SpeechCommand command;

         Log.d("SpeechCommander", "Results: " + matches);

         for (int i = 0; i < depth; i ++) {
            if ((command = getMatch(matches.get(i))) != null) {
               Log.d("SpeechCommander", "Performing command: " + matches.get(i));
               command.performCommand();
               break;
            }
         }

         if (mListening)
            mSpeechRecognizer.startListening(mRecognizerIntent);
      }
   };

   private static final int DEFAULT_DEPTH = 5;

   protected SpeechRecognizer mSpeechRecognizer;
   protected Intent mRecognizerIntent;
   protected Hashtable<String, SpeechCommand> mCommands;
   protected Context mContext;
   protected int mDepth;
   protected boolean mListening;

   public SpeechCommander(Context context, String callingPackage) {
      this(context, callingPackage, DEFAULT_DEPTH);
   }

   public SpeechCommander(Context context, String callingPackage, int depth) {
      mContext = context;
      mCommands = new Hashtable<String, SpeechCommand>();
      mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
      mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
      mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
       RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
      mRecognizerIntent.putExtra("calling_package", callingPackage);

      mDepth = depth;
      mListening = false;
   }

   public void startListening() {
      if (!mListening) {
         mSpeechRecognizer.startListening(mRecognizerIntent);
         mListening = true;
      }
   }

   public void stopListening() {
      if (mListening) {
         mSpeechRecognizer.stopListening();
         mListening = false;
      }
   }

   public void destroy() {
      mSpeechRecognizer.destroy();
   }

   public void cancel() {
      mSpeechRecognizer.cancel();
   }

   public void addCommand(String phrase, SpeechCommand command) {
      mCommands.put(phrase, command);
   }

   private SpeechCommand getMatch(String phrase) {
      for (String command : mCommands.keySet()) {
         if (phrase.indexOf(command) != -1)
            return mCommands.get(command);
      }
      return null;
   }
}
