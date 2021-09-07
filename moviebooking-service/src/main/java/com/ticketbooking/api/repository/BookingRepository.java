package com.ticketbooking.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import com.ticketbooking.api.entity.Booking;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Async
	public List<Optional<Booking>> findByUserNameAndTheaterId(String userName,String theaterId);

}
