package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredictionHistoryRepository extends JpaRepository<PredictionHistoryEntity, String> {
}