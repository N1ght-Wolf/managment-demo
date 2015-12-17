package com.davidoyeku.azure.practical.managment_demo;

import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

public class Vehicle {
	// Vehicle registration plate (e.g. 'BD51 SMR', 'LA51 ABC', 'LL51 JMB')
	//
	// Vehicle type (Car, Truck, Motorcycle)
	//
	// Current speed of travel
	//
	// Details of the speed camera which made the sighting.
	private String regPlate;
	private String vehicleType;
	private int currentSpeed;
	private final String [] VEHICLE_TYPES= {"CAR","TRUCK","MOTORCYCLE"};
	private Random rand;
	private int cameraId;
	public final static String VEHICLE_SUB = "vehicle_sub";
	public static final int TYPE = 0;
	public Vehicle() {
		rand = new Random();
		this.regPlate = generateLicense();
		this.vehicleType = VEHICLE_TYPES[rand.nextInt(VEHICLE_TYPES.length)];
		this.currentSpeed = rand.nextInt(100);
		// TODO Auto-generated constructor stub
	}
	
	public void setCameraId(int id){
		this.cameraId = id;
	}

	private String generateLicense() {
		return RandomStringUtils.randomAlphanumeric(4).toUpperCase();
    }
	public String getRegPlate() {
		return regPlate;
	}

	public void setRegPlate(String regPlate) {
		this.regPlate = regPlate;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public int getCameraId() {
		return cameraId;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Registration:"+regPlate+" "+"VehicleType:"+vehicleType+" "+"Speed:"+currentSpeed+" "+"CameraId"+cameraId;
	}



}
