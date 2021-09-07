package com.ticketbooking.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketbooking.api.entity.Notification;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>{

}
