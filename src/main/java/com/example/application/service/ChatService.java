package com.example.application.service;

import com.example.application.model.MessageModel;
import com.example.application.repository.MessageRepository;
import com.example.application.security.SecurityService;
import com.example.application.views.chat.ChatView;
import com.example.application.views.chat.Message;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.function.Predicate.not;

@Service
public class ChatService {
    private final List<UI> uiList;
    private final SecurityService securityService;
    private final DisplayNameService displayNameService;
    private final MessageRepository messageRepository;

    public ChatService(MessageRepository messageRepository,
                       SecurityService securityService, DisplayNameService displayNameService) {
        this.messageRepository = messageRepository;
        this.securityService = securityService;
        this.displayNameService = displayNameService;

        uiList = new ArrayList<>();
    }

    public void addUI(UI ui) {
        uiList.add(ui);
    }

    public void postMessage(String value, UI currentUI) throws ChatException {
        uiList.stream()
                .filter(not(UI::isAttached))
                .forEach(this::removeUI);
        UserDetails sendingUser = securityService.getAuthenticatedUser();
        Date date = new Date();
        if (sendingUser == null) {
            uiList.forEach(ui -> ui.access(() -> sendMessage(value, currentUI, ui, date)));
        } else {

            MessageModel messageModel = new MessageModel();
            messageModel.setBody(value);
            messageModel.setTimestamp(new Date().toInstant());
            messageModel.setImage(false);

            String displayName = displayNameService
                    .getDisplayName(sendingUser)
                    .orElseGet(sendingUser::getUsername);
            messageModel.setTitle(displayName);
            messageModel.setUserId(sendingUser.getUsername());

            try {
                messageRepository.save(messageModel);
                uiList.forEach(ui -> ui.access(() -> sendMessage(value, sendingUser, ui, date)));
            } catch (Exception e) {
                throw new ChatException();
            }
        }
    }

    public void postImageMessage(String fileName, UI currentUI) {
        uiList.stream()
                .filter(not(UI::isAttached))
                .forEach(this::removeUI);
        UserDetails sendingUser = securityService.getAuthenticatedUser();
        Date date = new Date();
        if (sendingUser == null) {
            uiList.forEach(ui -> ui.access(() -> sendImageMessage(fileName, currentUI, ui, date)));
        } else {
            MessageModel messageModel = new MessageModel();
            messageModel.setBody(fileName);
            messageModel.setTimestamp(new Date().toInstant());
            messageModel.setImage(true);

            String displayName = displayNameService
                    .getDisplayName(sendingUser)
                    .orElseGet(sendingUser::getUsername);
            messageModel.setTitle(displayName);
            messageModel.setUserId(sendingUser.getUsername());
            messageRepository.save(messageModel);

            uiList.forEach(ui -> ui.access(() -> sendImageMessage(fileName, sendingUser, ui, date)));
        }
    }

    private void sendMessage(String value, UI currentUI, UI ui, Date date) {
        Message message = createMessage(value, currentUI, date);

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

    private void sendImageMessage(String fileName, UI currentUI, UI ui, Date date) {
        Message message = createImageMessage(fileName, currentUI, date);

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

    private void sendMessage(String value, UserDetails sendingUser, UI ui, Date date) {
        Message message = createMessage(value, sendingUser, date);

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

    private void sendImageMessage(String value, UserDetails sendingUser, UI ui, Date date) {
        Message message = createImageMessage(value, sendingUser, date);

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

    private Message createMessage(String value, UserDetails sendingUser, Date date) {
        return displayNameService.getDisplayName(sendingUser)
                .map(displayName -> new Message(sendingUser.getUsername(), displayName, value, date, false))
                .orElseGet(() -> new Message(sendingUser.getUsername(), value, date, false, false));
    }

    private Message createImageMessage(String fileName, UserDetails sendingUser, Date date) {
        return displayNameService.getDisplayName(sendingUser)
                .map(displayName -> new Message(sendingUser.getUsername(), displayName, fileName, date, true))
                .orElseGet(() -> new Message(sendingUser.getUsername(), fileName, date, false, true));
    }

    private Message createMessage(String value, UI ui, Date date) {
        return new Message("Anonymous " + ui.getUIId(), value, date, true, false);
    }

    private Message createImageMessage(String value, UI ui, Date date) {
        return new Message("Anonymous " + ui.getUIId(), value, date, true, true);
    }

    public void removeUI(UI ui) {
        uiList.remove(ui);
    }

    public List<Message> getAllOldMessages() {
        Iterable<MessageModel> messageModelList = messageRepository.findAll();
        return StreamSupport.stream(messageModelList.spliterator(), false)
                .map(this::createMessage)
                .toList();
    }

    private Message createMessage(MessageModel model) {
        Instant timestamp = model.getTimestamp();
        Date date = Date.from(timestamp);
        Message message = new Message(model.getUserId(), model.getTitle(), model.getBody(), date, model.isImage());
        if (securityService.getAuthenticatedUser().getUsername().equals(model.getUserId())) {
            message.addClassName("own");
        }
        return message;
    }
}
