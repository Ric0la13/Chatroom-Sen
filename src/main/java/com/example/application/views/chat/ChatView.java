package com.example.application.views.chat;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)

public class ChatView extends VerticalLayout {

    public ChatView() {
        Scroller chatContent = new Scroller();
        chatContent.setSizeFull();

        TextField inputField = new TextField();
        inputField.setWidthFull();
        Button sendButton = new Button(VaadinIcon.PAPERPLANE_O.create());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout sendContainer = new HorizontalLayout(inputField, sendButton);
        sendContainer.setWidthFull();

        add(chatContent, sendContainer);
        setSizeFull();
    }
}
