package com.gearback.zt.recommendation;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class ApplicationData extends Application {
    public List<Recommend.AppCategory> appCategories = new ArrayList<>();
    public List<Recommend.SimilarApp> similarApps = new ArrayList<>();
    public int myCategory = 0;
    public AppDataBaseHelper appDataBaseHelper = null;
    public boolean showDot = false;
}
