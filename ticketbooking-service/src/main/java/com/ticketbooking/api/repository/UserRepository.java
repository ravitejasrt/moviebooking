package com.ticketbooking.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketbooking.api.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>,CrudRepository<User, Long>
{

	@Query(value = "select * from user where user_name = :userName", nativeQuery = true)
	User findUserByUserName(@Param("userName") String userName);
	
	
	
}
