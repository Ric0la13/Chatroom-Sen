package com.example.application.views.profile;

import com.example.application.security.SecurityService;
import com.example.application.service.DisplayNameService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.io.IOUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.security.PermitAll;
import java.io.FileOutputStream;
import java.io.IOException;

@PageTitle("My Profile")
@PermitAll
@Route(value = "profile", layout = MainLayout.class)
public class ProfileView extends VerticalLayout {

    private final TextField name;
    private final DisplayNameService displayNameService;

    public ProfileView(SecurityService securityService, DisplayNameService displayNameService) {
        this.displayNameService = displayNameService;

        UserDetails authenticatedUser = securityService.getAuthenticatedUser();

        name = new TextField("Your name");
        Button changeDisplayName = new Button("Set display-name");
        changeDisplayName.addClickListener(e -> changeDisplayNameForUser(authenticatedUser));
        changeDisplayName.addClickShortcut(Key.ENTER);

        Upload dropEnabledUpload = createUpload(authenticatedUser);

        setMargin(true);

        HorizontalLayout nameContainer = new HorizontalLayout(name, changeDisplayName);
        nameContainer.setAlignItems(Alignment.BASELINE);

        add(nameContainer, dropEnabledUpload);
    }

    private Upload createUpload(UserDetails authenticatedUser) {

        Button uploadButton = new Button("Upload profile-picture...");

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        Upload dropEnabledUpload = new Upload(memoryBuffer);
        dropEnabledUpload.setId("upload-element");
        dropEnabledUpload.setUploadButton(uploadButton);

        dropEnabledUpload.setAcceptedFileTypes("image/png", ".png");

        String username = authenticatedUser.getUsername();
        String fileName = "src/main/webapp/VAADIN/profilepictures/" + username + ".png";

        dropEnabledUpload.addSucceededListener(event -> changeProfilePicture(memoryBuffer, fileName));
        dropEnabledUpload.addFileRejectedListener(event -> {
            Notification notification = Notification.show("file needs to be a png-file");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        dropEnabledUpload.addFailedListener(event -> {
            Notification notification = Notification.show("file can not be uploaded to server");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        return dropEnabledUpload;
    }

    private static void changeProfilePicture(MemoryBuffer memoryBuffer, String fileName) {
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            byte[] bytes = IOUtils.toByteArray(memoryBuffer.getInputStream());
            out.write(bytes);
        } catch (IOException e) {
            Notification.show("image can not be written to server");
        }
    }

    private void changeDisplayNameForUser(UserDetails authenticatedUser) {
        if (name.getValue().isBlank()) {
            Notification.show("Your display-name may not be blank");
        }
        displayNameService.setDisplayName(authenticatedUser, name.getValue());
        Notification.show("Your display-name has sucessfully changed to: " + name.getValue());
    }

}
