package com.bdi.agent.model;

import javax.persistence.*;
import java.util.*;

@Entity
@Table
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;  //conversation id from Rasa tracker

    @OneToMany(mappedBy="agent")
    private Set<Belief> beliefs = new HashSet<>();

    @OneToMany(mappedBy="agent")
    private Set<Desire> desires = new HashSet<>();


    @OneToOne(mappedBy="agent")
    private Emotion emotion;

//    private Float emotionvalue;

    private Long previousintentionId;

    private Long currentintentionId;

    public String currentSubject;

    public Boolean active;

    public Long currentAction;

    public float score;

    @ElementCollection
    @CollectionTable(name = "log", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "log")
    @OrderColumn(name = "order_idx")
    private List<String> log = new ArrayList<>();

    public Agent() {
    }

    public Agent(String userId) {
        this.userId = userId;
        this.emotion = new Emotion();
    }

    public Long getId() {
        return id;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getUserId() {
        return userId;
    }

    public void setUser(String userId) {
        this.userId = userId;
    }

    public String getCurrentSubject() {
        return currentSubject;
    }

    public void setCurrentSubject(String currentSubject) {
        this.currentSubject = currentSubject;
    }

    public void setDesires(Set<Desire> desires) {
        this.desires = desires;
    }

    public Set<Belief> getBeliefs() {
        return beliefs;
    }

    public void setBeliefs(Set<Belief> beliefs) {
        this.beliefs = beliefs;
    }

    public void setemotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public Float getemotion(){
        return emotion.getValue();
    }

    public Long getpreviousIntention() {
        return previousintentionId;
    }

    public Long getcurrentIntention() {
        return currentintentionId;
    }

    public void setpreviousIntention(Long previousintentionId) {
        this.previousintentionId = previousintentionId;
    }

    public void setcurrentIntention(Long currentintentionId) {
        this.currentintentionId = currentintentionId;
    }

    public List<String> getLog() {
        return log;
    }

    public void addLog(String chat) {
        this.log.add(chat);
    }

    public Long getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(Long currentAction) {
        this.currentAction = currentAction;
    }

    public void setScore(float score) {
        this.score = score;
    }

}
