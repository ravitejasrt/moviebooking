package com.ticketbooking.api.rest.controller;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketbooking.api.entity.User;
import com.ticketbooking.api.enums.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.api.repository.UserRepository;

import com.ticketbooking.api.util.ResponseParser;

@RestController
@RequestMapping(value = "/v1")
@Component
@Configuration
public class UserController {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern
			.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	@Autowired
	private ResponseParser responseParser;


	@Autowired
	private UserRepository userReposiitory;


	/**
	 * get the user details
	 * @param userName
	 * @return
	 */
	@RequestMapping(value = "/getUserDetails", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getUserDetails(@RequestParam("userName") String userName) {
		this.LOGGER.info(" getUser () with input params:  " + userName);
		try {
			User user = this.userReposiitory.findUserByUserName(userName);
			this.LOGGER.info(" getUser () found the user with input username " + user.toString());
			
			ObjectMapper mapper = new ObjectMapper();
			String jsonUser = mapper.writeValueAsString(user);
			this.LOGGER.info(" getUser () status success! ");
			return new ResponseEntity<>(jsonUser, HttpStatus.OK);

		} catch (Exception ex) {
			this.LOGGER.info(" getUser () Error occured:  " + ex.getMessage());
			return new ResponseEntity<>(this.responseParser.build(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					ex.getMessage(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * SignUp of a user
	 * This API should not be authenticated
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/addUser", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> addUser(@RequestBody User user) {
		try {
			this.LOGGER.info("addUser() called with input:  " + user.toString());
			User newUser = user;
			validateInput(newUser);
			String userName = getUserName(user).replaceAll("\\s", "");
			newUser.setUserName(userName);
			newUser.setCreatedOn(LocalDateTime.now());
			this.LOGGER.info("addUser() setting username as   " + newUser.getUserName());
			this.userReposiitory.save(newUser);
			this.LOGGER.info("successfully saved user object " + newUser.toString());
			return new ResponseEntity<>(this.responseParser.build(HttpStatus.CREATED.value(), "Successfully saved user with user name: "+newUser.getUserName(),
					"Successfully saved user with user name: "+newUser.getUserName()), HttpStatus.CREATED);
		} catch (IllegalArgumentException ex) {
			this.LOGGER.error("Error Saving user object " + ex.getMessage());
			return new ResponseEntity<>(
					this.responseParser.build(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), ex.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			this.LOGGER.error("Error Saving user object " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(
					this.responseParser.build(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	/**
	 * 
	 * @param user
	 */
	private void validateInput(User user) {
		try {
			Assert.notNull(user, "User object must not be null");
			Assert.hasLength(user.getFirstName(), "User first name must not be null or empty");
			Assert.hasLength(user.getLastName(), "User last name must not be null or empty");
			Assert.hasLength(user.getMobileNumber(), "User mobile number must not be empty");
			Assert.hasLength(user.getUserType().toString(), "User type must not be null or empty");
			Assert.hasLength(user.getEmail(), "Email must not be null or empty");
			Assert.hasLength(user.getAuthentication(), "password must not be null or empty");
			Assert.isTrue(user.getMobileNumber().length() == 10, "Invalid mobile number");
			switch (user.getUserType().toString().toUpperCase()) {
			case "ADMIN":
				user.setUserType(UserType.ADMIN);
				this.LOGGER.debug("validateInput(user), User type found as ADMIN");
				break;
			case "NORMAL":
				user.setUserType(UserType.NORMAL);
				this.LOGGER.debug("validateInput(user), User type found as NORMAL");
				break;
			default:
				Assert.isTrue(false, "Only values allowd for user type is : Admin or Normal");
			}
			Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(user.getEmail());
			Assert.isTrue(matcher.find(), "Invalid email id");
			Pattern p = Pattern.compile("(0/91)?[7-9][0-9]{9}");
			Matcher m = p.matcher(user.getMobileNumber());
			Assert.isTrue((m.find() && m.group().equals(user.getMobileNumber())), "Invalid mobile number");
		} catch (IllegalArgumentException e) {
			this.LOGGER.error("input error ", e.getMessage());
			throw new IllegalArgumentException(e.getMessage());
		}

	}

	/**
	 * @param user
	 * @return
	 */
	private String getUserName(User user) {
		return (user.getLastName() + user.getFirstName() + user.getMobileNumber());

	}

}
