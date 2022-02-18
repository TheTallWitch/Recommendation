package com.gearback.zt.recommendation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends Fragment {

    RecyclerView itemList;
    TextView noApps, appCategory;
    TextView backBtn;
    AppAdapter adapter;
    List<Recommend.SimilarApp> apps = new ArrayList<>();
    Recommend.BackListener backListener = null;
    AppDataBaseHelper appDataBaseHelper = null;

    //name, category, negate, alpha

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_list_fragment, container, false);
        itemList = view.findViewById(R.id.itemList);
        noApps = view.findViewById(R.id.noApps);
        backBtn = view.findViewById(R.id.backBtn);
        appCategory = view.findViewById(R.id.appCategory);
        noApps.setTypeface(noApps.getTypeface(), Typeface.ITALIC);
        appCategory.setText(getArguments().getString("name"));

        if (getArguments().getBoolean("negate", false)) {
            backBtn.setTextColor(Color.BLACK);
            backBtn.setAlpha(getArguments().getFloat("alpha"));
        }

        backBtn.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp() );

        UpdateList();

        return view;
    }

    public void UpdateList() {
        try {
            if (appDataBaseHelper == null) {
                appDataBaseHelper = new AppDataBaseHelper(getActivity());
            }
            appDataBaseHelper.opendatabase();
            apps = appDataBaseHelper.getApps(getArguments().getInt("id"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (apps.size() > 0) {
            noApps.setVisibility(View.GONE);
            itemList.setVisibility(View.VISIBLE);
            LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
            itemList.setLayoutManager(mLinearLayoutManager);

            adapter = new AppAdapter(getActivity(), getArguments().getString("name"), apps, position -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(apps.get(position).getUrl()));
                try {
                    startActivity(intent);
                }
                catch (ActivityNotFoundException e) {
                    Log.d("Error", e.toString());
                }
            });
            itemList.setAdapter(adapter);
        } else {
            noApps.setVisibility(View.VISIBLE);
            itemList.setVisibility(View.GONE);
        }
    }

    public void SetBackListener(Recommend.BackListener listener) {
        backListener = listener;
    }
}
