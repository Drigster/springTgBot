package me.drigster.tgBot.bot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.drigster.tgBot.Todo;

@Component
public class TestBot extends TelegramLongPollingBot {

    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String GET = "/get";
    private static final String GET_ALL = "/getall";
    private static final String CREATE = "/create";
    private static final String DO = "/do";
    private static final String UNDO = "/undo";
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
            case START -> {
                lastCommand = START;

                startCommand(chatId);
            }
            case HELP -> {
                lastCommand = HELP;

                helpCommand(chatId);
            }
            case GET_ALL -> {
                lastCommand = GET_ALL;

                getAllCommand(chatId);
            }
            case GET -> {
                lastCommand = GET;

                sendMsg(chatId, "Plz type todo id:");
            }
            case CREATE -> {
                lastCommand = CREATE;

                sendMsg(chatId, "Plz type todo text:");
            }
            case DO -> {
                lastCommand = DO;

                sendMsg(chatId, "Plz type todo id:");
            }
            case UNDO -> {
                lastCommand = UNDO;

                sendMsg(chatId, "Plz type todo id:");
            }
            case DELETE -> {
                lastCommand = DELETE;

                sendMsg(chatId, "Plz type todo id:");
            }
            case DELETE_ALL -> {
                lastCommand = DELETE_ALL;

                deleteAllCommand(chatId);
            }
            default -> {
                switch (lastCommand) {
                    case GET -> {
                        getCommand(chatId, message);
                    }
                    case CREATE -> {
                        createCommand(chatId, message);
                    }
                    case DELETE -> {
                        deleteCommand(chatId, message);
                    }
                    case DO -> {
                        doCommand(chatId, message);
                    }
                    case UNDO -> {
                        undoCommand(chatId, message);
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
                /do - to mark todo done
                /undo - to unmark todo done
                /delete - to delete todo
                /deleteall - to delete all todos
            """;
        sendMsg(chatId, text);
    }

    private void getAllCommand(String chatId) {
        Todo[] todos;
        try {
            todos = restTemplate.getForObject(
                "http://localhost:8080/todos", Todo[].class);
        } catch (Exception e) {
            System.out.println(e);
            sendMsg(chatId, "Internal error");
            return;
        }
        
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

            Todo todo;
            try {
                todo = restTemplate.getForObject(
                    "http://localhost:8080/todo/" + message, Todo.class);
            } catch (NotFound e) {
                sendMsg(chatId, "Todo not found");
                return;
            } catch (Exception e) {
                System.out.println(e);
                sendMsg(chatId, "Internal error");
                return;
            }
            

            if (todo == null) {
                sendMsg(chatId, "Todo not found");
                return;
            }

            sendMsg(chatId, todo.toString());
        }
    }

    private void createCommand(String chatId, String message) {
        if(!lastCommand.equals(CREATE)){
            lastCommand = CREATE;

            sendMsg(chatId, "Plz type todo text:");
        }
        else {
            lastCommand = "";

            if (message.length() < 1) {
                sendMsg(chatId, "Text can't be empty");
                return;
            }
            if (message.length() > 255) {
                sendMsg(chatId, "Text must be shorter chan 255 symbols");
                return;
            }

            Map<String, String> todoMap = new HashMap<String, String>();
            todoMap.put("text", message);

            ObjectMapper objectMapper = new ObjectMapper();
            String todoJson;
            try {
                todoJson = objectMapper.writeValueAsString(todoMap);
            } catch (Exception e) {
                System.out.println(e);
                sendMsg(chatId, "Internal error");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<String>(todoJson, headers);

            Todo todo;
            try {
                todo = restTemplate.postForObject("http://localhost:8080/todos", request, Todo.class);
            } catch (Exception e) {
            System.out.println(e);
                sendMsg(chatId, "Internal error");
                return;
            }
            
            if (todo == null) {
                sendMsg(chatId, "Todo was not created");
                return;
            }

            sendMsg(chatId, "Todo created: ");
            sendMsg(chatId, todo.toString());
        }
    }

    private void deleteCommand(String chatId, String message) {
        if(!lastCommand.equals(DELETE)){
            lastCommand = DELETE;

            sendMsg(chatId, "Plz type todo id:");
        }
        else {
            lastCommand = "";

            if (!isNumeric(message)) {
                sendMsg(chatId, "Id must be number");
                return;
            }

            try {
                restTemplate.delete("http://localhost:8080/todo/" + message);
            } catch (Exception e) {
                System.out.println(e);
                sendMsg(chatId, "Internal error");
                return;
            }

            sendMsg(chatId, "Todo deleted");
        }
    }

    private void deleteAllCommand(String chatId) {
        try {
            restTemplate.delete("http://localhost:8080/todo/all");
        } catch (Exception e) {
            System.out.println(e);
            sendMsg(chatId, "Internal error");
            return;
        }

        sendMsg(chatId, "Todos deleted");
    }

    private void doCommand(String chatId, String message) {
        if(!lastCommand.equals(DO)){
            lastCommand = DO;

            sendMsg(chatId, "Plz type todo id:");
        }
        else {
            lastCommand = "";

            if (!isNumeric(message)) {
                sendMsg(chatId, "Id must be number");
                return;
            }

            try {
                restTemplate.getForEntity("http://localhost:8080/todo/" + message + "/done", null);
            } catch (Exception e) {
                System.out.println(e);
                sendMsg(chatId, "Internal error");
                return;
            }

            sendMsg(chatId, "Todo marked as done");
        }
    }

    private void undoCommand(String chatId, String message) {
        if(!lastCommand.equals(UNDO)){
            lastCommand = UNDO;

            sendMsg(chatId, "Plz type todo id:");
        }
        else {
            lastCommand = "";

            if (!isNumeric(message)) {
                sendMsg(chatId, "Id must be number");
                return;
            }

            try {
                restTemplate.getForEntity("http://localhost:8080/todo/" + message + "/undo", null);
            } catch (Exception e) {
                System.out.println(e);
                sendMsg(chatId, "Internal error");
                return;
            }

            sendMsg(chatId, "Todo marked as undone");
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
            String text;
            if(todo.text().length() > 31) {
                text = todo.text().substring(0, 28) + "...";
            }
            else {
                text = todo.text();
            }
            table += String.format("| %-4s | %-31s | %-5s |\n", todo.id(), text, todo.isDone());
        }
        return table;
    }
}