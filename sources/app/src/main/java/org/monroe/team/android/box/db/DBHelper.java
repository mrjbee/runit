package org.monroe.team.android.box.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.monroe.team.android.box.Closure;

public class DBHelper extends SQLiteOpenHelper {

    private final Schema schema;

    public DBHelper(Context context, Schema schema) {
        super(context, schema.name, null, schema.version);
        this.schema = schema;
    }

    public void onCreate(final SQLiteDatabase db) {
        schema.withEachTable(new Closure<Schema.Table, Void>() {
            @Override
            public Void execute(Schema.Table table) {
                db.execSQL(table.createScript());
                return null;
            }
        });
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
