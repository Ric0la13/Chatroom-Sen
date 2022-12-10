package com.example.application.utils;

import com.example.application.model.ApplicationUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserUtils {
    private static final List<ApplicationUser> allUsers = new ArrayList<>();

    public static void addUser(ApplicationUser user) {
        allUsers.add(user);
    }

    public static List<ApplicationUser> getAllUsers() {
        return allUsers;
    }

    public static Optional<ApplicationUser> getCurrentApplicationUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof User user) {
            return allUsers.stream()
                    .filter(e -> user.getUsername().equals(e.getUserName()))
                    .findFirst();
        }
        return Optional.empty();

    }
}