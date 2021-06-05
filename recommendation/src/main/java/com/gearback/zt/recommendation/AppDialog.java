package com.gearback.zt.recommendation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.gearback.methods.Methods;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppDialog extends DialogFragment {
    Methods methods = new Methods();
    TextView appName, appDescription, appCategory;
    ImageView appIcon;
    CardView appBtn;
    TextView acceptBtn, rejectBtn;

    Recommend.SimilarApp app = null;
    AppDataBaseHelper appDataBaseHelper = null;
    List<Recommend.AppCategory> appCategories = new ArrayList<>();

    public static AppDialog newInstance(String token) {
        AppDialog f = new AppDialog();
        Bundle args = new Bundle();
        args.putString("token", token);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_dialog, container, false);
        setCancelable(false);
        appName = view.findViewById(R.id.appName);
        appDescription = view.findViewById(R.id.appDescription);
        appCategory = view.findViewById(R.id.appCategory);
        appIcon = view.findViewById(R.id.appIcon);
        appBtn = view.findViewById(R.id.appBtn);
        acceptBtn = view.findViewById(R.id.setDialogBtn);
        rejectBtn = view.findViewById(R.id.closeDialogBtn);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.fade_animation;

        if (getArguments().getString("token").equals("")) {
            appBtn.setVisibility(View.GONE);
            appDescription.setVisibility(View.GONE);
            acceptBtn.setText(getString(R.string.rec_continue_dialog));
            rejectBtn.setVisibility(View.GONE);
        }
        else {
            appBtn.setVisibility(View.VISIBLE);
            appDescription.setVisibility(View.VISIBLE);
            acceptBtn.setText(getString(R.string.rec_download));
            rejectBtn.setVisibility(View.VISIBLE);

            try {
                if (appDataBaseHelper == null) {
                    appDataBaseHelper = new AppDataBaseHelper(getActivity());
                }
                appDataBaseHelper.opendatabase();
                app = appDataBaseHelper.getApp(getArguments().getString("token"));
                appName.setText(app.getName());
                appDescription.setText(app.getDescription());
                Picasso.with(getActivity()).load(app.getIcon().replace(".png", "promo.png")).into(appIcon);
                for (int j = 0; j < appCategories.size(); j++) {
                    if (appCategories.get(j).getId() == app.getCategory()) {
                        appCategory.setText(appCategories.get(j).getName());
                        break;
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        appBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(app.getUrl()));
                try {
                    startActivity(intent);
                }
                catch (ActivityNotFoundException e) {
                    Log.d("Error", e.toString());
                }
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!getArguments().getString("token").equals("")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(app.getUrl()));
                    try {
                        startActivity(intent);
                    }
                    catch (ActivityNotFoundException e) {
                        Log.d("Error", e.toString());
                    }
                }
                dismiss();
            }
        });
        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }
    
    public void SetData(List<Recommend.AppCategory> appCategories) {
        this.appCategories = appCategories;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window win = getDialog().getWindow();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = (int) (displaymetrics.widthPixels * 0.99);
        if (methods.isTablet(getActivity())) {
            width = (int) (displaymetrics.widthPixels * 0.5);
        }
        win.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        win.setGravity(Gravity.CENTER);
    }
}
