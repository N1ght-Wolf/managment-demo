package com.davidoyeku.azure.practical.managment_demo;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.CreateRuleResult;
import com.microsoft.windowsazure.services.servicebus.models.CreateSubscriptionResult;
import com.microsoft.windowsazure.services.servicebus.models.RuleInfo;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.servicebus.models.TopicInfo;

public class Simulation {
	private Configuration config;
	private ServiceBusContract service;
	private TopicInfo topicInfo;
	public static String topic;
	private List<SpeedCamera> speedCams;
	private Random rand;
	
	public Simulation(Path input) {
		speedCams = new ArrayList<>();
		rand = new Random();
		this.topic="SpeedCamera";
		initiateConfigurations();
		//initiateSubscriptions();
		readSpeedCameras(input);
	}

	private void initiateConfigurations() {
		config = ServiceBusConfiguration.configureWithSASAuthentication(
				"cloudcomputingcoursework",
				"RootManageSharedAccessKey",
				"/c6Fv+2oH0sAnN0kGyua2gLa6RqmZmcViV3xjWEtofE=",
				".servicebus.windows.net");
		service = ServiceBusService.create(config);
	}
	
	private void initiateSubscriptions(){
		try {
			speedCameraSubscription();
			vehicleSubscription();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void speedCameraSubscription() throws ServiceException {
		SubscriptionInfo subInfo = new SubscriptionInfo(SpeedCamera.SPEED_CAM_SUB);
		CreateSubscriptionResult result = service.createSubscription(topic, subInfo);
		RuleInfo ruleInfo = new RuleInfo("onlySpeedCameras");
		ruleInfo = ruleInfo.withSqlExpressionFilter("type = "+SpeedCamera.TYPE);
		CreateRuleResult ruleResult = service.createRule(topic,SpeedCamera.SPEED_CAM_SUB , ruleInfo);
		// Delete the default rule, otherwise the new rule won't be invoked.
		service.deleteRule(topic, SpeedCamera.SPEED_CAM_SUB, "$Default");
	}
	private void vehicleSubscription() throws ServiceException {
		SubscriptionInfo subInfo = new SubscriptionInfo(Vehicle.VEHICLE_SUB);
		CreateSubscriptionResult result = service.createSubscription(topic, subInfo);
		RuleInfo ruleInfo = new RuleInfo("onlyVehicles");
		ruleInfo = ruleInfo.withSqlExpressionFilter("type = "+Vehicle.TYPE);
		CreateRuleResult ruleResult = service.createRule(topic,Vehicle.VEHICLE_SUB , ruleInfo);
		// Delete the default rule, otherwise the new rule won't be invoked.
		service.deleteRule(topic, Vehicle.VEHICLE_SUB, "$Default");
	}
	private void readSpeedCameras(Path input) {
		// TODO Auto-generated method stub
		try {
			Scanner scan  = new Scanner(new FileReader(input.toString()));
			//where to store the camera properties
			String cameraProperties [];
			while(scan.hasNextLine()){
				//read the current line of camera properties
				cameraProperties = scan.nextLine().split("\t");
				//add it to the simulation speed cameras
				speedCams.add(new SpeedCamera(cameraProperties,service,topic));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Simulation sim = new Simulation(Paths.get("SpeedCamera.txt"));
		while(true){
			//4 cars per minute
			sim.simulate(2);
			try {
				System.out.println("Sleeping...");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void simulate(int perMinute) {
		SpeedCamera speedCamera ;
		for(int i=0; i<perMinute; i++){
		speedCamera = speedCams.get(rand.nextInt(speedCams.size()));
		speedCamera.recordVehiclePassing();
		}
	}

}
