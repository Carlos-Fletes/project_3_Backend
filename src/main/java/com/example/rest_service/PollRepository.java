package com.example.rest_service;

import com.example.rest_service.PollStatus;
import com.example.rest_service.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findByStatus(PollStatus status);
}
