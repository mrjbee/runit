package org.monroe.team.runit.app.db;

import org.monroe.team.android.box.db.Schema;

public class RunitSchema extends Schema {

    public RunitSchema() {
        super(3, "Runit.db", Application.class);
    }

    public static class Application_v2 extends VersionTable {

        public final String TABLE_NAME = "application";

        public final String _ID = "id";
        public final String _TITLE = "title";
        public final String _PACKAGE = "package";
        public final String _LAST_LAUNCH_DATE = "last_run";
        public final String _LAUNCH_TIMES = "launch_times";

        public Application_v2() {
            define(2,TABLE_NAME)
                .column(_ID, "INTEGER PRIMARY KEY")
                .column(_TITLE, "STRING NOT NULL")
                .column(_PACKAGE, "STRING NOT NULL")
                .column(_LAST_LAUNCH_DATE, "INTEGER")
                .column(_LAUNCH_TIMES,"INTEGER");
        }
    }

    public static class Application extends Application_v2{
        public final String _CATEGORY = "category";
        public Application() {
            define(3)
                .column(_CATEGORY, "INTEGER");
        }
    }

}
