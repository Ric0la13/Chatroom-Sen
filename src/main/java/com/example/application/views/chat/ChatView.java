package com.example.application.views.chat;

import com.example.application.security.SecurityService;
import com.example.application.service.ChatService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Chat")
@AnonymousAllowed
@Route(value = "chat", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class ChatView extends VerticalLayout implements BeforeLeaveObserver {

    private final ChatService chatService;
    private final VerticalLayout chatContent;

    private final boolean isAnonymous;

    public ChatView(ChatService chatService, SecurityService securityService) {
        this.chatService = chatService;

        isAnonymous = securityService.getAuthenticatedUser() == null;

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

    public void addMessage(Message message) {
        chatContent.add(message);
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        chatService.removeUI(UI.getCurrent());
    }
}
