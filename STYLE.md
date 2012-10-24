Code Style
==========

* Three space indentation.
* 100 column max line width.
  * When breaking lines, continue on next line one space in.
  * There are exceptions to this if it makes code line up better.
* [UNIX file format](http://en.wikipedia.org/wiki/Newline#Representations)
* [K&R style braces](http://en.wikipedia.org/wiki/Indent_style#K.26R_style)
  * Even though it is unnecessary, prefer including braces around a single line of code. It simply makes it easier to add more code such as logs and is less error prone since all of the braces line up without any weird gaps.


Example:
```
...

public class TopicListAdapter extends Section {
   private Context mContext;
   private ArrayList<TopicNode> mTopicList;
   private String mHeader;
   private TopicSelectedListener mTopicListener;
   private TopicListRowView prevSelected = null;

   public TopicListAdapter(Context context, String header,
    ArrayList<TopicNode> topicList) {
      mContext = context;
      mHeader = header;
      mTopicList = topicList;
   }

   public void setTopicList(ArrayList<TopicNode> topicList) {
      mTopicList = topicList;
   }

   public void setTopicSelectedListener(TopicSelectedListener topicListener) {
      mTopicListener = topicListener;
   }

   public int getCount() {
      return mTopicList.size();
   }

   public Object getItem(int position) {
      return mTopicList.get(position);
   }

   public long getItemId(int position) {
      return position;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      TopicListRowView topicRow;

      if (convertView == null) {
         topicRow = new TopicListRowView(mContext);
      } else {
         topicRow = (TopicListRowView)convertView;
      }

      topicRow.setTopic(mTopicList.get(position));

      return topicRow;
   }

...
```

Basically, look around our code and match the style around the code you're
editing.  We're not style nazi's here, but we like to keep things nice and
maintainable.  Be smart.
