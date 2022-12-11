package com.example.application.views.userlist;

import com.example.application.security.SecurityService;
import com.example.application.service.DisplayNameService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@PageTitle("Userlist")
@PermitAll
@Route(value = "userlist", layout = MainLayout.class)
public class UserlistView extends VerticalLayout {

    private final List<UserDetails> loggedInUsers;
    private final Environment environment;

    public UserlistView(Environment environment,
                        SecurityService securityService,
                        DisplayNameService displayNameService) {
        this.environment = environment;

        List<UserDetails> allUsers = securityService.getAllUsers();
        loggedInUsers = securityService.getLoggedInUsers();

        Grid<UserDetails> userGrid = new Grid<>();
        userGrid.setSizeFull();
        userGrid.setItems(allUsers);

        userGrid.addComponentColumn(this::getProfilePicture)
                .setWidth("5rem").setFlexGrow(0);
        userGrid.addColumn(UserDetails::getUsername)
                .setHeader("Username");
        userGrid.addColumn(userDetails -> displayNameService.getDisplayName(userDetails).orElse(""))
                .setHeader("Displayname");
        userGrid.addComponentColumn(UserlistView::getAuthorityList)
                .setHeader("Roles");
        userGrid.addComponentColumn(this::getStatusBatch)
                .setHeader("Status");

        add(userGrid);
        setSizeFull();
    }

    private Span getStatusBatch(UserDetails user) {
        boolean loggedIn = loggedInUsers.stream().map(UserDetails::getUsername).toList().contains(user.getUsername());

        if (loggedIn) {
            Span online = new Span("ONLINE");
            online.addClassName("online");
            return online;
        } else {
            Span offline = new Span("OFFLINE");
            offline.addClassName("offline");
            return offline;
        }
    }

    private static Div getAuthorityList(UserDetails userDetails) {
        Span[] roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.split("_")[1])
                .map(Span::new)
                .toArray(Span[]::new);

        for (Span role : roles) {
            role.addClassName("role");
        }

        Div roleContainer = new Div(roles);
        roleContainer.addClassName("role-container");
        return roleContainer;
    }

    private Image getProfilePicture(UserDetails userDetails) {
        String userId = userDetails.getUsername();
        StreamResource profileResource = getProfileResource(userId);

        Image profilePicture;
        if (profileResource == null) {
            profilePicture = new Image("public/images/default-avatar.png", "Profile Picture from " + userId);
        } else {
            profilePicture = new Image(profileResource, "Profile Picture from " + userId);
            profilePicture.getElement().setAttribute("onError", "{"
                    + "event.target.src = \"public/images/default-avatar.png\"}");
        }
        profilePicture.addClassName("profile");
        return profilePicture;
    }

    private StreamResource getProfileResource(String userId) {
        String property = environment.getProperty("image.profile");
        if (property == null) return null;
        String profilePicturePath = property + "/" + userId + ".png";

        File f = new File(profilePicturePath);

        if (!f.exists()) {
            return null;
        }

        return new StreamResource(userId + ".png", () -> {
            try {
                return new FileInputStream(profilePicturePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
