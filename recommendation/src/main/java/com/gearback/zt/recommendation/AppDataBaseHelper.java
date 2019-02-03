package com.gearback.zt.recommendation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AppDataBaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH;
    public static final String DB_NAME = "app_db.db3";
    private SQLiteDatabase myDataBase;
    private final Context context;
    private Recommend classes = new Recommend();

    public AppDataBaseHelper(Context context) throws IOException {
        super(context, DB_NAME, null, 1);
        this.context = context;
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        boolean dbexist = checkdatabase();
        if (dbexist) {
            opendatabase();
        } else {
            createdatabase();
        }
    }

    public void createdatabase() throws IOException {
        boolean dbexist = checkdatabase();
        SQLiteDatabase db_Read = null;
        if(!dbexist) {
            db_Read = this.getReadableDatabase();
            db_Read.close();
            //this.getReadableDatabase();
            try {
                copydatabase();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkdatabase() {
        boolean checkdb = false;
        try {
            String myPath = DB_PATH + DB_NAME;
            File dbfile = new File(myPath);
            checkdb = dbfile.exists();
        } catch(SQLiteException e) {
            e.printStackTrace();
        }
        return checkdb;
    }

    private void copydatabase() throws IOException {
        InputStream myinput = context.getAssets().open(DB_NAME);

        String outfilename = DB_PATH + DB_NAME;

        OutputStream myoutput = new FileOutputStream(outfilename);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer))>0) {
            myoutput.write(buffer,0,length);
        }

        myoutput.flush();
        myoutput.close();
        myinput.close();

        opendatabase();
    }

    public void opendatabase() throws SQLException {
        String mypath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
    }
    public void closeDatabase() {
        if (myDataBase != null && myDataBase.isOpen()) {
            myDataBase.close();
        }
    }

    public void addApp(Recommend.SimilarApp app) {
        if (!myDataBase.isOpen()) {
            opendatabase();
        }

        boolean found = false;
        Cursor cursor = myDataBase.rawQuery("SELECT * FROM apps WHERE category=" + app.getCategory(), null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(1).equals(app.getToken())) {
                    found = true;
                    break;
                }
            } while (cursor.moveToNext());
        }

        if (!found) {
            ContentValues cv = new ContentValues();
            cv.put("token", app.getToken());
            cv.put("name", app.getName());
            cv.put("description", app.getDescription());
            cv.put("icon", app.getIcon());
            cv.put("url", app.getUrl());
            cv.put("category", app.getCategory());
            cv.put("date", app.getDate());
            cv.put("promoted", app.getPromoted());
            myDataBase.insert("apps", null, cv);
        }
    }
    public void updateApp(Recommend.SimilarApp app)
    {
        if (!myDataBase.isOpen()) {
            opendatabase();
        }
        ContentValues cv = new ContentValues();
        cv.put("date", app.getDate());
        cv.put("promoted", app.getPromoted());
        myDataBase.update("apps", cv, "token='" + app.getToken() + "'", null);
    }
    public List<Recommend.SimilarApp> getApps() {
        if (!myDataBase.isOpen()) {
            opendatabase();
        }
        List<Recommend.SimilarApp> items = new ArrayList<>();
        Cursor cursor = myDataBase.rawQuery("SELECT * FROM apps", null);
        if (cursor.moveToFirst()) {
            do {
                items.add(classes.new SimilarApp(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getString(8)));
            } while (cursor.moveToNext());
        }

        return items;
    }
    public List<Recommend.SimilarApp> getApps(int category) {
        if (!myDataBase.isOpen()) {
            opendatabase();
        }
        List<Recommend.SimilarApp> items = new ArrayList<>();
        Cursor cursor = myDataBase.rawQuery("SELECT * FROM apps WHERE category=" + category, null);
        if (cursor.moveToFirst()) {
            do {
                items.add(classes.new SimilarApp(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getString(8)));
            } while (cursor.moveToNext());
        }

        return items;
    }
    public List<Recommend.SimilarApp> getOtherApps(int category) {
        if (!myDataBase.isOpen()) {
            opendatabase();
        }
        List<Recommend.SimilarApp> items = new ArrayList<>();
        Cursor cursor = myDataBase.rawQuery("SELECT * FROM apps WHERE category!=" + category, null);
        if (cursor.moveToFirst()) {
            do {
                items.add(classes.new SimilarApp(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getString(8)));
            } while (cursor.moveToNext());
        }

        return items;
    }
    public Recommend.SimilarApp getPromoteApp(int category, String myToken) {
        if (!myDataBase.isOpen()) {
            opendatabase();
        }
        Recommend.SimilarApp item = null;
        Cursor cursor = myDataBase.rawQuery("SELECT * FROM apps WHERE category=" + category + " AND promoted=0 AND token!='" + myToken + "'", null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            item = classes.new SimilarApp(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getString(8));
            cursor.close();
        }

        return item;
    }
    public Recommend.SimilarApp getApp(String token) {
        if (!myDataBase.isOpen()) {
            opendatabase();
        }
        Recommend.SimilarApp item = null;
        Cursor cursor = myDataBase.rawQuery("SELECT * FROM apps WHERE token='" + token + "'", null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            item = classes.new SimilarApp(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getString(8));
            cursor.close();
        }

        return item;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }
}
