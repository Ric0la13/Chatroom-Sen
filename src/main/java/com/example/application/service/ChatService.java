package com.example.application.service;

import com.example.application.views.chat.ChatView;
import com.example.application.views.chat.Message;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.function.Predicate.not;

@Service
public class ChatService {
    private final List<UI> uiList;

    public ChatService() {
        uiList = new ArrayList<>();
    }

    public void addUI(UI ui) {
        uiList.add(ui);
    }

    public void postMessage(String value, UI currentUI) {
        uiList.stream()
                .filter(not(UI::isAttached))
                .forEach(this::removeUI);
        uiList.forEach(ui -> ui.access(() -> {
            String userId = String.valueOf(currentUI.getUIId());
            Message message = new Message(userId, value);
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
        }));
    }

    public void removeUI(UI ui) {
        uiList.remove(ui);
    }
}
