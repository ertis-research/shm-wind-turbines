package com.ertis.windturbinesai;

import android.graphics.Bitmap;

public class Damage {
    private Bitmap image;
    private String name;

    public Damage(Bitmap img, String n) {
        image = img;
        name = n;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
