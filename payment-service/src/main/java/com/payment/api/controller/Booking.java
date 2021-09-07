package com.payment.api.controller;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


public class Booking implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long bookingId;
	
	private String userId;
	
	private String theaterId;
	
	private UUID paymentId;
	
	private String movieId;
	
	private UUID notificationId;
	
	private int totalPrice;
	
	private LocalDateTime createdOn = LocalDateTime.now();
	
	private LocalDateTime updatedOn;
	
	private LocalDateTime deleted;
	
	private int seatBooked;
	
	private String seatNumbers;

	public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTheaterId() {
		return theaterId;
	}

	public void setTheaterId(String theaterId) {
		this.theaterId = theaterId;
	}

	public UUID getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(UUID paymentId) {
		this.paymentId = paymentId;
	}

	public String getMovieId() {
		return movieId;
	}

	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}

	public UUID getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(UUID notificationId) {
		this.notificationId = notificationId;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public LocalDateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(LocalDateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	public LocalDateTime getDeleted() {
		return deleted;
	}

	public void setDeleted(LocalDateTime deleted) {
		this.deleted = deleted;
	}

	public int getSeatBooked() {
		return seatBooked;
	}

	public void setSeatBooked(int seatBooked) {
		this.seatBooked = seatBooked;
	}

	public String getSeatNumbers() {
		return seatNumbers;
	}

	public void setSeatNumbers(String seatNumbers) {
		this.seatNumbers = seatNumbers;
	}

	@Override
	public String toString() {
		return "Booking [bookingId=" + bookingId + ", userId=" + userId + ", theaterId=" + theaterId + ", paymentId="
				+ paymentId + ", movieId=" + movieId + ", notificationId=" + notificationId + ", totalPrice="
				+ totalPrice + ", createdOn=" + createdOn + ", updatedOn=" + updatedOn + ", deleted=" + deleted
				+ ", seatBooked=" + seatBooked + ", seatNumbers=" + seatNumbers + "]";
	}

	
}
