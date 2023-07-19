package com.bdi.agent.model;

import javax.persistence.*;

@Entity
@Table
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade =  CascadeType.ALL)
    @JoinColumn(name="desire_id", referencedColumnName = "id")
    private Desire desire;

    private String type;
    private String name;
    private String subject;
    private String attribute;
    private Boolean completed;

    public Action() {

    }

    public Action(Desire desire, String type, String name, String subject, String attribute, Boolean isCompleted) {
        this.desire = desire;
        this.type = type;
        this.name = name;
        this.subject = subject;
        this.attribute = attribute;
        this.completed = isCompleted;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public String getAttribute() {
        return attribute;
    }

    public Boolean getIsCompleted() {
        return completed;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.completed = isCompleted;
    }
}
