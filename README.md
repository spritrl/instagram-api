# Instagram-Api Project

## Table of Contents
- [Description](#description)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
  - [Terms and Conditions](#terms-and-conditions)
  - [Connecting to an Instagram Account](#connecting-to-an-instagram-account)
  - [Security Note](#security-note)
  - [Basic Operations](#basic-operations)
- [Contribution](#contribution)

## Description
This project utilizes the `instagram4j` API for interacting with Instagram. It is designed to perform various automated actions such as fetching users who liked a specific post, obtaining commenters of a post, and more.

## Features
- **Follow and Unfollow Users:** Programmatically follow or unfollow specific Instagram users using `followUnfollowAction`.
- **Retrieve User Profiles Who Commented on a Post:** Fetch a list of Instagram usernames who have commented on a specific post using `fetchCommentersOfLatestPost`.
- **Fetch Users Who Liked a Post:** Get a list of all users who liked a specific post on Instagram with `fetchLikersOfLatestPost`, handling pagination for large user lists.
- **Fetch User IDs from Usernames:** Convert a list of Instagram usernames to user IDs with `findUserIdsFromUsernames`.
- **Check Account Privacy:** Determine if an account is private using `isPrivateAccount`.
- **Check Following Status:** Find out if the current user is following a specific account using `isFollowing`.
- **Fetch Last Post Timestamp:** Retrieve the timestamp of the last post of a user with `fetchLastPostTimestamp`.
- **Sort Users by Recent Activity:** Order a list of users based on their recent activity using `sortUsersByRecentActivity`.
- **Robust Error Handling:** Includes comprehensive error handling for scenarios like user not found, login issues, etc.
- **Asynchronous Operations:** Leverages Java's `CompletableFuture` for asynchronous API calls, ensuring efficient performance and non-blocking operations.

## Prerequisites
- Java 8 or higher (Java 11, OpenJDK 11.0.10 recommended)
- Maven
- An Instagram account

## Installation
Provide steps to install and set up your project. For example:
1. Clone the repository: `git clone https://github.com/spritrl/instagram-api.git`.
2. Install Maven dependancies.
4. Configure file `config.properties` (check [Connecting to an Instagram Account](#connecting-to-an-instagram-account)).

## Usage

### Terms and Conditions
This library is crafted for educational purposes and personal experimentation, considering Instagram's public API limitations.

- **Prefer Official API:** Where feasible, utilize the official Instagram public API.
- **No Spam Activities:** Refrain from using this library for any form of spamming (bot actions, unsolicited messaging, etc.).
- **Disallowance of Malicious Intent:** Assistance will not be extended to those using the library for harmful purposes.
- **Human-like Interaction:** Implement realistic delays between requests to mimic human interaction and prevent server overload.
- **Ethical Principles:** Adhere to ethical guidelines and responsible use. Misuse of this tool is strongly discouraged.
- **Independent and Unofficial:** This library is independently created and is not in any official capacity associated with, endorsed by, or affiliated with Instagram or its parent companies. It is an unofficial API, and its usage is at the user's discretion and risk.

**Disclaimer from Contributors:** The contributors of this library bear no responsibility for its usage or ongoing maintenance. Due to the evolving nature of this project, features may change or become obsolete over time. The project is distributed under the Apache Software License (ASL).

---
## Connecting to an Instagram Account

To use this project with your Instagram account, you need to configure your credentials in the `config.properties` file. Here's how you can do it:

1. **Locate the `config.properties` File:**
   Find the `config.properties` file in the root directory of the project.

2. **Edit the File:**
   Open the file in a text editor and fill in your Instagram username and password:
    ```
    username=your_instagram_username
    password=your_instagram_password
    ```

3. **Save the Changes:**
After editing, save the file. The application will use these credentials to log in to Instagram when you run it.

### Security Note
It's important to keep your Instagram credentials secure. Do not share the `config.properties` file or your credentials with anyone. If you're using a version control system like git, make sure to add `config.properties` to your `.gitignore` file to prevent it from being uploaded to a public repository.

---
### Basic Operations

- **Follow/Unfollow a User:**
  Execute a follow or unfollow action for a specified username.
  ```java
  followUnfollowAction(client, "target_username", "Follow");
  ```
- **Fetch Likers of the Latest Post:**
Retrieve a list of users who liked the latest post of a specified user.
    ```java
    PostUtils
        .fetchLikersOfLatestPost(client, "username")
        .thenAccept(likers -> {
            likers.forEach(System.out::println);
        });
    ```
- **Get Commenters of a Post:**
Obtain a list of users who commented on the latest post of a specified user.
    ```java
    fetchCommentersOfLatestPost(client, "username")
    .thenAccept(commenters -> {
        commenters.forEach(System.out::println);
    });
    ```
## Contribution
Instructions for those who wish to contribute to the project. For example:
- Fork the repository.
- Create a new branch for your changes (`git checkout -b feature/new_feature`).
- Submit your changes via a Pull Request.
