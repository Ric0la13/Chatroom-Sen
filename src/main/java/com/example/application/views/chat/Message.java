package com.example.application.views.chat;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message extends Div {


    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM yyyy HH:mm");

    public Message(String userId, String messageText) {
        Span userIdSpan = new Span(userId);
        Span messageTextSpan = new Span(messageText);

        Date currentTime = new Date();
        String timeStamp = DATE_FORMAT.format(currentTime);
        Span dateTimeSpan = new Span(timeStamp);

        userIdSpan.addClassName("user-id");
        messageTextSpan.addClassName("text");
        dateTimeSpan.addClassName("timestamp");

        Div message = new Div(userIdSpan, messageTextSpan, dateTimeSpan);
        message.addClassName("message");

        String profilePicturePath = "VAADIN/profilepictures/" + userId + ".png";

        Image profilePicture = new Image(profilePicturePath, "Profile Picture from " + userId);
        profilePicture.addClassName("profile");

        add(profilePicture, message);

        addClassName("picandmess");
    }
}
