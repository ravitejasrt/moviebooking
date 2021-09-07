package com.ticketbooking.api.service.impl;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ticketbooking.api.GlobalProperties;
import com.ticketbooking.api.entity.*;
import com.ticketbooking.api.enums.*;
import com.ticketbooking.api.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.ticketbooking.api.util.DummyBookMyShowException;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Service
public class GenericServiceImpl {
	private static final String DO_NOT_REPLY_DUMMY_BMS_COM = "doNotReply@dummy.bms.com";

	@Autowired
	private TheaterRepository theaterRepo;

	@Autowired
	private MovieRepository movieRepo;

	@Autowired
	private ScreenRepository screenRepo;

	@Autowired
	private SeatMatrixRepository matrixRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UrlServiceImpl urlService;

	@Autowired
	private NotificationRepository notificationRepo;
	
    @Autowired
    private EurekaClient eurekaClient;
    
    @Autowired
    private LoadBalancerClient loadBalancer;


//    private static final String SERVICE_NAME = "payment-service";
    @Value("${spring.application.name}")
    private String SERVICE_NAME;


//    @Value("${booking.timeout}")
//    private int timeout;

//    @Value("${booking.seat.limit}")
//    private String seatlimit;

    @Autowired
    private GlobalProperties appProperties;

    
    private final RestTemplate restTemplate;

    @Autowired
    public GenericServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public List<String> getSupportedCities() {
		return this.theaterRepo.getSupportdCities();
	}

	/**
	 * This service method will return available movies in a city
	 * 
	 * @param city
	 * @return
	 * @throws JsonProcessingException
	 */
	public org.json.JSONArray getAvailableMovies(String city) throws JsonProcessingException {
		this.LOGGER.info("getAvailableMovies() called in generic service with city: " + city);
		List<Movie> availableMovies = this.movieRepo.getAvailableMovies(city);
		this.LOGGER.info("getAvailableMovies() got " + availableMovies.size() + " movies in city: " + city);
		org.json.JSONArray result = new org.json.JSONArray();
		for (Movie movie : availableMovies) {
			ObjectMapper mapper = new ObjectMapper();
			String movieJson = mapper.writeValueAsString(movie);
			JSONObject obj = new JSONObject(movieJson);
			obj.put("updatedOn", movie.getUpdatedOn().toString());
			obj.put("createdOn", movie.getCreatedOn().toString());
			obj.put("activeDateStart", movie.getActiveDateStart().toString());
			obj.put("activeDateEnd", movie.getActiveDateEnd());
			
			result.put(obj);
		}
		this.LOGGER.info("getAvailableMovies() successfully returned the movies in city: " + city);
		return result;
	}

	/**
	 * This service method will return the screen details for a city which are
	 * showing a movie below is the returned format of JSON:
	 * 
	 * { "movieId1": [ { "screens1": true }, { "screens2": true } ] }
	 * 
	 * @param movie
	 * @param city
	 * @return
	 * @throws JsonProcessingException
	 */
	public JSONObject getScreensShowingMovie(String theaterId, String movieId, String city)
			throws JsonProcessingException {
		this.LOGGER.info(
				"getScreensShowingMovie() getting screens which are showing movie : " + movieId + " in city: " + city);
		List<Theater> theatersShowingThisMovie = this.theaterRepo.getScreensShowingMovie(theaterId, city);
		this.LOGGER.info("getScreensShowingMovie() got " + theatersShowingThisMovie.size()
				+ " screens which are showing movie : " + movieId + " in city: " + city);
		JSONObject result = new JSONObject();
		for (Theater theater : theatersShowingThisMovie) {
			List<Screen> screens = this.screenRepo.findScreenByTheaterIdAndMovieId(theater.getTheaterId(), movieId);
			JSONArray screensArray = new JSONArray();
			for (Screen screen : screens) {
				ObjectMapper mapper = new ObjectMapper();
				String screenJsonObj = mapper.writeValueAsString(screen);
				JSONObject obj = new JSONObject(screenJsonObj);
				screensArray.put(obj);
			}
			result.put(movieId, screensArray);

		}
		this.LOGGER.info(
				"getScreensShowingMovie() successfully returned screens for movie : " + movieId + " in city: " + city);
		return result;

	}

