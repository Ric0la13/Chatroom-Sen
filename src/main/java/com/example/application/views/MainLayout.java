package com.example.application.views;


import com.example.application.components.appnav.AppNav;
import com.example.application.components.appnav.AppNavItem;
import com.example.application.security.LoginView;
import com.example.application.security.SecurityService;
import com.example.application.views.chat.ChatView;
import com.example.application.views.profile.ProfileView;
import com.example.application.views.userlist.UserlistView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.FileInputStream;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
    private final Environment environment;
    private H2 viewTitle;

    public MainLayout(SecurityService securityService, Environment environment) {
        this.securityService = securityService;
        this.environment = environment;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        if (authenticatedUser != null) {

            Span userName = new Span(authenticatedUser.getUsername());
            userName.setId("user-id-header");

            Button logout = new Button("Logout", click ->
                    securityService.logout());
            logout.setId("logout-button");

            addToNavbar(true, toggle, viewTitle,
                    userName, getProfilePicture(authenticatedUser), logout);
        } else {
            Button login = new Button("Login", click ->
                    UI.getCurrent().navigate(LoginView.class));
            login.addClassName(LumoUtility.Margin.Left.AUTO);
            login.addClassName(LumoUtility.Margin.Right.SMALL);
            addToNavbar(true, toggle, viewTitle, login);
        }
    }

    private Image getProfilePicture(UserDetails userDetails) {
        String userId = userDetails.getUsername();
        StreamResource imageResource = getStreamResource(userId);

        Image profilePicture;

        if (imageResource == null) {
            profilePicture = new Image("public/images/default-avatar.png", "Profile Picture from " + userId);
        } else {
            profilePicture = new Image(imageResource, "Profile Picture from " + userId);
            profilePicture.getElement().setAttribute("onError", "{"
                    + "event.target.src = \"public/images/default-avatar.png\"}");
        }

        profilePicture.addClassNames("profile", "small");
        return profilePicture;
    }

    private StreamResource getStreamResource(String userId) {
        String property = environment.getProperty("image.profile");
        if (property == null) return null;
        String profilePicturePath = property.formatted(userId);

        return new StreamResource(userId + ".png", () -> {
            try {
                return new FileInputStream(profilePicturePath);
            }
            catch (Exception e) {
                return null;
            }
        });
    }

    private void addDrawerContent() {
        H1 appName = new H1("Chatroom-Sen");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();

        AppNavItem myProfile = new AppNavItem("My Profile", ProfileView.class, VaadinIcon.USER.create());
        AppNavItem userlist = new AppNavItem("Userlist", UserlistView.class, VaadinIcon.USERS.create());
        AppNavItem chat = new AppNavItem("Chat", ChatView.class, VaadinIcon.CHAT.create());
        nav.addItem(myProfile, userlist, chat);

        UI.getCurrent().getPage().fetchCurrentURL(url -> {
            if (url.getPath().equals("/")) {
                chat.getElement().setAttribute("active", true);
            }
        });
        return nav;
    }

    private Footer createFooter() {
        return new Footer();
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
