package com.veprojects.reactivechat.entities;

public class Response {

    private String message;
    private Object content;

    public Response(String message,Object content){
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
