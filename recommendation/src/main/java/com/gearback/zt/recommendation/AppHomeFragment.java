package com.gearback.zt.recommendation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gearback.methods.Methods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AppHomeFragment extends Fragment {
    TextView backBtn;
    RecyclerView itemList;
    AppCategoryAdapter adapter;

    Recommend.SimilarApp promoteApp = null;
    Methods methods = new Methods();
    AppDataBaseHelper appDataBaseHelper = null;

    Recommend.BackListener backListener = null;
    Recommend.ListClickListener clickListener = null;
    
    int myCategory = 0;
    boolean showDot = false;
    List<Recommend.AppCategory> appCategories = new ArrayList<>();

    //app token

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_home_fragment, container, false);
        backBtn = view.findViewById(R.id.backBtn);
        itemList = view.findViewById(R.id.itemList);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backListener != null) {
                    backListener.onBack();
                }
            }
        });

        return view;
    }

    public void InitializeData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        try {
            if (appDataBaseHelper == null) {
                appDataBaseHelper = new AppDataBaseHelper(getActivity());
            }
            promoteApp = appDataBaseHelper.getPromoteApp(myCategory, getArguments().getString("token"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("LAST_PROMOTE_DATE", methods.calendarToString(Calendar.getInstance(), Methods.DATEFORMAT2));
        editor.apply();

        if (showDot) {
            if (promoteApp != null) {
                promoteApp.setPromoted(1);
                promoteApp.setDate(methods.calendarToString(Calendar.getInstance(), Methods.DATEFORMAT2));
                appDataBaseHelper.updateApp(promoteApp);

                AppDialog appDialog = AppDialog.newInstance(promoteApp.getToken());
                appDialog.SetData(appCategories);
                appDialog.show(getActivity().getSupportFragmentManager(), "appDialog");
            }
            else {
                AppDialog appDialog = AppDialog.newInstance("");
                appDialog.SetData(appCategories);
                appDialog.show(getActivity().getSupportFragmentManager(), "appDialog");
            }


        }

        SortCategories();
    }

    public void SortCategories() {
        Recommend.AppCategory mainCat = null;
        for (int i = 0; i < appCategories.size(); i++) {
            if (appCategories.get(i).getId() == myCategory) {
                mainCat = appCategories.get(i);
                appCategories.remove(i);
                break;
            }
        }
        appCategories.add(0, mainCat);

        try {
            if (appDataBaseHelper == null) {
                appDataBaseHelper = new AppDataBaseHelper(getActivity());
            }
            for (int i = 0; i < appCategories.size(); i++) {
                appCategories.get(i).setApps(appDataBaseHelper.getApps(appCategories.get(i).getId()));
            }

            LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
            itemList.setLayoutManager(mLinearLayoutManager);

            adapter = new AppCategoryAdapter(getActivity(), appCategories, new AppCategoryAdapter.OnItemClickListener() {
                @Override
                public void onCategoryClick(int position) {
                    if (clickListener != null) {
                        clickListener.onClick(appCategories.get(position).getId(), appCategories.get(position).getName());
                    }
                }

                @Override
                public void onItemClick(String url) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    try {
                        startActivity(intent);
                    }
                    catch (ActivityNotFoundException e) {
                        Log.d("Error", e.toString());
                    }
                }
            });
            itemList.setAdapter(adapter);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SetBackListener(Recommend.BackListener listener) {
        backListener = listener;
    }

    public void SetClickListener(Recommend.ListClickListener listener) {
        clickListener = listener;
    }
    
    public void SetData(int cat, List<Recommend.AppCategory> categories, boolean show) {
        myCategory = cat;
        appCategories = categories;
        showDot = show;

        InitializeData();
    }

}
