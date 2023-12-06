package me.drigster.tgBot;

import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Todo(Long id, String text, Boolean isDone) {
    public String toString(){
        String isDoneStr = isDone ? "Yes" : "No";
        String todo = "";
        todo += "<pre>";
        todo += String.format("| %-8s | %-42s\n", "Id", id);
        todo += String.format("| %-8s | %-42s\n", "Is done", isDoneStr);
        todo += String.format("| %-8s | %-42s\n", "Text", text);
        todo += "</pre>";
        return todo;
    }

    public InlineKeyboardMarkup getKeyboard(){
        InlineKeyboardMarkup inlineKeyboard = InlineKeyboardMarkup.builder()
        .keyboardRow(List.of(
            InlineKeyboardButton.builder().text("Mark as done").callbackData("do " + id).build(),
            InlineKeyboardButton.builder().text("Unmark as done").callbackData("undo " + id).build(),
            InlineKeyboardButton.builder().text("Delete").callbackData("delete " + id).build()
        ))
        .keyboardRow(List.of(
            InlineKeyboardButton.builder().text("Get another").callbackData("/get").build(),
            InlineKeyboardButton.builder().text("Get all").callbackData("/getall").build()
        ))
        .build();
        return inlineKeyboard;
    }
}
