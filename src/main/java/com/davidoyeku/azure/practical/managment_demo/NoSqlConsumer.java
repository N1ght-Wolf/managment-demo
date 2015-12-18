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
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveSubscriptionMessageResult;

public class NoSqlConsumer {
	private String topic;
	private CloudStorageAccount storageAccount;
	private CloudTableClient tableClient;
	private CloudTable speedCamTable;
	private CloudTable vehicleTable;
	private Configuration config;
	private ServiceBusContract service;
	public static final String storageConnectionString = "DefaultEndpointsProtocol=http;"
			+ "AccountName=courseworkstorage;"
			+ "AccountKey=1m2uphgsSiB+nDsPCzzJeva8IJbvu8H6k7tggqvetKTGT+Y4DEHWday5kZlAnV8u2/ca0hnLhbf1qo3qdcojiQ==";

	public NoSqlConsumer() {
		// TODO Auto-generated constructor stub
		this.topic = "SpeedCamera";
		try {
			initiateConfigurations();
			this.storageAccount = CloudStorageAccount.parse(storageConnectionString);
			this.tableClient = storageAccount.createCloudTableClient();
			vehicleTable = createTable(VehicleEntity.VEHICLE_ENTITY_TABLE);
			speedCamTable = createTable(SpeedCameraEntity.SPEED_CAMERA_ENTITY_TABLE);
		} catch (InvalidKeyException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void initiateConfigurations() {
		config = ServiceBusConfiguration.configureWithSASAuthentication(
				"cloudcomputingcoursework",
				"RootManageSharedAccessKey",
				"/c6Fv+2oH0sAnN0kGyua2gLa6RqmZmcViV3xjWEtofE=",
				".servicebus.windows.net");
		service = ServiceBusService.create(config);
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

	public void addToTable(String subscriber, BrokeredMessage msg) {
		TableOperation insert;
		SpeedCameraEntity speedCameraEntity = null;
		VehicleEntity vehicleEntity = null;
		switch (subscriber) {
		case Vehicle.VEHICLE_SUB:
			
			vehicleEntity = new VehicleEntity(
					(String)msg.getProperty("regPlate"),
					(String)msg.getProperty("vehicleType")
					);
			vehicleEntity.setCameraId((Integer)msg.getProperty("id"));
			vehicleEntity.setCurrentSpeed((Integer)msg.getProperty("currentSpeed"));
			vehicleEntity.setCameraMaxSpeed((Integer)msg.getProperty("cameraMaxSpeed"));
			//vehicleEntity.setTYPE((Integer)msg.getProperty("type"));
			insert = TableOperation.insertOrReplace(vehicleEntity);
			
			try {
				vehicleTable.execute(insert);
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("written to db ..."+VehicleEntity.VEHICLE_ENTITY_TABLE);
			break;
		case SpeedCamera.SPEED_CAM_SUB:
			speedCameraEntity = new SpeedCameraEntity(
					(Integer)msg.getProperty("id"),
					(Integer)msg.getProperty("maxSpeed")
					);
			speedCameraEntity.setStreetName((String) msg.getProperty("streetName"));
			speedCameraEntity.setTown((String)msg.getProperty("town"));
			//speedCameraEntity.setTYPE((Integer)msg.getProperty("type"));
			insert = TableOperation.insertOrReplace(speedCameraEntity);
			try {
				speedCamTable.execute(insert);
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("written to db ..."+SpeedCameraEntity.SPEED_CAMERA_ENTITY_TABLE);
			break;

		default:
			break;
		}
	}

	public void recieveIncomingMessageFrom(String subscriber) {
		try {
			// createTable(subscriber);
			ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
			opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
			while (true) {
				ReceiveSubscriptionMessageResult resultSubMsg = service.receiveSubscriptionMessage(topic, subscriber,
						opts);
				BrokeredMessage message = resultSubMsg.getValue();
				if (message != null && message.getMessageId() != null) {
					System.out.println("MessageID: " + message.getMessageId());
					// Display the topic message.
					addToTable(subscriber, message);
					System.out.print("From topic: ");
					byte[] b = new byte[200];
					String s = null;
					int numRead = message.getBody().read(b);
					while (-1 != numRead) {
						s = new String(b);
						s = s.trim();
						System.out.print(s);
						numRead = message.getBody().read(b);
					}
					System.out.println();
					System.out.println("Custom Property: " + message.getProperty("MessageNumber"));
					// Delete message.
					System.out.println("Deleting this message.");
					service.deleteMessage(message);
				} else {
					System.out.println("Finishing up - no more messages.");
					break;
					// Added to handle no more messages.
					// Could instead wait for more messages to be added.
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
	
	public static void main(String[] args) {
		NoSqlConsumer consumer = new NoSqlConsumer();
		consumer.recieveIncomingMessageFrom(Vehicle.VEHICLE_SUB);
		consumer.recieveIncomingMessageFrom(SpeedCamera.SPEED_CAM_SUB);
	}

}
