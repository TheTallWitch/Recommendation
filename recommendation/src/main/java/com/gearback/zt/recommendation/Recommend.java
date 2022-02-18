package com.gearback.zt.recommendation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gearback.methods.HttpGetRequest;
import com.gearback.methods.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Recommend {
    
    private Methods methods = new Methods();
    private AppDataBaseHelper appDataBaseHelper = null;
    private String appToken = "";
    private boolean showDot = false;

    public void CheckDate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastDate = preferences.getString("LAST_PROMOTE_DATE", "");
        int myCategory = preferences.getInt("MY_CATEGORY", -1);
        if (lastDate.equals("")) {
            if (myCategory == -1) {
                showDot = false;
            }
            else {
                showDot = true;
            }
        }
        else {
            Date date = methods.StringDateToDate(lastDate, Methods.DATEFORMAT2);
            if (date != null) {
                int addDays = 15;
                if (myCategory != -1) {
                    try {
                        if (appDataBaseHelper == null) {
                            appDataBaseHelper = new AppDataBaseHelper(context);
                        }
                        appDataBaseHelper.opendatabase();
                        SimilarApp promoteApp = appDataBaseHelper.getPromoteApp(myCategory, appToken);
                        if (promoteApp == null) {
                            addDays = 30;
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar nowCalendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, addDays);
                if (nowCalendar.getTime().after(calendar.getTime())) {
                    showDot = true;
                }
                else {
                    showDot = false;
                }
            }
        }
    }
    public void CheckDate(Context context, CheckDateListener listener) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastDate = preferences.getString("LAST_PROMOTE_DATE", "");
        int myCategory = preferences.getInt("MY_CATEGORY", -1);
        if (lastDate.equals("")) {
            if (myCategory == -1) {
                if (listener != null) {
                    listener.onResult(false);
                }
            }
            else {
                if (listener != null) {
                    listener.onResult(true);
                }
            }
        }
        else {
            Date date = methods.StringDateToDate(lastDate, Methods.DATEFORMAT2);
            if (date != null) {
                int addDays = 15;
                if (myCategory != -1) {
                    try {
                        if (appDataBaseHelper == null) {
                            appDataBaseHelper = new AppDataBaseHelper(context);
                        }
                        appDataBaseHelper.opendatabase();
                        SimilarApp promoteApp = appDataBaseHelper.getPromoteApp(myCategory, appToken);
                        if (promoteApp == null) {
                            addDays = 30;
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar nowCalendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, addDays);
                if (nowCalendar.getTime().after(calendar.getTime())) {
                    if (listener != null) {
                        listener.onResult(true);
                    }
                }
                else {
                    if (listener != null) {
                        listener.onResult(false);
                    }
                }
            }
        }
    }
    public void FetchApps(final Context context, final FetchAppResult listener) {
        CheckDate(context);
        HttpGetRequest getRequest = new HttpGetRequest(null, new HttpGetRequest.TaskListener() {
            @Override
            public void onFinished(String result) {
                if (!result.equals("")) {
                    try {
                        JSONObject mainObject = new JSONObject(result);
                        JSONArray categories = mainObject.getJSONArray("categories");
                        JSONArray apps = mainObject.getJSONArray("apps");
                        List<Recommend.AppCategory> appCategories = new ArrayList<>();
                        int myCategory = 0;
                        for (int i = 0; i < categories.length(); i++) {
                            JSONObject item = categories.getJSONObject(i);
                            appCategories.add(new AppCategory(item.getInt("id"), item.getString("name"), new ArrayList<SimilarApp>()));
                        }
                        for (int i = 0; i < apps.length(); i++) {
                            JSONObject item = apps.getJSONObject(i);
                            if (item.getString("token").equals(appToken)) {
                                myCategory = item.getInt("category");
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("MY_CATEGORY", myCategory);
                                editor.apply();
                            }
                            else {
                                SimilarApp app = new SimilarApp(item.getString("token"), item.getString("name"), item.getString("description"), item.getInt("category"), item.getString("icon"), item.getString("url"), 0, "");

                                try {
                                    if (appDataBaseHelper == null) {
                                        appDataBaseHelper = new AppDataBaseHelper(context);
                                    }
                                    appDataBaseHelper.opendatabase();
                                    appDataBaseHelper.addApp(app);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        SortCategories(appCategories, myCategory, context, listener);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        getRequest.execute("https://www.zodtond.com/app_upload/applications/appintro/API/intro/default.aspx?os=android", "");
    }

    public void SortCategories(List<Recommend.AppCategory> appCategories, int myCategory, Context context, FetchAppResult listener) {
        AppCategory mainCat = null;
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
                appDataBaseHelper = new AppDataBaseHelper(context);
            }
            for (int i = 0; i < appCategories.size(); i++) {
                appCategories.get(i).setApps(appDataBaseHelper.getApps(appCategories.get(i).getId()));
            }

            listener.onResult(myCategory, appCategories);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void SetAppToken(String token) {
        appToken = token;
    }

    public interface FetchAppResult {
        public void onResult(int myCategory, List<Recommend.AppCategory> appCategories);
    }

    public interface CheckDateListener {
        public void onResult(boolean showDot);
    }

    public interface BackListener {
        public void onBack();
    }

    public interface ListClickListener {
        public void onClick(int id, String name);
    }

    public class SimilarApp {
        private String token;
        private String name;
        private String description;
        private int category;
        private String icon;
        private String url;
        private int promoted;
        private String date;

        public SimilarApp(String token, String name, String description, int category, String icon, String url, int promoted, String date) {
            this.token = token;
            this.name = name;
            this.description = description;
            this.category = category;
            this.icon = icon;
            this.url = url;
            this.promoted = promoted;
            this.date = date;
        }

        public String getToken() {return token;}
        public String getName() {return name;}
        public String getDescription() {return description;}
        public String getIcon() {return icon;}
        public String getUrl() {return url;}
        public int getCategory() {return category;}
        public String getDate() {return date;}
        public int getPromoted() {return promoted;}

        public void setPromoted(int promoted) {this.promoted = promoted;}
        public void setDate(String date) {this.date = date;}
    }

    public static class AppCategory {
        private int id;
        private String name;
        private List<SimilarApp> apps;

        public AppCategory(int id, String name, List<SimilarApp> apps) {
            this.id = id;
            this.name = name;
            this.apps = apps;
        }

        public int getId() {return id;}
        public String getName() {return name;}
        public List<SimilarApp> getApps() {return apps;}

        public void setApps(List<SimilarApp> apps) {this.apps = apps;}
    }

    public class AppViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView description;
        public TextView category;
        public ImageView icon;

        public AppViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.appName);
            description = itemView.findViewById(R.id.appDescription);
            category = itemView.findViewById(R.id.appCategory);
            icon = itemView.findViewById(R.id.appIcon);
        }
    }

    public class AppPreviewViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView icon;

        public AppPreviewViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.appName);
            icon = itemView.findViewById(R.id.appIcon);
        }
    }

    public class AppCategoryViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout moreBtn;
        public TextView category;
        public RecyclerView itemList;
        public TextView header;

        public AppCategoryViewHolder(View itemView) {
            super(itemView);
            moreBtn = itemView.findViewById(R.id.appMoreBtn);
            itemList = itemView.findViewById(R.id.itemList);
            category = itemView.findViewById(R.id.appCategory);
            header = itemView.findViewById(R.id.appHeaderText);
        }
    }
}
