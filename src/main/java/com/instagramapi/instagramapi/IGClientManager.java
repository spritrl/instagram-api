package com.instagramapi.instagramapi;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class IGClientManager {

  public static IGClient createClient() throws IOException {
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
      System.out.println("Successfully logged in !" + username + password);
      System.out.println("client" + client);
      return client;
    } catch (IGLoginException e) {
      e.printStackTrace();
      return null;
    } finally {
      scanner.close();
    }
  }
}
