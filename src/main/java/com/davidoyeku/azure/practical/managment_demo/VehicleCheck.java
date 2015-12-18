package com.davidoyeku.azure.practical.managment_demo;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveSubscriptionMessageResult;

public class VehicleCheck {
	
	private Configuration config;
	private ServiceBusContract service;
	private String topic;
	public static final String storageConnectionString = "DefaultEndpointsProtocol=http;"
			+ "AccountName=courseworkstorage;"
			+ "AccountKey=1m2uphgsSiB+nDsPCzzJeva8IJbvu8H6k7tggqvetKTGT+Y4DEHWday5kZlAnV8u2/ca0hnLhbf1qo3qdcojiQ==";

	public VehicleCheck() {
		topic = "SpeedCamera";
		initiateConfigurations();
		//recieveIncomingMessageFrom("vehicle_sub");
		recieveFromQueue();
	}
	
	private void initiateConfigurations() {
		config = ServiceBusConfiguration.configureWithSASAuthentication(
				"cloudcomputingcoursework",
				"RootManageSharedAccessKey",
				"/c6Fv+2oH0sAnN0kGyua2gLa6RqmZmcViV3xjWEtofE=",
				".servicebus.windows.net");
		service = ServiceBusService.create(config);
	}
	public void recieveFromQueue(){
		try
		{
		    // Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount = 
		        CloudStorageAccount.parse(storageConnectionString);

		    // Create the queue client.
		    CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

		    // Retrieve a reference to a queue.
		    CloudQueue queue = queueClient.getQueueReference("speedingcarqueue");
		    queue.downloadAttributes();

		    // Retrieve the newly cached approximate message count.
		    int cachedMessageCount = (int)queue.getApproximateMessageCount();
		    // Retrieve 20 messages from the queue with a visibility timeout of 300 seconds.
		    for (CloudQueueMessage message : queue.retrieveMessages(cachedMessageCount, 300, null, null)) {
		        // Do processing for all messages in less than 5 minutes, 
		        // deleting each message after processing.
//		        queue.deleteMessage(message);
		    	System.out.println(message.getMessageContentAsString());
		    }
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
	}
	public void recieveIncomingMessageFrom(String subscriber) {
		try {
			System.out.println("subscriber "+ subscriber);
			// createTable(subscriber);
			ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
			opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
			while (true) {
				ReceiveSubscriptionMessageResult resultSubMsg = service.receiveSubscriptionMessage(topic, subscriber,
						opts);
				BrokeredMessage message = resultSubMsg.getValue();
				System.out.println("Custom Property: " + message.getProperty("regPlate"));
				System.out.println("Custom Property: " + message.getProperty("cameraId"));
				if (message != null && message.getMessageId() != null) {
					System.out.println("MessageID: " + message.getMessageId());
					// Display the topic message.
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
//					System.out.println("Custom Property: " + message.getProperty("MessageNumber"));
					// Delete message.
					System.out.println("Deleting this message.");
					isVehicleStolen((String)message.getProperty("regPlate"));
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
	
	public static boolean isVehicleStolen(String vehicleRegistration)
	{
	    try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return (Math.random() < 0.95);
	}
	
	public static void main(String[] args) {
		VehicleCheck vehicleCheck = new VehicleCheck();
	}
}
