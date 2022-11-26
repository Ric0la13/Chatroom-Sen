package com.example.application.views.chat;

import com.example.application.service.ChatService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
public class ChatView extends VerticalLayout implements BeforeLeaveObserver {

    private final ChatService chatService;
    private final VerticalLayout chatContent;

    public ChatView(ChatService chatService) {
        this.chatService = chatService;

        chatContent = new VerticalLayout();
        chatService.addUI(UI.getCurrent());
        Scroller scroller = new Scroller(chatContent);
        scroller.setSizeFull();

        TextField inputField = new TextField();
        inputField.setWidthFull();
        Button sendButton = new Button(VaadinIcon.PAPERPLANE_O.create(), buttonClickEvent -> {
            String value = inputField.getValue();
            if (value.isBlank()) {
                return;
            }
            chatService.postMessage(value, UI.getCurrent());
            inputField.clear();
        });
        inputField.addKeyDownListener(Key.ENTER, keyDownEvent -> sendButton.click());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout sendContainer = new HorizontalLayout(inputField, sendButton);
        sendContainer.setWidthFull();
        add(scroller, sendContainer);
        setSizeFull();
    }

    public void addMessage(Component message) {
        chatContent.add(message);
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        chatService.removeUI(UI.getCurrent());
    }
}
