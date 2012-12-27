import android.content.Context;
import android.util.AttributeSet;

import com.mobeta.android.dslv.DragSortListView;


public class CustomDragList extends DragSortListView{

   public CustomDragList(Context context, AttributeSet attrs) {
      super(context, attrs);

   }
   
   @Override
   public void requestLayout() {
      // if (!super.mBlockLayoutRequests) {
      //     super.requestLayout();
      // }
   }

}
