package com.gearback.modules.recommendation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gearback.methods.Methods;

import java.io.IOException;
import java.util.Calendar;

public class AppHomeFragment extends Fragment {
    TextView backBtn;
    RecyclerView itemList;
    AppCategoryAdapter adapter;
    ImageView appLogo;
    ProgressBar progressBar;

    Recommend.SimilarApp promoteApp = null;
    Methods methods = new Methods();
    AppDataBaseHelper appDataBaseHelper = null;
    RecommendSingleton singleton = null;

    Recommend.BackListener backListener = null;
    Recommend.ListClickListener clickListener = null;
    
    int myCategory = 0;
    boolean showDot = false;
    private static final float[] NEGATIVE = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };

    //app token, negate, alpha

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_home_fragment, container, false);
        backBtn = view.findViewById(R.id.backBtn);
        itemList = view.findViewById(R.id.itemList);
        appLogo = view.findViewById(R.id.appLogo);
        progressBar = view.findViewById(R.id.progressBar);
        
        singleton = RecommendSingleton.getInstance();

        if (getArguments() != null && getArguments().getBoolean("negate", false)) {
            NEGATIVE[18] = getArguments().getFloat("alpha");
            appLogo.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
            backBtn.setTextColor(Color.BLACK);
            backBtn.setAlpha(getArguments().getFloat("alpha"));
        }

        myCategory = getArguments().getInt("cat");
        showDot = getArguments().getBoolean("showDot");

        backBtn.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp() );

        appLogo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.rec_ad_domain)));
            try {
                startActivity(intent);
            }
            catch (ActivityNotFoundException e) {
                Log.d("Error", e.toString());
            }
        });

        Recommend recommend = new Recommend();

        if (singleton.appCategories.size() == 0) {
            progressBar.setVisibility(View.VISIBLE);
            recommend.FetchApps(getContext(), (cat, list) -> {
                progressBar.setVisibility(View.GONE);
                singleton.appCategories = list;
                InitializeData();
            });
        }
        else {
            InitializeData();
        }

        return view;
    }

    public void InitializeData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        try {
            if (appDataBaseHelper == null) {
                appDataBaseHelper = new AppDataBaseHelper(getActivity());
            }
            if (getArguments() != null) {
                promoteApp = appDataBaseHelper.getPromoteApp(myCategory, getArguments().getString("token"));
            }
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
                appDialog.SetData(singleton.appCategories);
                appDialog.show(requireActivity().getSupportFragmentManager(), "appDialog");
            }
            else {
                AppDialog appDialog = AppDialog.newInstance("");
                appDialog.SetData(singleton.appCategories);
                appDialog.show(requireActivity().getSupportFragmentManager(), "appDialog");
            }


        }

        SortCategories();
    }

    public void SortCategories() {
        Recommend.AppCategory mainCat = null;
        for (int i = 0; i < singleton.appCategories.size(); i++) {
            if (singleton.appCategories.get(i).getId() == myCategory) {
                mainCat = singleton.appCategories.get(i);
                singleton.appCategories.remove(i);
                break;
            }
        }
        if (mainCat != null) {
            singleton.appCategories.add(0, mainCat);
        }

        try {
            if (appDataBaseHelper == null) {
                appDataBaseHelper = new AppDataBaseHelper(requireActivity());
            }
            for (int i = 0; i < singleton.appCategories.size(); i++) {
                singleton.appCategories.get(i).setApps(appDataBaseHelper.getApps(singleton.appCategories.get(i).getId()));
            }

            LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(requireActivity());
            itemList.setLayoutManager(mLinearLayoutManager);

            adapter = new AppCategoryAdapter(requireActivity(), singleton.appCategories, new AppCategoryAdapter.OnItemClickListener() {
                @Override
                public void onCategoryClick(int position) {
                    NavDirections action = AppHomeFragmentDirections.actionAppHomeFragmentToAppListFragment(singleton.appCategories.get(position).getId(), singleton.appCategories.get(position).getName(), getArguments().getBoolean("negate", false), getArguments().getFloat("alpha"));
                    Navigation.findNavController(requireView()).navigate(action);
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
}
