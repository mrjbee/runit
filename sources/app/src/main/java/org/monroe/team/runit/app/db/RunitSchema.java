package org.monroe.team.runit.app.db;

import org.monroe.team.android.box.db.Schema;

public class RunitSchema extends Schema {

    public RunitSchema() {
        super(2, "Runit.db", Application.class);
    }

    public static class Application implements Table {

        public static final String TABLE_NAME = "application";

        public static final String _ID = "id";
        public static final String _TITLE = "title";
        public static final String _PACKAGE = "package";
        public static final String _LAST_LAUNCH_DATE = "last_run";
        public static final String _LAUNCH_TIMES = "launch_times";

        @Override
        public String createScript() {
            return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    _TITLE + " STRING NOT NULL, " +
                    _PACKAGE + " STRING NOT NULL," +
                    _LAST_LAUNCH_DATE + " INTEGER, " +
                    _LAUNCH_TIMES + " INTEGER " +
                    " )";
        }

    }

}
