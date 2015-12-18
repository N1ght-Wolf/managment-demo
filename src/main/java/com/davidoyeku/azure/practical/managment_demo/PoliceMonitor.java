package com.davidoyeku.azure.practical.managment_demo;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.CreateRuleResult;
import com.microsoft.windowsazure.services.servicebus.models.CreateSubscriptionResult;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.RuleInfo;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.servicebus.models.TopicInfo;

public class PoliceMonitor {
	
	private String speedingSubscription;
	private ServiceBusContract service;
	private TopicInfo topicInfo;
	private CloudStorageAccount storageAccount;
	

	private String topic;
	private int maxIncrease = 10;
	private CloudTableClient tableClient;
	private CloudTable overspeeding;
	public static final String storageConnectionString = "DefaultEndpointsProtocol=http;"
			+ "AccountName=courseworkstorage;"
			+ "AccountKey=1m2uphgsSiB+nDsPCzzJeva8IJbvu8H6k7tggqvetKTGT+Y4DEHWday5kZlAnV8u2/ca0hnLhbf1qo3qdcojiQ==";

	public PoliceMonitor() {
		topic = "speedcamera";
		speedingSubscription = "speeding_vehicles_sub";
		initiateConfigurations();
		overspeeding = createTable("SpeedingVehicles");
	}
	
	public void initiateConfigurations(){
		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication(
				"cloudcomputingcoursework",
				"RootManageSharedAccessKey",
				"/c6Fv+2oH0sAnN0kGyua2gLa6RqmZmcViV3xjWEtofE=",
				".servicebus.windows.net");
		service = ServiceBusService.create(config);
		try {
			this.storageAccount = CloudStorageAccount.parse(storageConnectionString);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.tableClient = storageAccount.createCloudTableClient();
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
	
	public void addToTable(BrokeredMessage msg){
		PoliceMonitorEntity policeMonitor = new PoliceMonitorEntity(
				(String)msg.getProperty("regPlate"),
				(String)msg.getProperty("vehicleType"));
		policeMonitor.setCameraId((Integer)msg.getProperty("cameraId"));
		policeMonitor.setPriority((String)msg.getProperty("priority"));

		
		TableOperation insert = TableOperation.insertOrReplace(policeMonitor);
		try {
			overspeeding.execute(insert);
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("written to db ...");
	}

	private void collectOverSpeedingVehicles() {
		// TODO Auto-generated method stub
		try {
			// createTable(subscriber);
			ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
			opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
			while (true) {
				ReceiveSubscriptionMessageResult resultSubMsg = service.receiveSubscriptionMessage(topic, speedingSubscription,
						opts);
				BrokeredMessage message = resultSubMsg.getValue();
				if (message != null && message.getMessageId() != null) {
					
					System.out.println("MessageID: " + message.getMessageId());
					int currentSpeed = (int)message.getProperty("currentSpeed");
					int cameraMaxSpeed = (int)message.getProperty("cameraMaxSpeed");
					double increase= calculatePercentageIncrease(cameraMaxSpeed, currentSpeed);
					boolean overTenPerncent = increase > maxIncrease;
					// Display the topic message.
					byte[] b = new byte[200];
					String s = null;
					int numRead = message.getBody().read(b);
					while (-1 != numRead) {
						s = new String(b);
						s = s.trim();
						numRead = message.getBody().read(b);
					}
					if(overTenPerncent){
						s= s+" PRIORITY";
						message.setProperty("priority","priority");
					}else{
						message.setProperty("priority","non-priority");
					}
					addToTable(message);
					System.out.println(s);
					//System.out.println("Custom Property: " + message.getProperty("MessageNumber"));
					// Delete message.
					System.out.println("Deleting this message.");
					service.deleteMessage(message);
				} else {
					System.out.println("Finishing up - no more messages.");
					break;
				}
			}
		} catch (ServiceException e) {
			System.out.print("ServiceException encountered: ");
			System.out.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			System.out.print("Generic exception encountered: ");
			System.out.println(e.getMessage());
		}
	}
	
	private double calculatePercentageIncrease(int cameraMaxSpeed, int currentSpeed){
		double increase = currentSpeed - cameraMaxSpeed;
		increase  = increase /cameraMaxSpeed;
		
		return increase*100;
	}
	public CloudTable createTable(String tableName) {
		CloudTable cloudTable = null;
		try {
			// Create the table if it doesn't exist.
			cloudTable = tableClient.getTableReference(tableName);
			cloudTable.createIfNotExists();
		} catch (Exception e) {
			// Output the stack trace.
			e.printStackTrace();
		}
		return cloudTable;
	}



}
