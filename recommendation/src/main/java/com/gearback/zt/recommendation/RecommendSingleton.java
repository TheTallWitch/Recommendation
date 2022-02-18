package com.gearback.zt.recommendation;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecommendSingleton {
    private static RecommendSingleton instance = null;
    public List<Recommend.AppCategory> appCategories = new ArrayList<>();

    protected RecommendSingleton() {

    }

    public static RecommendSingleton getInstance() {
        if (instance == null) {
            instance = new RecommendSingleton();
        }
        return instance;
    }
}
