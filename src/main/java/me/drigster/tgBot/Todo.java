package me.drigster.tgBot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Todo(Long id, String text, Boolean done) {
    public String toString(){
        return String.format("Id: %s\nText: %s\nIs done: %s", id, text, done);
    }
}