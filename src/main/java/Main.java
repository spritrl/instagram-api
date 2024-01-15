import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.models.media.timeline.Comment;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest.FriendshipsAction;
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.requests.media.MediaGetCommentsRequest;
import com.github.instagram4j.instagram4j.responses.media.MediaGetCommentsResponse;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import java.util.*;
import java.util.concurrent.Callable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

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
