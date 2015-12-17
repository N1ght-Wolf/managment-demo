package com.davidoyeku.azure.practical.managment_demo;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;

public class QueryApplication {
	private CloudStorageAccount storageAccount;
	private CloudTableClient tableClient;
	private CloudTable speedCamTable;
	private CloudTable vehicleTable;
	private Configuration config;
	private ServiceBusContract service;
	public static final String storageConnectionString = 
			"DefaultEndpointsProtocol=http;"
			+ "AccountName=courseworkstorage;"
			+ "AccountKey=1m2uphgsSiB+nDsPCzzJeva8IJbvu8H6k7tggqvetKTGT+Y4DEHWday5kZlAnV8u2/ca0hnLhbf1qo3qdcojiQ==";
	
	public QueryApplication() {
		initiateConfigurations();
	}

	private void initiateConfigurations() {
		config = ServiceBusConfiguration.configureWithSASAuthentication(
				"cloudcomputingcoursework",
				"RootManageSharedAccessKey",
				"/c6Fv+2oH0sAnN0kGyua2gLa6RqmZmcViV3xjWEtofE=",
				".servicebus.windows.net");
		service = ServiceBusService.create(config);
		try {
			CloudStorageAccount storageAccount =
				       CloudStorageAccount.parse(storageConnectionString);
		     tableClient = storageAccount.createCloudTableClient();
		    
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void getAllSpeedCameras(){
		String type = "type";
	    try {
			CloudTable cloudTable = tableClient.getTableReference(SpeedCameraEntity.SPEED_CAMERA_ENTITY_TABLE);
//			String fetchSpeedCamera = TableQuery.generateFilterCondition(type, QueryComparisons.EQUAL, SpeedCameraEntity.TYPE);
			  TableQuery<SpeedCameraEntity> allSpeedCameras = TableQuery.from(SpeedCameraEntity.class);
			  for (SpeedCameraEntity entity : cloudTable.execute(allSpeedCameras)) {
				  System.out.println("adsdas");
			        System.out.println(entity.getPartitionKey() +
			            " " +entity.toString());
			    }
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		QueryApplication query = new QueryApplication();
		query.getAllSpeedCameras();
	}

}
