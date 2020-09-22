package HelperClasses.HomeAdapter;


import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class CategoriesHelperClass {

    String title;
    int image;
    GradientDrawable gradient;

    public CategoriesHelperClass(String title, int image, GradientDrawable gradient) {
        this.title = title;
        this.image = image;
        this.gradient = gradient;
    }

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }

    public GradientDrawable getGradient() {
        return gradient;
    }
}

