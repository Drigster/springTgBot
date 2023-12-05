package me.drigster.tgBot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import me.drigster.tgBot.Todo;

@Component
public class TestBot extends TelegramLongPollingBot {

    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String GET = "/get";
    private static final String GET_ALL = "/getall";
    private static final String CREATE = "/create";
    private static final String MODIFY = "/modify";
    private static final String DELETE = "/delete";
    private static final String DELETE_ALL = "/deleteall";

    private String lastCommand = "";

    @Autowired
    private RestTemplate restTemplate;

    public TestBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String message = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        switch (message) {
            case START -> startCommand(chatId);
            case HELP -> helpCommand(chatId);
            case GET_ALL -> getAllCommand(chatId);
            case GET -> {
                lastCommand = GET;

                sendMsg(chatId, "Plz type todo id:");
            }
            default -> {
                switch (lastCommand) {
                    case GET -> {
                        getCommand(chatId, message);
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "TestBot";
    }

    private void startCommand(String chatId) {
        String text = """
                Welcome to TodoBot!
            """;
        sendMsg(chatId, text);
    }

    private void helpCommand(String chatId) {
        String text = """
            Available commands:
                /help - to get help
                /get - to get todo
                /getall - to get all todos
                /create - to create todo
                /modify - to modify todo
                /delete - to delete todo
                /deleteall - to delete all todos
            """;
        sendMsg(chatId, text);
    }

    private void getAllCommand(String chatId) {
        Todo[] todos = restTemplate.getForObject(
            "http://localhost:8080/todos", Todo[].class);
        
        if (todos == null) {
            sendMsg(chatId, "Todos not found");
            return;
        }

        sendMsg(chatId, "<pre>" + generateTable(todos) + "</pre>");
    }

    private void getCommand(String chatId, String message) {
        if(!lastCommand.equals(GET)){
            lastCommand = GET;

            sendMsg(chatId, "Plz type todo id:");
        }
        else {
            lastCommand = "";

            if (!isNumeric(message)) {
                sendMsg(chatId, "Id must be number");
                return;
            }

            Todo todo = restTemplate.getForObject(
                "http://localhost:8080/todo/" + message, Todo.class);
            
            if (todo == null) {
                sendMsg(chatId, "Todo not found");
                return;
            }

            sendMsg(chatId, todo.toString());
        }
    }

    private void sendMsg(String chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        sendMessage.enableHtml(true);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error while sending message: " + e.getMessage());
        }
    }

    public static boolean isNumeric(String str) { 
        try {  
          Double.parseDouble(str);  
          return true;
        } catch(NumberFormatException e){  
          return false;  
        }  
    }
    
    public String generateTable(Todo[] todos) {
        String table = "";
        table += String.format("| %-4s | %-31s | %-5s |\n", "Id", "Text", "Done");
        table += String.format("+ %-4s + %-31s + %-5s +\n", "", "", "").replace(' ', '-');
        for (Todo todo: todos) {
            table += String.format("| %-4s | %-31s | %-5s |\n", todo.id(), todo.text(), todo.done());
        }
        return table;
    }
}