package com.instagramapi.instagramapi.utils;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.friendships.Friendship;
import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsShowRequest;
import com.github.instagram4j.instagram4j.requests.users.UsersSearchRequest;
import com.github.instagram4j.instagram4j.responses.users.UsersSearchResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UserUtils {

  public static CompletableFuture<Long> findUserIdFromUsername(IGClient client, String username) {
    return client.sendRequest(new UsersSearchRequest(username))
        .thenApply(UsersSearchResponse::getUsers)
        .thenApply(users -> users.stream()
            .filter(user -> user.getUsername().equalsIgnoreCase(username))
            .findAny()
            .map(Profile::getPk)
            .orElse(null));
  }

  public static CompletableFuture<List<Long>> findUserIdsFromUsernames(IGClient client, List<String> usernames) {
    List<CompletableFuture<Long>> futures = usernames.stream()
        .map(username -> findUserIdFromUsername(client, username))
        .collect(Collectors.toList());

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
  }

  public static CompletableFuture<Boolean> isPrivateAccount(IGClient client, String username) {
    return client.sendRequest(new UsersSearchRequest(username))
        .thenApply(UsersSearchResponse::getUsers)
        .thenApply(users -> users.stream()
            .filter(user -> user.getUsername().equalsIgnoreCase(username))
            .findAny()
            .map(Profile::is_private)
            .orElse(false));
  }

  public static CompletableFuture<Boolean> isFollowing(IGClient client, String targetUsername) {
    return client.actions().users().findByUsername(targetUsername)
        .thenCompose(userAction -> {
          long targetUserId = userAction.getUser().getPk();
          return client.sendRequest(new FriendshipsShowRequest(targetUserId));
        })
        .thenApply(response -> {
          Friendship friendship = response.getFriendship();
          return friendship != null && friendship.isFollowing();
        });
  }
}
