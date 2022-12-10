package com.example.application.security;

import com.example.application.model.ApplicationUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Route("register")
@AnonymousAllowed
@PageTitle("Sign Up | Vaadin CRM")
public class SignupView extends VerticalLayout {

    private final Binder<ApplicationUser> binder;
    private final SecurityService securityService;

    public SignupView(SecurityService securityService) {
        this.securityService = securityService;
        binder = new Binder<>();

        addClassName("signup-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addClassNames(LumoUtility.AlignSelf.CENTER);
        verticalLayout.setWidth(344, Unit.PIXELS);
        verticalLayout.getStyle().set("padding-top", "2px");

        FormLayout signupForm = new FormLayout();

        TextField nickname = new TextField("Nickname");
        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        PasswordField passwordConfirm = new PasswordField("Confirm password");

        nickname.addClassName(LumoUtility.Padding.Top.NONE);

        signupForm.add(nickname, username, password, passwordConfirm);

        Button createAccount = new Button("Create account", click -> handleRegisterButton());
        createAccount.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createAccount.setWidthFull();

        H2 title = new H2("Sign up");
        title.addClassName(LumoUtility.Margin.NONE);
        verticalLayout.add(title, signupForm, createAccount);

        binder.forField(nickname).asRequired()
                .bind(ApplicationUser::getNickname, ApplicationUser::setNickname);
        binder.forField(username).asRequired()
                .withValidator(user -> user.length() == 5, "Username needs to be exactly five characters long")
                .withValidator(securityService::userNameStillAvailable, "Username is already taken")
                .bind(ApplicationUser::getUserName, ApplicationUser::setUserName);
        binder.forField(password).asRequired()
                .withValidator(pw -> pw.length() > 8, "Password must contain at least eight characters")
                .bind(ApplicationUser::getPassword, ApplicationUser::setPassword);
        binder.forField(passwordConfirm).asRequired()
                .withValidator(pw -> password.getValue().equals(pw), "Passwords don't match")
                .bind(a -> null, (a, b) -> {});

        RouterLink toLogin = new RouterLink("Go to Login", LoginView.class);

        add(new H1("Signup to Chatroom"), verticalLayout, toLogin);
    }

    private void handleRegisterButton() {
        try {
            ApplicationUser applicationUser = new ApplicationUser();
            binder.writeBean(applicationUser);
            securityService.register(applicationUser);
            Notification notification = Notification.show("successfully registered");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(LoginView.class);
        } catch (ValidationException e) {
            log.error("user can not be registered", e);
        }
    }
}
