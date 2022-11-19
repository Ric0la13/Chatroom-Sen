package com.example.application.views.chat;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)

public class ChatView extends HorizontalLayout {
}
