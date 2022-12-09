package com.parkit.parkingsystem.model;

import static java.time.ZoneId.systemDefault;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

public class Ticket {

    private static final double DISCOUNT = 0.95;

    private int id;

    private ParkingSpot parkingSpot;

    private String vehicleRegNumber;

    private double price;

    private Date inTime;

    private Date outTime;

    private boolean isRecurringUser;

    public Ticket() {
        this.inTime = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getInTime() {
        return inTime;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    public Date getOutTime() {
        return outTime;
    }

    public void setOutTime(Date outTime) {
        this.outTime = outTime;
    }

    public boolean isRecurringUser() {
        return isRecurringUser;
    }

    public void setRecurringUser(boolean isRecurringUser) {
        this.isRecurringUser = isRecurringUser;
    }

    public void computePrice(double ratePerHour) {
        final Duration duration = calculateDuration();
        if (isLessThan30MinutesParking(duration)) {
            this.setPrice(0);
        } else {
            final double standardPrice = duration.toMinutes() * ratePerHour / 60;
            this.setPrice(this.isRecurringUser ? standardPrice * DISCOUNT : standardPrice);
        }
    }

    private boolean isLessThan30MinutesParking(Duration duration) {
        return duration.toMinutes() < 30;
    }

    private Duration calculateDuration() {
        final LocalDateTime start = this.inTime.toInstant().atZone(systemDefault()).toLocalDateTime();
        final LocalDateTime end = this.outTime.toInstant().atZone(systemDefault()).toLocalDateTime();
        return Duration.between(start, end);
    }

    public boolean isInvalid() {
        return this.outTime == null || this.outTime.before(this.inTime);
    }
}
