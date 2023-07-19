package com.bdi.agent.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class Knowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private String attribute;

    @ElementCollection
    @CollectionTable(name = "responses", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "responses")
    private List<String> values = new ArrayList<>();

    public Knowledge() {

    }

    public Knowledge(String subject, String attribute) {
        this.subject = subject;
        this.attribute = attribute;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> responses) {
        this.values = responses;
    }
}
