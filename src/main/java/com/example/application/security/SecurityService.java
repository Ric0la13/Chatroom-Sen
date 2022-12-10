package com.example.application.security;

import com.example.application.model.ApplicationUser;
import com.example.application.utils.UserUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component
public class SecurityService implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    private static final String LOGOUT_SUCCESS_URL = "/";
    private final UserDetailsManager userDetailsManager;

    private static final Set<String> loggedInUserIds = new HashSet<>();

    public SecurityService(UserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
    }

    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        // Anonymous or no authentication.
        return null;
    }

    public List<UserDetails> getAllUsers() {

        try {
            Field usersField = InMemoryUserDetailsManager.class.getDeclaredField("users");
            usersField.setAccessible(true);

            Map<String, UserDetails> userDetailsMap = (Map<String, UserDetails>) usersField.get(userDetailsManager);

            return new ArrayList<>(userDetailsMap.values());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return List.of();
        }
    }

    public List<UserDetails> getLoggedInUsers() {
        return loggedInUserIds.stream()
                .map(userDetailsManager::loadUserByUsername)
                .toList();
    }

    public void logout() {
        UserDetails authenticatedUser = getAuthenticatedUser();

        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);

        loggedInUserIds.remove(authenticatedUser.getUsername());
    }

    public boolean userNameStillAvailable(String username) {
        return !userDetailsManager.userExists(username);
    }

    public void register(ApplicationUser applicationUser) {
        UserUtils.addUser(applicationUser);
        userDetailsManager.createUser(User
                .withUsername(applicationUser.getUserName())
                .password("{noop}" + applicationUser.getPassword())
                .roles("USER")
                .build()
        );

    }

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
        loggedInUserIds.add(userDetails.getUsername());
    }
}