import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.models.media.timeline.Comment;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest.FriendshipsAction;
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.requests.media.MediaGetCommentsRequest;
import com.github.instagram4j.instagram4j.requests.media.MediaGetLikersRequest;
import com.github.instagram4j.instagram4j.requests.users.UsersSearchRequest;
import com.github.instagram4j.instagram4j.responses.media.MediaGetCommentsResponse;
import com.github.instagram4j.instagram4j.responses.users.UsersSearchResponse;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import java.util.*;
import java.util.concurrent.Callable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        Scanner scanner = new Scanner(System.in);

        Callable<String> inputCode = () -> {
            System.out.print("Verification code : ");
            return scanner.nextLine();
        };

        LoginHandler twoFactorHandler = (client, response) -> {
            return IGChallengeUtils.resolveTwoFactor(client, response, inputCode);
        };

        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
            String username = prop.getProperty("username");
            String password = prop.getProperty("password");

            IGClient client = IGClient.builder()
                    .username(username)
                    .password(password)
                    .onTwoFactor(twoFactorHandler)
                    .login();
            System.out.println("Successfully logged in !");
        } catch (IGLoginException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static CompletableFuture<Boolean> isPrivateAccount(IGClient client, String username) {
        return client.sendRequest(new UsersSearchRequest(username))
                .thenApply(UsersSearchResponse::getUsers)
                .thenApply(users -> users.stream()
                        .filter(user -> user.getUsername().equalsIgnoreCase(username))
                        .findAny()
                        .map(user -> user.is_private())
                        .orElse(false));
    }

    private static CompletableFuture<Pair<Long, Long>> fetchLastPostTimestamp(IGClient client, Long userId,
            String username) {
        return isPrivateAccount(client, username)
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

    private static CompletableFuture<List<Long>> sortUsersByRecentActivity(IGClient client, List<String> usernames) {
        List<CompletableFuture<Pair<Long, Long>>> futures = new ArrayList<>();

        for (String username : usernames) {
            futures.add(client.actions().users().findByUsername(username)
                    .thenCompose(userAction -> fetchLastPostTimestamp(client, userAction.getUser().getPk(), username)));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(Pair<Long, Long>::getSecond).reversed())
                        .map(Pair::getFirst)
                        .collect(Collectors.toList()));
    }

    private static CompletableFuture<ArrayList<String>> fetchLikersOfLatestPost(IGClient client, String username) {
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

    private static CompletableFuture<ArrayList<String>> fetchCommentersOfLatestPost(IGClient client, String username) {
        return client.actions().users().findByUsername(username)
                .thenCompose(userAction -> new FeedUserRequest(userAction.getUser().getPk()).execute(client))
                .thenCompose(feedResponse -> {
                    List<TimelineMedia> medias = feedResponse.getItems();
                    if (!medias.isEmpty()) {
                        TimelineMedia lastPost = medias.get(0);
                        return getAllCommentingUsers(client, lastPost.getId());
                    } else {
                        System.out.println("[fetchCommentersOfLatestPost] No post found.");
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .exceptionally(throwable -> {
                    System.out.println("[fetchCommentersOfLatestPost] Error : " + throwable.getMessage());
                    return null;
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
                        System.out.println("[getAllComments] User from minId " + nextMinId + " - "
                                + comment.getUser().getUsername());
                        commenterList.add(comment.getUser().getUsername());
                    }
                    nextMinId = response.getNext_min_id();
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            } while (nextMinId != null);
            System.out.println("[getAllCommentUser] Users list: " + commenterList);
            System.out.println("[getAllCommentUser] List size: " + commenterList.size());
            return commenterList;
        });
    }

    public static void followUnfolowAction(IGClient client, String username, String action) {
        client.actions().users().findByUsername(username)
                .thenAccept(userAction -> {
                    userAction.action(action == "Follow" ? FriendshipsAction.CREATE : FriendshipsAction.DESTROY)
                            .thenAccept(response -> {
                                if (response.getStatus().equals("ok")) {
                                    System.out.println("[followUnfolowAction] Follow request sent.");
                                } else {
                                    System.out.println("[followUnfolowAction] Follow request error.");
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
