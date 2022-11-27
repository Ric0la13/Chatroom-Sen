package com.example.application.views.helloworld;

import com.example.application.security.SecurityService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.io.IOUtils;

import javax.annotation.security.PermitAll;
import java.io.FileOutputStream;
import java.io.IOException;

@PageTitle("Hello World")
@PermitAll
@Route(value = "hello", layout = MainLayout.class)
public class HelloWorldView extends HorizontalLayout {

    private final TextField name;

    public HelloWorldView(SecurityService securityService) {
        name = new TextField("Your name");
        Button sayHello = new Button("Say hello");
        sayHello.addClickListener(e -> Notification.show("Hello " + name.getValue()));
        sayHello.addClickShortcut(Key.ENTER);

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        Upload dropEnabledUpload = new Upload(memoryBuffer);
        dropEnabledUpload.setAcceptedFileTypes("image/png", ".png");

        String username = securityService.getAuthenticatedUser().getUsername();
        String fileName = "src/main/webapp/VAADIN/profilepictures/" + username + ".png";

        dropEnabledUpload.addSucceededListener(event -> {
            try (FileOutputStream out = new FileOutputStream(fileName)) {
                byte[] bytes = IOUtils.toByteArray(memoryBuffer.getInputStream());
                out.write(bytes);
            } catch (IOException e) {
                Notification.show("image can not be written to server");
            }
        });
        dropEnabledUpload.addFileRejectedListener(event -> Notification.show("file needs to be a png-file"));
        dropEnabledUpload.addFailedListener(event -> Notification.show("file can not be uploaded to server"));

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, name, sayHello);

        add(name, sayHello, dropEnabledUpload);
    }

}
