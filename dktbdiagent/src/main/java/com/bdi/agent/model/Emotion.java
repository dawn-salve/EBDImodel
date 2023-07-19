package com.bdi.agent.model;

import javax.persistence.*;

@Entity
@Table
public class Emotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade =  CascadeType.ALL)
    @JoinColumn(name="agent_id", referencedColumnName = "id")
    private Agent agent;
    private Float value;
    public Emotion(Agent agent, Float value) {
        this.agent = agent;
        this.value = value;
    }
    public Emotion(){
    }
    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }


}
