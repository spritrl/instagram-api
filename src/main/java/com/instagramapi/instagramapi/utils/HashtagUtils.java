package com.instagramapi.instagramapi.utils;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.requests.feed.FeedTagRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedTagResponse;

import java.util.concurrent.CompletableFuture;

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
}
