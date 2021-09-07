package com.payment.api.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/v1")
public class PaymentController {

    @Value("${eureka.instance.instanceId}")
    private String instanceId;

/*    
    private final RestTemplate restTemplate;

    @Autowired
    public PaymentController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
 */  
    
	@Autowired
	private PaymentRepository paymentRepository;

	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
    
	/*
	 * * A user has to pay for the seats within 2 minutes. 
	 * */
    
	@HystrixCommand(fallbackMethod = "getDefaultPaymentByCode"
			,
		    commandProperties = {
		       @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1200"),
		       @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value="60")
		    }
		)
    @GetMapping("/payment")
    public Payment doPayment(Booking booking) {
		Payment dummyPayment = new Payment();
		dummyPayment.setBookingId(booking.getBookingId());
		dummyPayment.setAmount(booking.getTotalPrice());
		dummyPayment.setStatus(Status.SUCCESS);
		dummyPayment.setCreatedOn(LocalDateTime.now());
		dummyPayment.setMethod(PaymentMethod.CREDITCARD);
		dummyPayment.setSourceDetails("HDFC CC NO: 123456789123");
		/**
		 * TODO : if payment is not success then un-book the seats
		 */

		Payment paymentFromDb = this.paymentRepository.save(dummyPayment);
		this.log.info("bookSeats()  saved dummy payment ...");
		return paymentFromDb;

    }

    /*
    @GetMapping("/payment")
    public Payment doPayment(Booking booking) {

    	
    	log.info("test: ");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "I'm " + instanceId;
    }
*/
    
    @SuppressWarnings("unused")
    Optional<PaymentResponse> getDefaultPaymentByCode() {
        log.info("getDefaultPaymentByCode: ");
        PaymentResponse response = new PaymentResponse();
        return Optional.ofNullable(response);
    } 

}


@Data
class PaymentResponse {
	
   @Getter @Setter 
   private UUID id;	

   @Getter @Setter 
   private Status status;
	
   @Getter @Setter
   private int amount;
	
   @Getter @Setter 
   private PaymentMethod method;
	
	 @Getter @Setter 
	private String sourceDetails;
	
	 @Getter @Setter 
	private Long bookingId;
	
	 @Getter @Setter 
	private LocalDateTime createdOn = LocalDateTime.now();
	
	 @Getter @Setter 
	private LocalDateTime updatedOn;
	
	 @Getter @Setter
	private LocalDateTime deleted;

}


