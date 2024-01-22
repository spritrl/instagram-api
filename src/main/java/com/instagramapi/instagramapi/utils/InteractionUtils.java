package com.instagramapi.instagramapi.utils;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest.FriendshipsAction;

public class InteractionUtils {

    private static final String ACTION_FOLLOW = "Follow";

    public static void followUnfollowAction(IGClient client, String username, String action) {
        client.actions().users().findByUsername(username)
                .thenAccept(userAction -> {
                    userAction
                            .action(ACTION_FOLLOW.equals(action) ? FriendshipsAction.CREATE : FriendshipsAction.DESTROY)
                            .thenAccept(response -> {
                                if (response.getStatus().equals("ok")) {
                                    System.out.println(
                                            "[followUnfolowAction] "
                                                    + (ACTION_FOLLOW.equals(action) ? ACTION_FOLLOW : "Unfollow")
                                                    + " request sent.");
                                } else {
                                    System.out.println(
                                            "[followUnfolowAction] "
                                                    + (ACTION_FOLLOW.equals(action) ? ACTION_FOLLOW : "Unfollow")
                                                    + " request error.");
                                }
                            })
                            .exceptionally(throwable -> {
                                System.out.println("[followUnfolowAction] Error : " + throwable.getMessage());
                                return null;
                            });
                })
                .exceptionally(throwable -> {
                    System.out.println("[followUnfolowAction] User not found : " + throwable.getMessage());
                    return null;
                })
                .join();
    }
}
