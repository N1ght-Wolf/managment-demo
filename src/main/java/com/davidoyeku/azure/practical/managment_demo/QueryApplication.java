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
//	private CloudTable speedCamTable;
//	private CloudTable vehicleTable;
	private Configuration config;
	private ServiceBusContract service;
	final String PARTITION_KEY = "PartitionKey";
	public static final String storageConnectionString = "DefaultEndpointsProtocol=http;"
			+ "AccountName=courseworkstorage;"
			+ "AccountKey=1m2uphgsSiB+nDsPCzzJeva8IJbvu8H6k7tggqvetKTGT+Y4DEHWday5kZlAnV8u2/ca0hnLhbf1qo3qdcojiQ==";

	public QueryApplication() {
		initiateConfigurations();
	}

	private void initiateConfigurations() {
		config = ServiceBusConfiguration.configureWithSASAuthentication("cloudcomputingcoursework",
				"RootManageSharedAccessKey", "/c6Fv+2oH0sAnN0kGyua2gLa6RqmZmcViV3xjWEtofE=", ".servicebus.windows.net");
		service = ServiceBusService.create(config);
		try {
			storageAccount = CloudStorageAccount.parse(storageConnectionString);
			tableClient = storageAccount.createCloudTableClient();

		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getHighPrioritySightings() {
		Iterable<SpeedCameraEntity> allCameras = getAllSpeedCameras();
		Iterable<PoliceMonitorEntity> allPoliceMonitor = getAllPoliceMonitorEntity();

		int cameraId;
		for (PoliceMonitorEntity policeMonitorEntity : allPoliceMonitor) {
			cameraId= policeMonitorEntity.getCameraId();
//			System.out.println(cameraId);
			for (SpeedCameraEntity speedEntity : allCameras) {
//				System.out.println( speedEntity.getPartitionKey());
				if(cameraId == Integer.parseInt(speedEntity.getPartitionKey())){
					System.out.println(policeMonitorEntity.getRowKey()+" spotted at "+speedEntity.getStreetName()+" "+speedEntity.getTown());
					break;
				}
			}
		}

	}

	public Iterable<PoliceMonitorEntity> getAllPoliceMonitorEntity() {
		CloudTable cloudTable = null;
		TableQuery<PoliceMonitorEntity> policeMonitor = null;
		try {
			String partitionFilter = TableQuery.generateFilterCondition("Priority", QueryComparisons.EQUAL, "priority");
			cloudTable = tableClient.getTableReference(PoliceMonitorEntity.POLICE_MONITOR_ENTITY_TABLE);
			policeMonitor = TableQuery.from(PoliceMonitorEntity.class).where(partitionFilter);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cloudTable.execute(policeMonitor);
	}

	public Iterable<VehicleEntity> getAllVehicles() {
		CloudTable cloudTable = null;
		TableQuery<VehicleEntity> vehicles = null;
		try {
			cloudTable = tableClient.getTableReference(VehicleEntity.VEHICLE_ENTITY_TABLE);
			vehicles = TableQuery.from(VehicleEntity.class);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cloudTable.execute(vehicles);
	}

	private Iterable<SpeedCameraEntity> getAllSpeedCameras() {
		Iterable<SpeedCameraEntity> speedCameras = null;
		try {
			CloudTable cloudTable = tableClient.getTableReference(SpeedCameraEntity.SPEED_CAMERA_ENTITY_TABLE);
			TableQuery<SpeedCameraEntity> allSpeedCameras = TableQuery.from(SpeedCameraEntity.class);
			speedCameras = cloudTable.execute(allSpeedCameras);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return speedCameras;
	}

	private void showAllSpeedCameras() {
		for (SpeedCameraEntity entity : getAllSpeedCameras()) {
			System.out.println(entity.getPartitionKey() + "=> maxspeed" );
			System.out.println(entity.getRowKey() + "=> identifier");
			System.out.println(entity.getStreetName() + "=> streetName");
			System.out.println(entity.getTown() + "=> town");
			System.out.println("--------------------------------");
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		QueryApplication query = new QueryApplication();
		//query.showAllSpeedCameras();
		 query.getHighPrioritySightings();
	}

}
