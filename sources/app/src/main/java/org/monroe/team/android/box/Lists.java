package org.monroe.team.android.box;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Lists {
    public static <DataType> void each(Collection<DataType> collection, Closure<DataType,Void> closure){
        for (DataType dataType : collection) {
            closure.execute(dataType);
        }
    }

    public static <ToData,FromData> List<ToData> collect(Collection<FromData> collection, Closure<FromData,ToData> closure){
        List<ToData> answerList = new ArrayList<ToData>(collection.size());
        for (FromData origin : collection) {
           ToData transformed = closure.execute(origin);
           if (transformed != null) answerList.add(transformed);
        }
        return answerList;
    }
}
