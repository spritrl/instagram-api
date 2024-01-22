package com.instagramapi.instagramapi;

import com.github.instagram4j.instagram4j.IGClient;
import com.instagramapi.instagramapi.utils.PostUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.instagramapi.instagramapi.utils.InteractionUtils.followUnfollowAction;
import static com.instagramapi.instagramapi.utils.PostUtils.fetchCommentersOfLatestPost;
import static com.instagramapi.instagramapi.utils.UserUtils.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            IGClient client = IGClientManager.createClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
