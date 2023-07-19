package com.bdi.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Perception {

    private String type;
    private String subject;
    private String attribute;
    private String text;

    public Perception() {

    }

    public Perception(@JsonProperty("type") String type, @JsonProperty("subject") String subject, @JsonProperty("attribute") String attribute,  @JsonProperty("text") String text) {
        this.type = type;
        this.subject = subject;
        this.attribute = attribute;
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getText() {
        return text;
    }

}
