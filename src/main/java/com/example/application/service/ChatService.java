package com.example.application.service;

import com.example.application.views.chat.ChatView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    private final List<UI> uiList;

    public ChatService() {
        uiList = new ArrayList<>();
    }

    public void addUI(UI ui) {
        uiList.add(ui);
    }

    public void postMessage(String value) {
        uiList.forEach(ui -> ui.access(() -> {
            Span message = new Span(value);
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
