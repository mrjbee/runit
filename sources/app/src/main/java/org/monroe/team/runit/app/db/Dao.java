package org.monroe.team.runit.app.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.db.DAOSupport;

import java.util.Date;
import java.util.List;

public class Dao  extends DAOSupport{

    public Dao(SQLiteDatabase db) {
        super(db);
    }



    public List<Result> getMostUsedApplications() {
        Cursor cursor = db.query(RunitSchema.Application.TABLE_NAME,
                allApplicationFields(),
                null,
                null,
                null,
                null,
                RunitSchema.Application._LAUNCH_TIMES + " DESC",
                "20");

        return bakeMany(cursor,new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return extractAppResult(arg);
            }
        });
    }

    public List<Result> getLastLaunchedApplications() {
        Cursor cursor = db.query(RunitSchema.Application.TABLE_NAME,
                allApplicationFields(),
                null,
                null,
                null,
                null,
                RunitSchema.Application._LAST_LAUNCH_DATE + " DESC",
                "20");

        return bakeMany(cursor,new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return extractAppResult(arg);
            }
        });
    }

    public Result getAppById(int appDbId) {
        final Cursor cursor = db.query(RunitSchema.Application.TABLE_NAME,
                allApplicationFields(),
                RunitSchema.Application._ID +" == ?",
                strs(appDbId),
                null,
                null,
                null);


        return bake(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return extractAppResult(cursor);
            }
        });
    }

    private String[] allApplicationFields() {
        return strs(RunitSchema.Application._ID,
                RunitSchema.Application._TITLE,
                RunitSchema.Application._PACKAGE,
                RunitSchema.Application._LAST_LAUNCH_DATE,
                RunitSchema.Application._LAUNCH_TIMES);
    }

    private Result extractAppResult(Cursor cursor) {
        return new Result()
                .with(cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getLong(4));
    }

    public Result getAppByName(String packageName, String title) {
        return getAppById(generateAppId(packageName, title));
    }

    private int generateAppId(String packageName, String title) {
        return (packageName+"."+title).hashCode();
    }

    public boolean updateApplicationLaunchStatistic(
            String packageName, String title,
            Date lastLaunchDate, long launchTimes, boolean insertIfNotExist) {
        if (insertIfNotExist && getAppByName(packageName,title) == null){
            insertApplication(packageName,title, launchTimes);
        }

        ContentValues appContentValue = new ContentValues(5);
        appContentValue.put(RunitSchema.Application._ID, generateAppId(packageName, title));
        appContentValue.put(RunitSchema.Application._PACKAGE, packageName);
        appContentValue.put(RunitSchema.Application._TITLE, title);
        appContentValue.put(RunitSchema.Application._LAST_LAUNCH_DATE, lastLaunchDate.getTime());
        appContentValue.put(RunitSchema.Application._LAUNCH_TIMES, launchTimes);

        return  1 == db.update(
                RunitSchema.Application.TABLE_NAME,
                appContentValue,
                RunitSchema.Application._ID +" == ?",
                strs(generateAppId(packageName,title))
        );
    }

    public boolean insertApplication(String packageName, String title, long launchTimes) {

        ContentValues appContentValue = new ContentValues(5);
        appContentValue.put(RunitSchema.Application._ID, generateAppId(packageName, title));
        appContentValue.put(RunitSchema.Application._PACKAGE, packageName);
        appContentValue.put(RunitSchema.Application._TITLE, title);
        appContentValue.put(RunitSchema.Application._LAUNCH_TIMES, launchTimes);

        return -1 != db.insert(RunitSchema.Application.TABLE_NAME,
                null,
                appContentValue);
    }

}
