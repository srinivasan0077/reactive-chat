package com.veprojects.reactivechat.entities;

public class Room {

    private Long id;
    private String name;
    private User createdBy;

    public Long getId() {
        return id;
    }

    public Room setId(Long id) {
        this.id = id;
        return this;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public Room setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getName() {
        return name;
    }

    public Room setName(String name) {
        this.name = name;
        return this;
    }
}
