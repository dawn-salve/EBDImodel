package com.bdi.agent.api;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Perception;
import com.bdi.agent.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AgentController {

    private final AgentService agentService;

    @Autowired
    public AgentController(AgentService agentService) {
        this.agentService = agentService;

    }

    @RequestMapping(path ="/agents", method = RequestMethod.GET)
    public ResponseEntity<List<Agent>> get() {
        List<Agent> agents = agentService.getAll();

        return new ResponseEntity<List<Agent>>(agents, HttpStatus.OK);
    }

    @RequestMapping(path ="/agent/{userId}", method = RequestMethod.POST)
    public ResponseEntity<String> addPerception(@PathVariable("userId") String userId, @RequestBody Perception perception) {
        System.out.println("%n----------Start identifying response entity--------------%n");

        if (!agentService.containsUserId(userId)) {
            agentService.createAgent(userId);
        }

        Agent agent = agentService.getByUserId(userId);

        if (!agent.isActive()) {
            return new ResponseEntity<>("Agent is inactive!", HttpStatus.OK);
        }

        String response = agentService.reason(agent, perception);

        if (response == null) {
            System.err.println();
            return new ResponseEntity<>("No response", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @RequestMapping(path = "/report/{userId}", method = RequestMethod.GET)
    public ResponseEntity<String> getReport(@PathVariable("userId") String userId) {
        System.out.println("get BDI report for user :" + userId);
        String response;

        if (!agentService.containsUserId(userId)) {
            return new ResponseEntity<>("No user id contained", HttpStatus.NOT_FOUND);
        }

        Agent agent = agentService.getByUserId(userId);
        response = agentService.getReport(agent);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