	/**
	 * [ { "seat1A": { "availability": true, "type": "royal" } }, { "seat1A": {
	 * "availability": true, "type": "royal" } } ]
	 * 
	 * @param screen
	 * @return
	 * @throws JsonProcessingException
	 */
	public org.json.JSONArray getSeatMatrix(String movieId, String theaterId, String screenStartsAt)
			throws JsonProcessingException {
		this.LOGGER.info("getSeatMatrix() : getting available seats for theater :  " + theaterId + " movieId: "
				+ movieId + " and start time: " + screenStartsAt);
		List<SeatMatrix> seatsMatrix = this.matrixRepository.getSeatMatrixForscreen(movieId, theaterId, screenStartsAt);
		org.json.JSONArray result = new org.json.JSONArray();
		this.LOGGER.info("getSeatMatrix() : result size for  available seats for theater :  " + theaterId + " movieId: "
				+ movieId + " and start time: " + screenStartsAt + " is " + seatsMatrix.size());
		for (SeatMatrix seat : seatsMatrix) {
			ObjectMapper mapper = new ObjectMapper();
			String seatJson = mapper.writeValueAsString(seat);
			JSONObject obj = new JSONObject(seatJson);
			obj.put("createdOn", seat.getCreatedOn().toString());
			obj.put("theaterId", seat.getPrimaryKey().getTheaterId());
			obj.put("movieId", seat.getPrimaryKey().getMovieId());
			obj.put("seatNumber", seat.getPrimaryKey().getSeatNumber());
			obj.put("ShowStartsAt", seat.getPrimaryKey().getScreenStartsAt());
			if (obj.has("primaryKey"))
				obj.remove("primaryKey");
			result.put(obj);
		}
		this.LOGGER.info("getSeatMatrix() : successfully got  available seats for theater :  " + theaterId
				+ " movieId: " + movieId + " and start time: " + screenStartsAt);
		return result;
	}

	/**
	 * This service method will be responsible for below things: 1. Check if the
	 * seats are already booked, if no then book these seats for this user in this
	 * theater for this movie 2. do the payment , dummy payment details 3. if
	 * payment is success then send the notification related to tickets 4. if
	 * payment not success then mark booked seats as unbook , we can also book the
	 * seats only if the payment is success but since there is a dummy payment data
	 * here so I am just booking seats irrespective of payment status 5. return the
	 * booking details to user 6. I am generating a tiny URL and that URL to user so
	 * that user can retrieve the tickets
	 * 
	 * Instead of making whole method synchronized , I have made logic of method
	 * synchronized to improve performance Why block of this method synchronized: so
	 * that not more than 2 threads can access it same time. But for now whole logic
	 * is inside synchronized block
	 * 
	 * @param seatsToBook
	 * @param userName
	 * @return
	 * @throws JsonProcessingException
	 * @throws JSONException
	 */
	
