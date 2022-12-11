package com.example.application.views.profile;

import com.example.application.repository.UserRepository;
import com.example.application.security.SecurityService;
import com.example.application.service.DisplayNameService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@PageTitle("My Profile")
@PermitAll
@Route(value = "profile", layout = MainLayout.class)
public class ProfileView extends VerticalLayout {

    private final TextField name;
    private final DisplayNameService displayNameService;
    private final UserRepository userRepository;
    private final Environment environment;

    public ProfileView(SecurityService securityService, DisplayNameService displayNameService,
                       UserRepository userRepository, Environment environment) {
        this.displayNameService = displayNameService;
        this.userRepository = userRepository;
        this.environment = environment;

        UserDetails authenticatedUser = securityService.getAuthenticatedUser();

        name = new TextField("Your name");
        Button changeDisplayName = new Button("Change display-name");
        changeDisplayName.addClickListener(e -> changeDisplayNameForUser(authenticatedUser));
        changeDisplayName.addClickShortcut(Key.ENTER);

        Label profilePictureLabel = new Label("Select your Profile Picture");

        Upload dropEnabledUpload = createUpload(authenticatedUser);

        setMargin(true);

        HorizontalLayout nameContainer = new HorizontalLayout(name, changeDisplayName);
        nameContainer.setAlignItems(Alignment.BASELINE);

        add(nameContainer, profilePictureLabel, dropEnabledUpload);
    }

    private Upload createUpload(UserDetails authenticatedUser) {

        Button uploadButton = new Button("Upload profile-picture...");

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        Upload dropEnabledUpload = new Upload(memoryBuffer);
        dropEnabledUpload.setId("upload-element");
        dropEnabledUpload.setMaxFileSize(50_000_000);
        dropEnabledUpload.setUploadButton(uploadButton);

        dropEnabledUpload.setAcceptedFileTypes("image/png", ".png", "image/jpeg", ".jpg", ".jpeg");

        String userId = authenticatedUser.getUsername();
        String property = environment.getProperty("image.profile");
        assert property != null;
        String fileName = property + "/" + userId + ".png";

        dropEnabledUpload.addSucceededListener(event -> changeProfilePicture(memoryBuffer, fileName));
        dropEnabledUpload.addFileRejectedListener(event -> showErrorMessage("file needs to be a png-file"));
        dropEnabledUpload.addFailedListener(event -> showErrorMessage("file can not be uploaded to server"));
        return dropEnabledUpload;
    }

    private static void changeProfilePicture(MemoryBuffer memoryBuffer, String fileName) {
        try (FileOutputStream out = new FileOutputStream(fileName)) {

            byte[] original = IOUtils.toByteArray(memoryBuffer.getInputStream());

            InputStream inputStream = new ByteArrayInputStream(original);
            BufferedImage originalImage = ImageIO.read(inputStream);

            int height = originalImage.getHeight();
            int width = originalImage.getWidth();
            Scalr.Mode mode = (height < width) ? Scalr.Mode.FIT_TO_HEIGHT: Scalr.Mode.FIT_TO_WIDTH;

            int size = 64;
            BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, mode,
                    size, size, Scalr.OP_ANTIALIAS);

            BufferedImage cropedImage = Scalr.crop(resizedImage, size, size);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            ImageIO.write(cropedImage, "png", byteArrayOutputStream);

            originalImage.flush();
            resizedImage.flush();
            cropedImage.flush();

            byte[] resized = byteArrayOutputStream.toByteArray();
            out.write(resized);
            MainLayout mainLayout = UI.getCurrent().getChildren().map(e -> (MainLayout) e).toList().get(0);
            mainLayout.updateProfilePicture();
        } catch (IOException e) {
            Notification.show("image can not be written to server");
        }
    }

    private void changeDisplayNameForUser(UserDetails authenticatedUser) {
        String nickname = name.getValue();
        if (nickname.isBlank()) {
            Notification.show("Your display-name may not be blank");
            return;
        }
        displayNameService.setDisplayName(authenticatedUser, nickname);

        userRepository.setUserNickname(nickname, authenticatedUser.getUsername());

        Notification.show("Your display-name has sucessfully changed to: " + nickname);
    }

    private static void showErrorMessage(String text) {
        Notification show = Notification.show(text);
        show.addThemeVariants(NotificationVariant.LUMO_ERROR);
        show.setPosition(Notification.Position.MIDDLE);
    }
}
