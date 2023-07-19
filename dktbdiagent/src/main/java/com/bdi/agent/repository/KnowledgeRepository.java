package com.bdi.agent.repository;

import com.bdi.agent.model.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {

    Knowledge findBySubjectAndAttribute(String subject, String attribute);

}
