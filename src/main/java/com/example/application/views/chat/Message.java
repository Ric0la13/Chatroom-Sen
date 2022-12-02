package com.example.application.views.chat;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message extends Div {


    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM yyyy HH:mm");

    public Message(String userId, String messageText, boolean isAnonymous) {

        Span nameSpan = new Span(userId);

        Span messageTextSpan = getMessageTextSpan(messageText);
        Span dateTimeSpan = getDateTimeSpan();

        Image profilePicture = isAnonymous ? getAnonymousProfilePicture() : getProfilePicture(userId);

        nameSpan.addClassName("user-id");

        Div message = new Div(nameSpan, messageTextSpan, dateTimeSpan);
        message.addClassName("message");

        add(profilePicture, message);

        addClassName("picandmess");
    }

    public Message(String userId, String displayName, String messageText) {

        Span nameSpan = new Span(displayName);

        Span messageTextSpan = getMessageTextSpan(messageText);
        Span dateTimeSpan = getDateTimeSpan();
        Image profilePicture = getProfilePicture(userId);

        nameSpan.addClassName("user-id");

        Div message = new Div(nameSpan, messageTextSpan, dateTimeSpan);
        message.addClassName("message");

        add(profilePicture, message);

        addClassName("picandmess");
    }

    private static Span getMessageTextSpan(String messageText) {
        Span messageTextSpan = new Span(messageText);
        messageTextSpan.addClassName("text");
        return messageTextSpan;
    }

    private static Span getDateTimeSpan() {
        Date currentTime = new Date();
        String timeStamp = DATE_FORMAT.format(currentTime);
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
