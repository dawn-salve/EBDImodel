package com.bdi.agent.repository;

import com.bdi.agent.model.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    Emotion findByAgentId(Long agentId);
}
