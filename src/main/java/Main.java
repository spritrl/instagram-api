import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest.FriendshipsAction;
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        Scanner scanner = new Scanner(System.in);

        Callable<String> inputCode = () -> {
            System.out.print("Veuillez saisir le code : ");
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
            System.out.println("Connexion réussie !");
            client.actions().users().findByUsername("instagram")
            .thenAccept(userAction -> {
                userAction.action(FriendshipsAction.CREATE)
                    .thenAccept(response -> {
                        if (response.getStatus().equals("ok")) {
                            System.out.println("Demande de suivi envoyée avec succès.");
                        } else {
                            System.out.println("Échec de l'envoi de la demande de suivi.");
                        }
                    })
                    .exceptionally(throwable -> {
                        System.out.println("Une erreur est survenue : " + throwable.getMessage());
                        return null;
                    });
            })
            .exceptionally(throwable -> {
                System.out.println("Une erreur lors de la recherche de l'utilisateur : " + throwable.getMessage());
                return null;
            })
            .join();
        } catch (IGLoginException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
