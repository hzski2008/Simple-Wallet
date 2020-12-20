package com.example.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.wallet.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}