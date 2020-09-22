package HelperClasses.HomeAdapter;

import android.graphics.drawable.Drawable;

public class MostViewedHelperClass {

    int imageView;
    String textView, textView1, textView2;


    public MostViewedHelperClass(int imageView, String textView, String textView1, String textView2) {
        this.imageView = imageView;
        this.textView = textView;
        this.textView1 = textView1;
        this.textView2 = textView2;
    }

    public int getImageView() {
        return imageView;
    }

    public String getTextView1() {
        return textView1;
    }

    public String getTextView2() {
        return textView2;
    }

    public String getTextView() {
        return textView;
    }
}