	public JSONObject bookSeats(List<SeatMatrix> seatsToBook, String userName , String theaterId)
			throws JSONException, JsonProcessingException {
		StringBuilder sb = new StringBuilder();
		
		/**
		 * TODO : move some of the logic out side synchronized block to optimize
		 * performance
		 */
		

		
		
		synchronized (seatsToBook) {
			this.LOGGER.info("bookSeats() service for username: " + userName);
			int totalPrice = 0;
			
	        // Seats are blocked on a first-come-first-served basis.
	        
/*	        BlockingQueue<Integer> bqueue
	            = new ArrayBlockingQueue<Integer>(6);

	        
	        // Create 1 object each for producer
	        // and consumer and pass them the common
	        // buffer created above
	        Producer p1 = new Producer(bqueue);
	        Consumer c1 = new Consumer(bqueue);
	  
	        // Create 1 thread each for producer
	        // and consumer and pass them their
	        // respective objects.
	        Thread pThread = new Thread(p1);
	        Thread cThread = new Thread(c1);
	  
	        // Start both threads
	        pThread.start();
	        cThread.start(); */

	        
			/*
			 * Getting value for total no of seats booked
			 * */

			List<Optional<Booking>> bklist=bookingRepository.findByUserNameAndTheaterId(userName, theaterId);
			StringBuffer sb1 =new StringBuffer();
			for(int i=0;i<bklist.size();i++) {
			  sb1.append(bklist.get(i).get().getSeatNumbers());	
			}
			String[] sarr=sb1.toString().split(",");
			int seatsbookedpertheather=sarr.length+1;
			
			
			/*
			 * 
			 * A user can choose up to 6 seats from a cinema hall. 
			 * */
			int seatlt=appProperties.getSeatlimit();
			if(seatsToBook.size()>6 || seatsbookedpertheather>6) {
				throw new DummyBookMyShowException("Not more than "+seatlt+" seats allowed");	
			}
			
			// update seat matrix first
			for (SeatMatrix seat : seatsToBook) {
				Optional<SeatMatrix> seatFromDb = this.matrixRepository.findById(seat.getPrimaryKey());
				SeatMatrix oSeatMatrix = seatFromDb.get();
				if (oSeatMatrix != null && oSeatMatrix.isBooked()) {
					throw new DummyBookMyShowException(
							"The seat number: " + seat.getPrimaryKey().getSeatNumber() + " is already booked");
				}
				oSeatMatrix.setBooked(true);
				totalPrice += oSeatMatrix.getPrice();
				this.matrixRepository.save(oSeatMatrix);
				sb.append(seat.getPrimaryKey().getSeatNumber() + " ,");
			}
			String seatNumbers = sb1.toString();
			seatNumbers = (seatNumbers.endsWith(",")) ? seatNumbers.substring(0, seatNumbers.length() - 2)
					: seatNumbers;
			this.LOGGER.info("bookSeats()  seats booked are  " + seatNumbers);
			this.LOGGER.info("Initiating the payment for this booking....This will be with dummy data.");
			/**
			 * TODO : apply the offer codes if applicable TODO : update the booking table
			 * for the user , this is for booking history
			 */
			SeatMatrix seat = seatsToBook.get(0);
			Booking booking = new Booking();
			booking.setCreatedOn(LocalDateTime.now());
			booking.setUserId(userName);
			booking.setMovieId(seat.getPrimaryKey().getMovieId());
			booking.setTheaterId(seat.getPrimaryKey().getTheaterId());
			booking.setSeatBooked(seatsToBook.size());
			booking.setSeatNumbers(seatNumbers);
			booking.setTotalPrice(totalPrice);
			this.bookingRepository.save(booking);
			this.LOGGER.info("bookSeats()  saved booking history.....");
			/**
			 * payment
			 */
			
	        InstanceInfo service = eurekaClient
	                .getApplication(SERVICE_NAME)
	                .getInstances()
	                .get(0);

	        
	        ServiceInstance serviceInstance=loadBalancer.choose(SERVICE_NAME);
	        System.out.println(serviceInstance.getUri());
	        
	        String hostName = service.getHostName();
	        int port = service.getPort();

	        URI url = URI.create("http://" + hostName + ":" + port + "/payment");

	  //      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
	  		
	        Payment dummyPayment = restTemplate.postForObject(url, booking, Payment.class);
	 //       this.LOGGER.info("bookSeats() response"+response);
	 // 		System.out.println("bookSeats() response"+response);

	  	/*	EmployeeList response = restTemplate.getForObject(
	  			  "http://localhost:8080/employees",
	  			  EmployeeList.class);
	  			List<Employee> employees = response.getEmployees();
	  	*/		
	  			
	//		Payment dummyPayment = doPayment(booking); // as RestTemplate used
			// update payment id in booking
			booking.setPaymentId(dummyPayment.getId());
			booking.setUpdatedOn(LocalDateTime.now());
			this.bookingRepository.save(booking);
			this.LOGGER.info("bookSeats() updated payment id in booking");
		
			if (dummyPayment == null)
				throw new DummyBookMyShowException("Payment failure for user : " + booking.getUserId());

			// send the notification and update the notification id in booking table
			User user = this.userRepository.findUserByUserName(booking.getUserId());
			if (user == null)
				throw new DummyBookMyShowException("There is no user exists with user name: " + booking.getUserId());

			Notification sentNotification = sendNotification(booking, user);
			booking.setNotificationId(sentNotification.getId());
			booking.setUpdatedOn(LocalDateTime.now());
			this.bookingRepository.save(booking);
			this.LOGGER.info("bookSeats() Notification sent , also updated the notification id to booking");

			return makeReturnedData(booking, user);

		}

	}

