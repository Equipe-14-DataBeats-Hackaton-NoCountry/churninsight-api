package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository;

import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PredictionRepository extends JpaRepository<PredictionHistory, UUID> {
    
    @Query("SELECT COUNT(p) FROM PredictionHistory p WHERE p.churnStatus = 'Vai cancelar'")
    long countChurnPredictions();
}

