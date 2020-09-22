package HelperClasses.HomeAdapter;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class FeaturedHelperClass {

    int image;
    String title, description;
    GradientDrawable gradient;

    public FeaturedHelperClass(int image, String title, String description, GradientDrawable gradient) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.gradient = gradient;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getDescription() {
        return Drawable.createFromPath(description);
    }

    public GradientDrawable getGradient() {
        return gradient;
    }
}
