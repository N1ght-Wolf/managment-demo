package com.davidoyeku.azure.practical.managment_demo;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class SpeedCameraEntity extends TableServiceEntity {
	private int id;
	private String streetName;
	private String town;
	private int maxSpeed;
	public static final String SPEED_CAMERA_ENTITY_TABLE = "speedcamera"; 
	public static  int TYPE=1;
	
	public static int getTYPE() {
		return TYPE;
	}

	public static void setTYPE(int tYPE) {
		TYPE = tYPE;
	}

	public SpeedCameraEntity() {
		// TODO Auto-generated constructor stub
	}

//	public int getId() {
//		return id;
//	}
//	public void setId(int id) {
//		this.id = id;
//	}
	public String getStreetName() {
		return streetName;
	}
	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}
	public String getTown() {
		return town;
	}
	public void setTown(String town) {
		this.town = town;
	}
//	public int getMaxSpeed() {
//		return maxSpeed;
//	}
//	public void setMaxSpeed(int maxSpeed) {
//		this.maxSpeed = maxSpeed;
//	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "id: "+this.id+" town:"+this.town+" streetName: "+this.streetName+" maxSpeed: "+this.maxSpeed;
		}
	public SpeedCameraEntity(int id, int maxSpeed) {
		// TODO Auto-generated constructor stub
		this.partitionKey = id+"";
		this.rowKey = maxSpeed+"";

	}

}
