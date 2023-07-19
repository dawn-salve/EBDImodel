package com.bdi.agent.service;

import com.bdi.agent.model.Action;
import com.bdi.agent.model.Desire;
import com.bdi.agent.repository.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ActionService {

    private final ActionRepository actionRepository;

    @Autowired
    public ActionService(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    public Action getActionById(Long id) {
        return actionRepository.getById(id);
    }

    public void addAction(Action action) {
        actionRepository.save(action);
    }

    List<Action> getActionsByDesireId(Long desireId) {
        return actionRepository.findByDesireId(desireId);
    }

    public Action getUncompletedAction(Long desireId) {
        List<Action> actions = getActionsByDesireId(desireId);
        for (Action action : actions) {
            if (!action.getIsCompleted()) {
                return action;
            }
        }
        return null;
    }

    /**
    * Each desire has an associated set of actions. This method creates actions for each desire.
    *
    * */
    public void addActionsToDesire(Desire desire) {
        if (desire.getName().equals("D1")) {
            addAction(new Action(desire, "inform", "A4","chitchat", "goodbye", false));
        }

        if (desire.getName().equals("D2")) {
            addAction(new Action(desire, "inform", "A1","bullying", "what", false));
            addAction(new Action(desire, "inform", "A2","bullying", "details", false));
            addAction(new Action(desire, "inform", "A3","bullying", "feeling", false));
        }

        if (desire.getName().equals("D3")) {
            addAction(new Action(desire, "inform", "A1","bullying", "what", false));
            addAction(new Action(desire, "inform", "A2","bullying", "details", false));
            addAction(new Action(desire, "inform", "A3","bullying", "feeling", false));
        }

        if (desire.getName().equals("D4")) {
            addAction(new Action(desire, "request", "A5","goal", "howkt", false));
        }

        if (desire.getName().equals("D5")) {
            addAction(new Action(desire, "request", "A6","help", "how", false));
//            addAction(new Action(desire, "request", "A7","help", "severity", false));
            addAction(new Action(desire, "request", "A7","help", "say", false));
        }

    }

}
