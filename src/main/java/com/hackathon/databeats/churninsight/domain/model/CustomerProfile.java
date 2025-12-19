package com.hackathon.databeats.churninsight.domain.model;

public record CustomerProfile(
        String gender,
        Integer age,
        String country,
        String subscriptionType,
        Double listeningTime,
        Integer songsPlayedPerDay,
        Double skipRate,
        String deviceType,
        Boolean offlineListening
) {}