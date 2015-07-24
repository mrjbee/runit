package org.monroe.team.runit.app.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.Schema;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.Date;
import java.util.List;

public class Dao  extends DAOSupport{

    public Dao(SQLiteDatabase db, Schema schema) {
        super(db, schema);
    }



    public List<Result> getMostUsedApplications() {
        Cursor cursor = db.query(table(RunitSchema.Application.class).TABLE_NAME,
                allApplicationFields(),
                table(RunitSchema.Application.class)._LAUNCH_TIMES.name() +" not null",
                null,
                null,
                null,
                table(RunitSchema.Application.class)._LAUNCH_TIMES.name() + " DESC",
                "20");

        return bakeMany(cursor,new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return extractAppResult(arg);
            }
        });
    }

    public List<Result> getLastLaunchedApplications() {
        Cursor cursor = db.query(table(RunitSchema.Application.class).TABLE_NAME,
                allApplicationFields(),
                table(RunitSchema.Application.class)._LAST_LAUNCH_DATE.name() +" not null",
                null,
                null,
                null,
                table(RunitSchema.Application.class)._LAST_LAUNCH_DATE.name() + " DESC",
                "20");

        return bakeMany(cursor,new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return extractAppResult(arg);
            }
        });
    }

    public Result getAppById(int appDbId) {
        final Cursor cursor = db.query(table(RunitSchema.Application.class).TABLE_NAME,
                allApplicationFields(),
                table(RunitSchema.Application.class)._ID.name() +" == ?",
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

    public List<Result> getAppsByCategory(Long category) {
        Cursor cursor;
        if (category != null) {
            cursor = db.query(table(RunitSchema.Application.class).TABLE_NAME,
                    allApplicationFields(),
                    table(RunitSchema.Application.class)._CATEGORY.name() + " == ?",
                    strs(category),
                    null,
                    null,
                    null);
        } else {
            cursor = db.query(table(RunitSchema.Application.class).TABLE_NAME,
                    allApplicationFields(),
                    table(RunitSchema.Application.class)._CATEGORY.name() + " is NULL",
                    null,
                    null,
                    null,
                    null);
        }
        return bakeMany(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return extractAppResult(arg);
            }
        });
    }

    private String[] allApplicationFields() {
        return strs(table(RunitSchema.Application.class)._ID.name(),
                table(RunitSchema.Application.class)._TITLE.name(),
                table(RunitSchema.Application.class)._PACKAGE.name(),
                table(RunitSchema.Application.class)._LAST_LAUNCH_DATE.name(),
                table(RunitSchema.Application.class)._LAUNCH_TIMES.name(),
                table(RunitSchema.Application.class)._CATEGORY.name());
    }

    private Result extractAppResult(Cursor cursor) {
        return new Result()
                .with(cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getLong(4),
                        (cursor.isNull(5)?null:cursor.getLong(5)));
    }

    public Result getAppByName(String packageName, String title) {
        return getAppById(generateAppId(packageName, title));
    }

    public int generateAppId(String packageName, String title) {
        return (packageName+"."+title).hashCode();
    }

    public boolean updateApplicationLaunchStatistic(
            String packageName, String title,
            Date lastLaunchDate, long launchTimes, boolean insertIfNotExist) {
        if (insertIfNotExist && getAppByName(packageName,title) == null){
            insertApplication(packageName,title);
        }

        ContentValues appContentValue = new ContentValues(5);
        appContentValue.put(table(RunitSchema.Application.class)._ID.name(), generateAppId(packageName, title));
        appContentValue.put(table(RunitSchema.Application.class)._PACKAGE.name(), packageName);
        appContentValue.put(table(RunitSchema.Application.class)._TITLE.name(), title);
        appContentValue.put(table(RunitSchema.Application.class)._LAST_LAUNCH_DATE.name(), lastLaunchDate.getTime());
        appContentValue.put(table(RunitSchema.Application.class)._LAUNCH_TIMES.name(), launchTimes);

        return  1 == db.update(
                table(RunitSchema.Application.class).TABLE_NAME,
                appContentValue,
                table(RunitSchema.Application.class)._ID.name() +" == ?",
                strs(generateAppId(packageName,title))
        );
    }

    public boolean insertApplication(String packageName, String title) {

        ContentValues appContentValue = new ContentValues(5);
        appContentValue.put(table(RunitSchema.Application.class)._ID.name(), generateAppId(packageName, title));
        appContentValue.put(table(RunitSchema.Application.class)._PACKAGE.name(), packageName);
        appContentValue.put(table(RunitSchema.Application.class)._TITLE.name(), title);

        return -1 != db.insert(table(RunitSchema.Application.class).TABLE_NAME,
                null,
                appContentValue);
    }

    public boolean insertApplicationWithCategory(String packageName, String title, int category) {

        ContentValues appContentValue = new ContentValues(5);
        appContentValue.put(table(RunitSchema.Application.class)._ID.name(), generateAppId(packageName, title));
        appContentValue.put(table(RunitSchema.Application.class)._PACKAGE.name(), packageName);
        appContentValue.put(table(RunitSchema.Application.class)._TITLE.name(), title);
        appContentValue.put(table(RunitSchema.Application.class)._CATEGORY.name(), category);

        return -1 != db.insert(table(RunitSchema.Application.class).TABLE_NAME,
                null,
                appContentValue);
    }

    public boolean updateApplicationCategory(
            String packageName, String title, Long category, boolean insertIfNotExist) {
        if (insertIfNotExist && getAppByName(packageName,title) == null){
            insertApplication(packageName,title);
        }

        ContentValues appContentValue = new ContentValues(5);
        appContentValue.put(table(RunitSchema.Application.class)._ID.name(), generateAppId(packageName, title));
        appContentValue.put(table(RunitSchema.Application.class)._CATEGORY.name(), category);

        return  1 == db.update(
                table(RunitSchema.Application.class).TABLE_NAME,
                appContentValue,
                table(RunitSchema.Application.class)._ID.name() +" == ?",
                strs(generateAppId(packageName,title))
        );
    }


    public List<Result> appsCountPerCategory() {
        //SELECT strftime('%Y-%m-%d', date / 1000, 'unixepoch'), count(*) FROM smoke GROUP BY strftime('%Y-%m-%d', date / 1000, 'unixepoch');
        Cursor cursor = db.query(table(RunitSchema.Application.class).TABLE_NAME,
                strs(table(RunitSchema.Application.class)._CATEGORY.name(), "count(*)"),
                null,
                null,
                table(RunitSchema.Application.class)._CATEGORY.name(),
                null,
                null);

        return bakeMany(cursor, new Closure<Cursor, Result>() {
            @Override
            public Result execute(Cursor arg) {
                return new Result().with(arg.isNull(0)? null:arg.getLong(0),
                        arg.getLong(1));
            }
        });
    }

    public int removeAppsNotInList(List<ApplicationData> appsData) {
        StringBuilder builder = new StringBuilder();
        for (ApplicationData applicationData : appsData) {
            builder
                .append(generateAppId(applicationData.packageName, applicationData.name))
                .append(",");
        }
        builder.deleteCharAt(builder.length()-1);
        return db.delete(
                table(RunitSchema.Application.class).TABLE_NAME,
                table(RunitSchema.Application.class)._ID.name() +" NOT IN ("+builder.toString()+")",
                null);
    }


}
