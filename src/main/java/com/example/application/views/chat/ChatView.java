package com.example.application.views.chat;

import com.example.application.security.SecurityService;
import com.example.application.service.ChatException;
import com.example.application.service.ChatService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import elemental.json.Json;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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

        if (!isAnonymous) {
            fillChatContentWithOldMessages();
        }

        TextField inputField = new TextField();
        inputField.setWidthFull();
        Button sendButton = new Button(VaadinIcon.PAPERPLANE_O.create(), buttonClickEvent -> {
            String value = inputField.getValue();
            if (value.isBlank()) {
                return;
            }
            try {
                chatService.postMessage(value, UI.getCurrent());
                inputField.clear();
            } catch (ChatException e) {
                showErrorMessage("Message can't be sent.");
            }
        });
        inputField.addKeyDownListener(Key.ENTER, keyDownEvent -> sendButton.click());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Upload upload = getUpload();

        HorizontalLayout sendContainer = new HorizontalLayout(inputField, upload, sendButton);
        sendContainer.setWidthFull();
        add(scroller, sendContainer);
        setSizeFull();
    }

    private void fillChatContentWithOldMessages() {
        List<Message> messages = chatService.getAllOldMessages();
        messages.forEach(chatContent::add);

        if (!messages.isEmpty()) {
            messages.get(messages.size() - 1).scrollIntoView();
        }
    }

    private Upload getUpload() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();

        Upload upload = new Upload(memoryBuffer);
        upload.setDropAllowed(false);

        upload.setAcceptedFileTypes("image/png", ".png", "image/jpeg", ".jpeg", ".jpg", "image/gif", ".gif");

        Button uploadButton = new Button(VaadinIcon.FILE_PICTURE.create());
        upload.setUploadButton(uploadButton);
        upload.setMaxFileSize(50_000_000);

        upload.addStartedListener(ChatView::removeFileListFromUpload);
        upload.addFileRejectedListener(ChatView::removeFileListFromUpload);

        upload.addFileRejectedListener(event -> showErrorMessage("Rejected: " + event.getErrorMessage()));
        upload.addFailedListener(event -> showErrorMessage("Failed to process image."));

        upload.addSucceededListener(event -> {
            String fileName = saveImageOnServer(memoryBuffer);
            chatService.postImageMessage(fileName, UI.getCurrent());
        });

        return upload;
    }

    private static void showErrorMessage(String text) {
        Notification show = Notification.show(text);
        show.addThemeVariants(NotificationVariant.LUMO_ERROR);
        show.setPosition(Notification.Position.MIDDLE);
    }

    private static String saveImageOnServer(MemoryBuffer memoryBuffer) {

        String extension = FilenameUtils.getExtension(memoryBuffer.getFileName());

        String name = System.currentTimeMillis() + "." + extension;
        String fileName = "src/main/webapp/VAADIN/chatpictures/" + name;
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            byte[] bytes = IOUtils.toByteArray(memoryBuffer.getInputStream());
            out.write(bytes);
        } catch (IOException e) {
            Notification.show("image can not be written to server");
        }
        return name;
    }

    private static void removeFileListFromUpload(ComponentEvent<Upload> event) {
        Upload upload = event.getSource();
        upload.getElement().setPropertyJson("files", Json.createArray());
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
