package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.ui.BaseFragment;

import java.util.ArrayList;

public class StepLinesFragment extends BaseFragment {

   public static final String GUIDE_STEP = "GUIDE_STEP_KEY";

   private Context mContext;
   private TextView mTitle;
   private ListView mLineList;
   private StepTextArrayAdapter mTextAdapter;
   private GuideStep mStep;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mContext = getActivity();
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      View view = inflater.inflate(R.layout.guide_step_lines, container, false);

      mLineList = (ListView) view.findViewById(R.id.step_text_list);
      mTitle = (TextView) view.findViewById(R.id.step_title);

      Bundle extras = getArguments();
      if (extras != null) {
         mStep = (GuideStep)extras.getSerializable(GUIDE_STEP);
      }

      String title = mStep.getTitle().length() == 0
       ? getString(R.string.step_number, mStep.getStepNum())
       : mStep.getTitle();

      // Set the guide title text, defaults to Step #
      mTitle.setText(title);

      // Initialize the step instructions text and bullets
      mTextAdapter = new StepTextArrayAdapter(mContext, R.id.step_text_list, mStep.getLines());
      mLineList.setAdapter(mTextAdapter);

      return view;
   }

   public class StepTextArrayAdapter extends ArrayAdapter<StepLine> {
      private ArrayList<StepLine> mLines;
      private Context mContext;

      public StepTextArrayAdapter(Context context, int viewResourceId,
       ArrayList<StepLine> lines) {
         super(context, viewResourceId, lines);

         mLines = lines;
         mContext = context;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         GuideStepLineView stepLine = (GuideStepLineView) convertView;

         if (stepLine == null) {
            stepLine = new GuideStepLineView(mContext);
         }
         stepLine.setLine(mLines.get(position));
         return stepLine;
      }
   }

}
