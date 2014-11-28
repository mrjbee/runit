package org.monroe.team.android.box.db;

import android.provider.BaseColumns;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.Lists;

import java.util.ArrayList;
import java.util.List;

public class Schema {

    public final int version;
    public final String name;
    private final List<Table> tableList;

    public Schema(int version, String name, Class<? extends Table>... tables){
        this.version = version;
        this.name = name;
        tableList = new ArrayList<Table>(tables.length);
        for (Class<? extends Table> tableClass : tables) {
            try {
                Table table = tableClass.newInstance();
                tableList.add(table);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void withEachTable(Closure<Table,Void> action){
        Lists.each(tableList, action);
    }

    public static interface Table {
        public String createScript();
    }

}
