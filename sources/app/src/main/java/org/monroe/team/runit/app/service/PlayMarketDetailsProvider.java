package org.monroe.team.runit.app.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class PlayMarketDetailsProvider {

    //https://play.google.com/store/apps/details?id=com.aspartame.RemindMe&hl=ru
    private final String BASE_URL = "https://play.google.com/store/apps/details";

    private HttpClient httpClient;

    private HttpClient getHttpClient() {
        if (httpClient == null){
            httpClient = new DefaultHttpClient();
        }
        return httpClient;
    }

    public String fetchData(String packageName) throws IOException, BlockException {
        HttpGet request = new HttpGet(BASE_URL+"?id="+packageName);
        HttpContext HTTP_CONTEXT = new BasicHttpContext();
        HTTP_CONTEXT.setAttribute(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
        request.setHeader("Referer", "http://www.google.com");
        HttpResponse response = getHttpClient().execute(request, HTTP_CONTEXT);//404 if not exists
        if (response.getStatusLine().getStatusCode() == 403 || response.getStatusLine().getStatusCode() == 400 ){
            throw new BlockException();
        }
        if (response.getStatusLine().getStatusCode() == 404){
            return null;
        }
        if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 400) {
            throw new IOException("Got bad response, error code = " + response.getStatusLine().getStatusCode());
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new IOException("No body ? = " + response.getStatusLine().getStatusCode());
        }
        return EntityUtils.toString(entity);
    }

    public PlayMarketCategory getCategory(String packageName) throws IOException, BlockException {
        String data = fetchData(packageName);
        if (data == null){
            return PlayMarketCategory.NONE;
        }
        for (PlayMarketCategory category : PlayMarketCategory.values()) {
            if (data.contains("class=\"document-subtitle category\" href=\"/store/apps/category/"+category.name())){
                return category;
            }
        }
        return PlayMarketCategory.UNDEFINED;
    }

    public final class BlockException extends Exception{}

    public enum PlayMarketCategory{
        NONE,
        UNDEFINED,
        BOOKS_AND_REFERENCE,BUSINESS,COMICS,COMMUNICATION,EDUCATION,ENTERTAINMENT,
        FINANCE,HEALTH_AND_FITNESS,LIBRARIES_AND_DEMO,LIFESTYLE,MEDIA_AND_VIDEO,
        MEDICAL,MUSIC_AND_AUDIO,NEWS_AND_MAGAZINES,PERSONALIZATION,PHOTOGRAPHY,
        PRODUCTIVITY,SHOPPING,SOCIAL,SPORTS,TOOL,TRANSPORTATION,
        TRAVEL_AND_LOCAL,WEATHER,
        GAME
    }
}
