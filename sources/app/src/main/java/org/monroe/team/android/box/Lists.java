package org.monroe.team.android.box;

import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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

    public static <DataType> void iterateAndRemove(Collection<DataType> list, Closure<Iterator<DataType>, Boolean> closure) {
        Iterator<DataType> iterator = list.iterator();
        while (iterator.hasNext()){
            if(closure.execute(iterator)){
                return;
            }
        }
    }
}
