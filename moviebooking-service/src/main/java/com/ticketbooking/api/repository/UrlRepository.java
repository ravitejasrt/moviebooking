package com.ticketbooking.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketbooking.api.entity.Url;


@Repository
public interface UrlRepository extends JpaRepository<Url, Long>{

}
