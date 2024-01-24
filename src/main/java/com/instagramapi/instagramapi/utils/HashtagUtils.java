package com.instagramapi.instagramapi.utils;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.requests.feed.FeedTagRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedTagResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

public class HashtagUtils {
    public static CompletableFuture<String> fetchPostsByHashtag(IGClient client, String hashtag) {
        return client.sendRequest(new FeedTagRequest(hashtag))
                .thenApply(response -> {
                    if (response instanceof FeedTagResponse) {
                        String httpResponse = ((FeedTagResponse) response).getHttpResponse();
                        return httpResponse;
                    } else {
                        throw new IllegalStateException("Error while fetching posts by hashtag.");
                    }
                })
                .exceptionally(throwable -> {
                    System.out.println("Error while fetching posts by hashtag :" + throwable.getMessage());
                    return null;
                });
    }

    public static List<JSONObject> extractMediaObjects(String jsonString) {
        JSONObject root = new JSONObject(jsonString);
        JSONObject data = root.getJSONObject("data");
        JSONArray sections = data.getJSONObject("top").getJSONArray("sections");

        List<JSONObject> mediaObjects = new ArrayList<>();

        for (int i = 0; i < sections.length(); i++) {
            JSONObject section = sections.getJSONObject(i);
            String layoutType = section.getString("layout_type");

            if ("media_grid".equals(layoutType)) {
                JSONObject layoutContent = section.getJSONObject("layout_content");
                if (layoutContent.has("medias")) {
                    JSONArray medias = layoutContent.getJSONArray("medias");
                    for (int j = 0; j < medias.length(); j++) {
                        mediaObjects.add(medias.getJSONObject(j));
                    }
                }
            }
        }

        return mediaObjects;
    }

    public static List<Long> extractMediaIds(List<JSONObject> extractedMedia) {
        List<Long> mediaIds = new ArrayList<>();

        for (JSONObject mediaObject : extractedMedia) {
            if (mediaObject.has("media") && mediaObject.getJSONObject("media").has("caption")) {
                JSONObject caption = mediaObject.getJSONObject("media").getJSONObject("caption");
                if (caption.has("media_id")) {
                    long mediaId = caption.getLong("media_id");
                    mediaIds.add(mediaId);
                }
            }
        }

        return mediaIds;
    }
}
