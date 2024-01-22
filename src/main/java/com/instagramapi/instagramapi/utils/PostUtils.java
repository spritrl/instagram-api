package com.instagramapi.instagramapi.utils;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.media.timeline.Comment;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.requests.media.MediaGetCommentsRequest;
import com.github.instagram4j.instagram4j.requests.media.MediaGetLikersRequest;
import com.github.instagram4j.instagram4j.responses.media.MediaGetCommentsResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PostUtils {

  public static CompletableFuture<ArrayList<String>> fetchCommentersOfLatestPost(IGClient client, String username) {
    return client.actions().users().findByUsername(username)
        .thenCompose(userAction -> new FeedUserRequest(userAction.getUser().getPk()).execute(client))
        .thenCompose(feedResponse -> {
          List<TimelineMedia> medias = feedResponse.getItems();
          if (!medias.isEmpty()) {
            TimelineMedia lastPost = medias.get(0);
            return getAllCommentingUsers(client, lastPost.getId());
          } else {
            System.out.println("[fetchCommentersOfLatestPost] No post found.");
            return CompletableFuture.completedFuture(new ArrayList<>());
          }
        })
        .exceptionally(throwable -> {
          System.out.println("[fetchCommentersOfLatestPost] Error : " + throwable.getMessage());
          return new ArrayList<>();
        });
  }

  public static CompletableFuture<ArrayList<String>> fetchLikersOfLatestPost(IGClient client, String username) {
    return client.actions().users().findByUsername(username)
        .thenCompose(userAction -> new FeedUserRequest(userAction.getUser().getPk()).execute(client))
        .thenCompose(feedResponse -> {
          List<TimelineMedia> medias = feedResponse.getItems();
          if (!medias.isEmpty()) {
            TimelineMedia lastPost = medias.get(0);
            return new MediaGetLikersRequest(lastPost.getId()).execute(client);
          } else {
            System.out.println("[fetchLikersOfLatestPost] No post found.");
            return CompletableFuture.completedFuture(null);
          }
        })
        .thenApply(likersResponse -> {
          if (likersResponse != null) {
            System.out.println(
                "[fetchLikersOfLatestPost] Users list size : " + likersResponse.getUsers().size());
            return likersResponse.getUsers()
                .stream()
                .map(user -> user.getUsername())
                .collect(Collectors.toCollection(ArrayList::new));
          } else {
            return new ArrayList<String>();
          }
        })
        .exceptionally(throwable -> {
          System.out.println("Une erreur est survenue : " + throwable.getMessage());
          return new ArrayList<String>();
        });
  }

  private static CompletableFuture<ArrayList<String>> getAllCommentingUsers(IGClient client, String mediaId) {
    return CompletableFuture.supplyAsync(() -> {
      ArrayList<String> commenterList = new ArrayList<>();
      String nextMinId = null;
      do {
        try {
          MediaGetCommentsResponse response = client
              .sendRequest(new MediaGetCommentsRequest(mediaId, nextMinId)).join();
          for (Comment comment : response.getComments()) {
            System.out
                .println("[getAllComments] User from minId " + nextMinId + " - " + comment.getUser().getUsername());
            commenterList.add(comment.getUser().getUsername());
          }
          nextMinId = response.getNext_min_id();
          Thread.sleep(10000);
        } catch (Exception e) {
          System.out.println("[getAllCommentingUsers] Error : " + e.getMessage());
          break;
        }
      } while (nextMinId != null);
      System.out.println("[getAllCommentingUsers] Users list: " + commenterList);
      System.out.println("[getAllCommentingUsers] List size: " + commenterList.size());
      return commenterList;
    });
  }

  public static CompletableFuture<Pair<Long, Long>> fetchLastPostTimestamp(IGClient client, Long userId,
      String username) {
    return UserUtils.isPrivateAccount(client, username)
        .thenCompose(isPrivate -> {
          if (isPrivate) {
            return CompletableFuture.completedFuture(null);
          } else {
            return new FeedUserRequest(userId).execute(client)
                .thenApply(feedResponse -> {
                  List<TimelineMedia> medias = feedResponse.getItems();
                  if (!medias.isEmpty()) {
                    long timestamp = medias.get(0).getTaken_at();
                    return new Pair<>(userId, timestamp);
                  }
                  return null;
                });
          }
        });
  }

  public static CompletableFuture<List<Long>> sortUsersByRecentActivity(IGClient client, List<String> usernames) {
    List<CompletableFuture<Pair<Long, Long>>> futures = new ArrayList<>();

    for (String username : usernames) {
      futures.add(
          UserUtils.findUserIdFromUsername(client, username)
              .thenCompose(userId -> fetchLastPostTimestamp(client, userId, username)));
    }

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Pair<Long, Long>::getSecond).reversed())
            .map(Pair::getFirst)
            .collect(Collectors.toList()));
  }
}