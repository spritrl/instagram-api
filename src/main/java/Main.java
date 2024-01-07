import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import java.util.Scanner;
import java.util.concurrent.Callable;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Callable<String> inputCode = () -> {
            System.out.print("Veuillez saisir le code : ");
            return scanner.nextLine();
        };

        LoginHandler twoFactorHandler = (client, response) -> {
            return IGChallengeUtils.resolveTwoFactor(client, response, inputCode);
        };

        try {
            IGClient client = IGClient.builder()
                                      .username("")
                                      .password("")
                                      .onTwoFactor(twoFactorHandler)
                                      .login();
            System.out.println("Connexion réussie !");
        } catch (IGLoginException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
