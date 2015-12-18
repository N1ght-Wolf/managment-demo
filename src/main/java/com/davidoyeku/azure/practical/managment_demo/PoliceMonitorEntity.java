package com.davidoyeku.azure.practical.managment_demo;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class PoliceMonitorEntity extends TableServiceEntity {
	private String Priority;
	private int cameraId;
	public static String POLICE_MONITOR_ENTITY_TABLE="SpeedingVehicles";

	public int getCameraId() {
		return cameraId;
	}

	public void setCameraId(int cameraId) {
		this.cameraId = cameraId;
	}

	public PoliceMonitorEntity() {
		// TODO Auto-generated constructor stub
	}
	
	public PoliceMonitorEntity(String regPlate, String vehicleType){
		this.rowKey = regPlate;
		this.partitionKey = vehicleType;
	}

	public String getPriority() {
		return Priority;
	}

	public void setPriority(String priority) {
		Priority = priority;
	}
	
	

}
