package com.davidoyeku.azure.practical.managment_demo;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class VehicleEntity  extends TableServiceEntity {
	private String regPlate;
	private String vehicleType;
	private int currentSpeed;
	private int cameraId;
	public static final String VEHICLE_ENTITY_TABLE = "vehicle"; 
	public static  int TYPE=0;
	
	public static int getTYPE() {
		return TYPE;
	}
	public static void setTYPE(int tYPE) {
		TYPE = tYPE;
	}
	public VehicleEntity(String regPlate, String vehicleType) {
		this.partitionKey = regPlate;
		this.rowKey = vehicleType;
	}
	public VehicleEntity() {
		// TODO Auto-generated constructor stub
	}
	

	public String getRegPlate() {
//		return regPlate;
//	}
//
//	public void setRegPlate(String regPlate) {
//		this.regPlate = regPlate;
//	}
//
//	public String getVehicleType() {
//		return vehicleType;
//	}
//
//	public void setVehicleType(String vehicleType) {
//		this.vehicleType = vehicleType;
//	}
//
//	public int getCurrentSpeed() {
//		return currentSpeed;
//	}

	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public int getCameraId() {
		return cameraId;
	}

	public void setCameraId(int cameraId) {
		this.cameraId = cameraId;
	}

}
