package org.monroe.team.runit.app.android.preneter;

import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class RefreshableListAdapter {

    private final RunitApp app;
    private final ViewGroup viewGroup;
    private final int itemCountToShow;
    private final List<Pair<View,ListDataItem>> visibleItemList;
    private final List<AddItemRequest> addRequestList;
    private final LayoutInflater inflater;
    private final int layoutId;

    public RefreshableListAdapter(RunitApp app, ViewGroup viewGroup, int layoutId,  int itemCountToShow) {
        this.app = app;
        this.viewGroup = viewGroup;
        this.itemCountToShow = itemCountToShow;
        visibleItemList = new ArrayList<Pair<View, ListDataItem>>(itemCountToShow);
        addRequestList = new ArrayList<AddItemRequest>(itemCountToShow);
        inflater = LayoutInflater.from(app.getApplicationContext());
        this.layoutId = layoutId;
    }

    public synchronized void refreshList(List<ApplicationData> applicationDataList){

        Set<ListDataItem> visibleLaunchersSet = new HashSet<ListDataItem>(visibleItemList.size());
        Set<ListDataItem> launchersToAddSet = new HashSet<ListDataItem>(visibleItemList.size());

        for (Pair<View, ListDataItem> viewListDataItemPair : visibleItemList) {
            visibleLaunchersSet.add(viewListDataItemPair.second);
        }

        for(int i=0; i<itemCountToShow && i<applicationDataList.size();i++){
            launchersToAddSet.add(new ListDataItem(applicationDataList.get(i)));
        }

        //sort which are shown and which have to be added
        //leaved which should be changed and which should be added
        Iterator<ListDataItem> toAddIterator = launchersToAddSet.iterator();
        while (toAddIterator.hasNext()){
            ListDataItem listDataItem = toAddIterator.next();
            if (visibleLaunchersSet.remove(listDataItem)){
                toAddIterator.remove();
            }
        }

        Iterator<ListDataItem> replaceIterator = visibleLaunchersSet.iterator();
        Iterator<ListDataItem> addIterator = launchersToAddSet.iterator();
        addRequestList.clear();
        while (addIterator.hasNext()){
            ListDataItem addItem = addIterator.next();
            ListDataItem replaceItem = null;
            if (replaceIterator.hasNext()){
               replaceItem = replaceIterator.next();
            }
            addRequestList.add(new AddItemRequest(addItem,replaceItem));
        }
        updateItems();
    }

    private synchronized void updateItems() {
        List<AddItemRequest> requestItemCLoneList = new ArrayList<AddItemRequest>(addRequestList);
        for (final AddItemRequest addItemRequest : requestItemCLoneList) {
            app.loadApplicationIcon(addItemRequest.newItem.data,new RunitApp.OnLoadApplicationIconCallback() {
                @Override
                public void load(ApplicationData applicationData, Drawable drawable) {
                    addItemRequest.newItem.icon = drawable;
                    updateVisibleView(addItemRequest);
                }
            });
        }
    }

    private synchronized void updateVisibleView(AddItemRequest request) {
        boolean isActual = checkIfRequestActual(request);
        if (!isActual) return;
        if (request.replaceItem == null){
           addNewView(request);
        } else {
           replaceView(request);
        }
    }

    private void replaceView(AddItemRequest itemRequest) {
        Iterator<Pair<View, ListDataItem>> iterator = visibleItemList.iterator();
        while (iterator.hasNext()){
            Pair<View, ListDataItem> viewListDataItemPair = iterator.next();
            if (viewListDataItemPair.second == itemRequest.replaceItem){
                viewGroup.removeView(viewListDataItemPair.first);
                iterator.remove();
                addNewView(itemRequest);
                return;
            }
        }
        throw new IllegalStateException("Couldn`t find view to replace");
    }

    private void addNewView(final AddItemRequest request) {
        ImageView imageView = (ImageView) inflater.inflate(layoutId,viewGroup,false);
        imageView.setImageDrawable(request.newItem.icon);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.launchApplication(request.newItem.data);
            }
        });
        viewGroup.addView(imageView);
        visibleItemList.add(new Pair<View, ListDataItem>(imageView,request.newItem));
    }

    private boolean checkIfRequestActual(AddItemRequest request) {
        boolean isActual = false;
        for (AddItemRequest addItemRequest : addRequestList) {
            if (request == addItemRequest) {
                isActual = true;
                break;
            }
        }
        return isActual;
    }

    public synchronized Set<ApplicationData> getVisibleApplications() {
        Set<ApplicationData> answer =new HashSet<ApplicationData>(visibleItemList.size());
        for (Pair<View, ListDataItem> viewListDataItemPair : visibleItemList) {
            answer.add(viewListDataItemPair.second.data);
        }
        for (AddItemRequest addItemRequest : addRequestList) {
            answer.add(addItemRequest.newItem.data);
            if (addItemRequest.replaceItem != null){
                answer.remove(addItemRequest.replaceItem.data);
            }
        }

        return answer;
    }

    private final static class AddItemRequest{

        private final ListDataItem newItem;
        private final ListDataItem replaceItem;

        private AddItemRequest(ListDataItem newItem, ListDataItem replaceItem) {
            this.newItem = newItem;
            this.replaceItem = replaceItem;
        }
    }

    private final static class ListDataItem{

        public final ApplicationData data;
        public Drawable icon;

        private ListDataItem(ApplicationData data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ListDataItem that = (ListDataItem) o;

            if (data != null ? !data.equals(that.data) : that.data != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return data != null ? data.hashCode() : 0;
        }
    }
}
