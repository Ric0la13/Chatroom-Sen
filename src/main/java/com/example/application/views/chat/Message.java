package com.example.application.views.chat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.StreamResource;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message extends Div {


    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM yyyy HH:mm");

    public Message(Environment environment, String userId, String input, Date date, boolean isAnonymous, boolean isImageMessage) {

        Span nameSpan = new Span(userId);

        Component messageBody;
        if (isImageMessage) {
            messageBody = createChatImage(input);
        } else {
            messageBody = new Span(input);
        }
        ((HasStyle) messageBody).addClassName("body");

        Span dateTimeSpan = getDateTimeSpan(date);

        Image profilePicture = isAnonymous ? getAnonymousProfilePicture() : getProfilePicture(environment, userId);

        nameSpan.addClassName("user-id");

        Div message = new Div(nameSpan, messageBody, dateTimeSpan);
        message.addClassName("message");

        add(profilePicture, message);

        addClassName("picandmess");
    }

    public Message(Environment environment, String userId, String displayName, String input, Date date, boolean isImageMessage) {

        Span nameSpan = new Span(displayName);

        Component messageBody;
        if (isImageMessage) {
            messageBody = createChatImage(input);
        } else {
            messageBody = new Span(input);
        }
        ((HasStyle) messageBody).addClassName("body");

        Span dateTimeSpan = getDateTimeSpan(date);
        Image profilePicture = getProfilePicture(environment, userId);

        nameSpan.addClassName("user-id");

        Div message = new Div(nameSpan, messageBody, dateTimeSpan);
        message.addClassName("message");

        add(profilePicture, message);

        addClassName("picandmess");
    }

    private Image createChatImage(String input) {
        Image messageBody = new Image("VAADIN/chatpictures/" + input, "imagine");
        messageBody.getElement().setAttribute("onLoad", """
                parent = event.target.parentElement.parentElement.parentElement.parentElement;
                parent.scrollTo(0, parent.scrollHeight);
                """);
        return messageBody;
    }

    private static Span getDateTimeSpan(Date date) {
        String timeStamp = DATE_FORMAT.format(date);
        Span dateTimeSpan = new Span(timeStamp);
        dateTimeSpan.addClassName("timestamp");
        return dateTimeSpan;
    }

    private static Image getProfilePicture(Environment environment, String userId) {
        StreamResource imageResource = getStreamResource(environment, userId);

        Image profilePicture;
        if (imageResource == null) {
            profilePicture = new Image("public/images/default-avatar.png", "Profile Picture from " + userId);
        } else {
            profilePicture = new Image(imageResource, "Profile Picture from " + userId);
            profilePicture.getElement().setAttribute("onError", "{"
                    + "event.target.src = \"public/images/default-avatar.png\"}");
        }
        profilePicture.addClassName("profile");
        return profilePicture;
    }

    private static StreamResource getStreamResource(Environment environment, String userId) {
        String property = environment.getProperty("image.profile");
        if (property == null) return null;
        String profilePicturePath = property.formatted(userId);

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

    private static Image getAnonymousProfilePicture() {
        Image profilePicture = new Image("public/images/anonymous-avatar.png", "Anonymous Profile Picture");
        profilePicture.addClassName("profile");
        return profilePicture;
    }
}
