package com.davidoyeku.azure.practical.managment_demo;

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

public class PoliceMonitor {
	
	private String speedingSubscription;
	private ServiceBusContract service;
	private TopicInfo topicInfo;
	private String topic;
	public PoliceMonitor() {
		topic = "speedcamera";
		speedingSubscription = "speeding_vehicles_sub";
		initiateConfigurations();
	}
	
	public void initiateConfigurations(){
		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication(
				"cloudcomputingcoursework",
				"RootManageSharedAccessKey",
				"/c6Fv+2oH0sAnN0kGyua2gLa6RqmZmcViV3xjWEtofE=",
				".servicebus.windows.net");
		service = ServiceBusService.create(config);
	}
	
	public void speedingVehicleSubscription(){
		SubscriptionInfo subInfo = new SubscriptionInfo(speedingSubscription);
		try {
			CreateSubscriptionResult result = service.createSubscription(topic, subInfo);
			RuleInfo ruleInfo = new RuleInfo("speeding_vehicles");
			ruleInfo = ruleInfo.withSqlExpressionFilter("currentSpeed > cameraMaxSpeed");
			CreateRuleResult ruleResult = service.createRule(topic,speedingSubscription , ruleInfo);
			// Delete the default rule, otherwise the new rule won't be invoked.
			service.deleteRule(topic, speedingSubscription, "$Default");
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		PoliceMonitor p = new PoliceMonitor();
		//p.speedingVehicleSubscription();
		p.collectOverSpeedingVehicles();
	}

	private void collectOverSpeedingVehicles() {
		// TODO Auto-generated method stub
		NoSqlConsumer consumer = new NoSqlConsumer();
		consumer.recieveIncomingMessageFrom(speedingSubscription);
	}
	
	private void calculatePercentageIncrease(){
		
	}


}
