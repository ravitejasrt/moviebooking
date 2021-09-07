package com.ticketbooking.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketbooking.api.entity.Payment;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>{

}
