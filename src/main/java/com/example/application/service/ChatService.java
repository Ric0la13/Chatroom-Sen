package com.example.application.service;

import com.example.application.security.SecurityService;
import com.example.application.views.chat.ChatView;
import com.example.application.views.chat.Message;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Service
public class ChatService {
    private final List<UI> uiList;
    private final SecurityService securityService;
    private final DisplayNameService displayNameService;

    public ChatService(SecurityService securityService, DisplayNameService displayNameService) {
        this.securityService = securityService;
        this.displayNameService = displayNameService;

        uiList = new ArrayList<>();
    }

    public void addUI(UI ui) {
        uiList.add(ui);
    }

    public void postMessage(String value, UI currentUI) {
        uiList.stream()
                .filter(not(UI::isAttached))
                .forEach(this::removeUI);
        UserDetails sendingUser = securityService.getAuthenticatedUser();
        if (sendingUser == null) {
            uiList.forEach(ui -> ui.access(() -> sendMessage(value, currentUI, ui)));
        } else {
            uiList.forEach(ui -> ui.access(() -> sendMessage(value, sendingUser, ui)));
        }
    }

    public void postImageMessage(String fileName, UI currentUI) {
        uiList.stream()
                .filter(not(UI::isAttached))
                .forEach(this::removeUI);
        UserDetails sendingUser = securityService.getAuthenticatedUser();
        if (sendingUser == null) {
            uiList.forEach(ui -> ui.access(() -> sendImageMessage(fileName, currentUI, ui)));
        } else {
            uiList.forEach(ui -> ui.access(() -> sendImageMessage(fileName, sendingUser, ui)));
        }
    }

    private void sendMessage(String value, UI currentUI, UI ui) {
        Message message = createMessage(value, currentUI);

        if (ui.equals(currentUI)) {
            message.addClassName("own");
        }
        ui.getChildren()
                .flatMap(Component::getChildren)
                .filter(ChatView.class::isInstance)
                .findFirst()
                .map(ChatView.class::cast)
                .ifPresent(component ->
                        component.addMessage(message));
        message.scrollIntoView();
    }

    private void sendImageMessage(String fileName, UI currentUI, UI ui) {
        Message message = createImageMessage(fileName, currentUI);

        if (ui.equals(currentUI)) {
            message.addClassName("own");
        }
        ui.getChildren()
                .flatMap(Component::getChildren)
                .filter(ChatView.class::isInstance)
                .findFirst()
                .map(ChatView.class::cast)
                .ifPresent(component ->
                        component.addMessage(message));
        message.scrollIntoView();
    }

    private void sendMessage(String value, UserDetails sendingUser, UI ui) {
        Message message = createMessage(value, sendingUser);

        Optional<ChatView> chatView = ui.getChildren()
                .flatMap(Component::getChildren)
                .filter(ChatView.class::isInstance)
                .findFirst()
                .map(ChatView.class::cast);

        boolean targetAnonymous = chatView.map(ChatView::isAnonymous).orElse(true);
        if (!targetAnonymous && securityService.getAuthenticatedUser().equals(sendingUser)) {
            message.addClassName("own");
        }

        chatView.ifPresent(component ->
                        component.addMessage(message));
        message.scrollIntoView();
    }

    private void sendImageMessage(String value, UserDetails sendingUser, UI ui) {
        Message message = createImageMessage(value, sendingUser);

        Optional<ChatView> chatView = ui.getChildren()
                .flatMap(Component::getChildren)
                .filter(ChatView.class::isInstance)
                .findFirst()
                .map(ChatView.class::cast);

        boolean targetAnonymous = chatView.map(ChatView::isAnonymous).orElse(true);
        if (!targetAnonymous && securityService.getAuthenticatedUser().equals(sendingUser)) {
            message.addClassName("own");
        }

        chatView.ifPresent(component ->
                component.addMessage(message));
        message.scrollIntoView();
    }

    private Message createMessage(String value, UserDetails sendingUser) {
        return displayNameService.getDisplayName(sendingUser)
                .map(displayName -> new Message(sendingUser.getUsername(), displayName, value, false))
                .orElseGet(() -> new Message(sendingUser.getUsername(), value, false, false));
    }

    private Message createImageMessage(String fileName, UserDetails sendingUser) {
        return displayNameService.getDisplayName(sendingUser)
                .map(displayName -> new Message(sendingUser.getUsername(), displayName, fileName, true))
                .orElseGet(() -> new Message(sendingUser.getUsername(), fileName, false, true));
    }

    private Message createMessage(String value, UI ui) {
        return new Message("Anonymous " + ui.getUIId(), value, true, false);
    }

    private Message createImageMessage(String value, UI ui) {
        return new Message("Anonymous " + ui.getUIId(), value, true, true);
    }

    public void removeUI(UI ui) {
        uiList.remove(ui);
    }
}
