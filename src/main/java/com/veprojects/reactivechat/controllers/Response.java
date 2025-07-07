package com.veprojects.reactivechat.controllers;

public class Response {

    private String message;
    private Object content;

    public Response(String message,String content){
        this.message=message;
        this.content=content;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
