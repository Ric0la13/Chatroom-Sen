package com.example.application.views.chat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message extends Div {


    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM yyyy HH:mm");

    public Message(String userId, String input, Date date, boolean isAnonymous, boolean isImageMessage) {

        Span nameSpan = new Span(userId);

        Component messageBody;
        if (isImageMessage) {
            messageBody = createChatImage(input);
        } else {
            messageBody = new Span(input);
        }
        ((HasStyle) messageBody).addClassName("body");

        Span dateTimeSpan = getDateTimeSpan(date);

        Image profilePicture = isAnonymous ? getAnonymousProfilePicture() : getProfilePicture(userId);

        nameSpan.addClassName("user-id");

        Div message = new Div(nameSpan, messageBody, dateTimeSpan);
        message.addClassName("message");

        add(profilePicture, message);

        addClassName("picandmess");
    }

    public Message(String userId, String displayName, String input, Date date, boolean isImageMessage) {

        Span nameSpan = new Span(displayName);

        Component messageBody;
        if (isImageMessage) {
            messageBody = createChatImage(input);
        } else {
            messageBody = new Span(input);
        }
        ((HasStyle) messageBody).addClassName("body");

        Span dateTimeSpan = getDateTimeSpan(date);
        Image profilePicture = getProfilePicture(userId);

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

    private static Image getProfilePicture(String userId) {
        String profilePicturePath = "VAADIN/profilepictures/" + userId + ".png";

        Image profilePicture = new Image(profilePicturePath, "Profile Picture from " + userId);
        profilePicture.getElement().setAttribute("onError", "{"
                + "event.target.src = \"public/images/default-avatar.png\"}");
        profilePicture.addClassName("profile");
        return profilePicture;
    }

    private static Image getAnonymousProfilePicture() {
        Image profilePicture = new Image("public/images/anonymous-avatar.png", "Anonymous Profile Picture");
        profilePicture.addClassName("profile");
        return profilePicture;
    }
}