	/*
	 * Hystrix rollback payment
	 * */
    @SuppressWarnings("unused")
    Optional<PaymentResponse> getDefaultPaymentByCode() {
        LOGGER.info("getDefaultPaymentByCode: ");
        PaymentResponse response = new PaymentResponse();
        return Optional.ofNullable(response);
    }
    
    
    
    
    
    
    
	private JSONObject makeReturnedData(Booking booking, User user) {
		JSONObject result = new JSONObject();
		/**
		 * user details:
		 */
		JSONObject userDetails = new JSONObject();
		userDetails.put("bookedBy", user.getFirstName() + " " + user.getLastName());
		result.put("userDtails", userDetails);

		JSONObject movieDetails = new JSONObject();
		Optional<Movie> movie = this.movieRepo.findById(booking.getMovieId());
		Movie movieFromDb = movie.get();
		if (movieFromDb == null)
			throw new DummyBookMyShowException("There is no movie found with id  " + booking.getMovieId());
		movieDetails.put("movieName", movieFromDb.getName());
		result.put("movieDetails", movieDetails);

		JSONObject theaterDetails = new JSONObject();
		Optional<Theater> theater = this.theaterRepo.findById(booking.getTheaterId());
		Theater theaterFromDb = theater.get();
		if (theaterFromDb == null)
			throw new DummyBookMyShowException("There is no theater found with id  " + booking.getTheaterId());
		theaterDetails.put("theaterName", theaterFromDb.getName());
		theaterDetails.put("theaterAddress", theaterFromDb.getAddress());
		result.put("theaterDetails", theaterDetails);

		JSONObject ticketDetails = new JSONObject();
		ticketDetails.put("ticketNumbers", booking.getSeatNumbers());
		ticketDetails.put("totalPrice", booking.getTotalPrice());
		result.put("ticketDetails", ticketDetails);

		return result;

	}

	private Payment doPayment(Booking booking) {
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
		this.LOGGER.info("bookSeats()  saved dummy payment ...");
		return paymentFromDb;
	}

	private Notification sendNotification(Booking booking, User user) {
		Notification tobeSendNotification = new Notification();
		tobeSendNotification.setBookingId(booking.getBookingId());
		tobeSendNotification.setStatus(Status.SUCCESS);
		tobeSendNotification.setReceiverEmail(user.getEmail());
		tobeSendNotification.setReceiverMobileNo(user.getMobileNumber());
		tobeSendNotification.setReceiverType(NotificationType.EMAIL);
		tobeSendNotification.setSenderEmail(DO_NOT_REPLY_DUMMY_BMS_COM);
		setTinyUrl(tobeSendNotification, booking, booking.getUserId());
		tobeSendNotification.setCreatedOn(LocalDateTime.now());
		tobeSendNotification.setSendTime(LocalDateTime.now());
		Notification sentNotification = this.notificationRepo.save(tobeSendNotification);
		return sentNotification;
	}

	private void setTinyUrl(Notification tobeSendNotification, Booking booking, String userName) {
		String bookingurl = "https://bms.com/?bookingid = 2&username =" + userName + "&totalAmount="
				+ booking.getTotalPrice() + "&totalSeatBoooked=" + booking.getSeatBooked();
		Url url = new Url();
		url.setCreatedOn(LocalDateTime.now());
		url.setOriginalUrl(bookingurl);
		String shorturl = this.urlService.convertToShortUrl(url);
		tobeSendNotification.setTinyUrl(shorturl);
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

