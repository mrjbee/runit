package org.monroe.team.runit.app.db;

import org.monroe.team.android.box.db.Schema;

public class RunitSchema extends Schema {

    public RunitSchema() {
        super(3, "Runit.db", Application.class);
    }

    public static class Application_v2 extends VersionTable {

        public final String TABLE_NAME = "application";

        public final ColumnID<String> _ID = column("id", String.class);
        public final ColumnID<String> _TITLE = column("title", String.class);
        public final ColumnID<String> _PACKAGE = column("package", String.class);
        public final ColumnID<Long> _LAST_LAUNCH_DATE = column("last_run", Long.class);
        public final ColumnID<Integer> _LAUNCH_TIMES = column("launch_times", Integer.class);

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
        public final ColumnID<Integer> _CATEGORY = column("category", Integer.class);;
        public Application() {
            define(3)
                .column(_CATEGORY, "INTEGER");
        }
    }

}
