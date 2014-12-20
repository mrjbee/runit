package org.monroe.team.runit.app.service;

import android.content.Context;

import org.monroe.team.android.box.utils.ResourceUtils;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.android.RunitApp;

import java.util.List;

public class CategoryNameResolver {

    private final Context context;

    public CategoryNameResolver(Context context) {
        this.context = context;
    }

    public String categoryNameById(Long id) {

        if (id==null){
            return context.getResources().getString(R.string.not_fetched_yet);
        }

        PlayMarketDetailsProvider.PlayMarketCategory playMarketCategory = convertToPlayMarketCategory(id);
        if (playMarketCategory != null){
            Integer resourceId = ResourceUtils.resourceID(R.string.class,"play_market_category_"+playMarketCategory.name());
            if (resourceId == null){
                return playMarketCategory.name();
            } else {
                return context.getResources().getString(resourceId);
            }
        }

        return "Unknown Category ("+id+")";
    }


    private PlayMarketDetailsProvider.PlayMarketCategory convertToPlayMarketCategory(Long id) {
        PlayMarketDetailsProvider.PlayMarketCategory playMarketCategory = null;
        for (PlayMarketDetailsProvider.PlayMarketCategory category : PlayMarketDetailsProvider.PlayMarketCategory.values()) {
            if (category.ordinal() == id){
                playMarketCategory = category;
                break;
            }
        }
        return playMarketCategory;
    }

}
