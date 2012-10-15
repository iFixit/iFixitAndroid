Code Style
==========

ONLY spaces, no tabs.  3 space indent.  When breaking lines, continue on next
line 1 space in.

100 column max line width.  Wrap to maximize readability (subjective, but use
your best judgement)

Bracket style example:

```
package com.dozuki.ifixit.util;

public class ImageSizes {
   private String mThumb;
   private String mMain;
   private String mFull;
   private String mGrid;

   public ImageSizes(String thumb, String main, String full, String grid) {
      mThumb = thumb;
      mMain = main;
      mFull = full;
      mGrid = grid;
   }

   public String getThumb() {
      return mThumb;
   }

   public String getMain() {
      return mMain;
   }

   public String getFull() {
      return mFull;
   }
   
   public String getGrid() {
     return mGrid;
   }
}
```

Basically, look around our code and match the style around the code you're
editing.  We're not style nazi's here, but nothing is more frustrating than
dealing with tabs/space combo's and weirdly named variables.  Be smart.

If your developing in Windows, make sure that your files are in Unix format. 
`dos2unix` it.
