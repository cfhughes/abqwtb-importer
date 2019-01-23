/*
package com.chughes.abqwtb;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class ImportGTFS {

  private static GtfsDaoImpl store;
  private static DynamoDB dynamoDB;

  static Set<String> c_set = new HashSet<>();
  static Set<Integer> t_set = new HashSet<>();

  public static void main(String[] args) throws IOException, InterruptedException {
    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File("C:/Users/chris/gtfs.zip"));

    store = new GtfsDaoImpl();
    reader.setEntityStore(store);

    reader.run();

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
        */
/*.withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
        *//*
.withRegion(Regions.US_EAST_1)
        .build();

    dynamoDB = new DynamoDB(client);

    importRoutes();
    importCalendar();
    importTrips();
    importStops();
    importTimes();


  }

  static void importRoutes() throws InterruptedException {
    deleteTable("Routes");
    Table table = dynamoDB.createTable("Routes",
        Arrays.asList(new KeySchemaElement("route_id", KeyType.HASH)),
        Arrays.asList(new AttributeDefinition("route_id", ScalarAttributeType.N)),
        new ProvisionedThroughput(1L, 5L));
    table.waitForActive();
    for (Route r : store.getAllRoutes()) {
      Item item = new Item().withPrimaryKey("route_id", Integer.parseInt(r.getId().getId()))
          .withString("route_short_name", r.getShortName());
      table.putItem(item);
    }
  }

  static void importCalendar() throws InterruptedException {
    deleteTable("Calendar");
    Table table = dynamoDB.createTable("Calendar",
        Arrays.asList(new KeySchemaElement("service_id", KeyType.HASH)),
        Arrays.asList(new AttributeDefinition("service_id", ScalarAttributeType.S)),
        new ProvisionedThroughput(1L, 5L));
    table.waitForActive();
    for (ServiceCalendar sc : store.getAllCalendars()) {
      if (sc.getEndDate().compareTo(new ServiceDate()) >= 0 && (sc.getMonday() > 0 || sc.getSaturday() > 0 || sc.getSunday() > 0)) {
        Item item = new Item().withPrimaryKey("service_id", sc.getServiceId().getId())
            .withBoolean("weekday", sc.getMonday() > 0)
            .withBoolean("saturday", sc.getSaturday() > 0)
            .withBoolean("sunday", sc.getSunday() > 0);
        table.putItem(item);
        c_set.add(sc.getServiceId().getId());
        System.out.println("Inserted " + sc.getServiceId().getId());
      }
    }
  }

  static void importTrips() throws InterruptedException {
    deleteTable("Trips");
    Table table = dynamoDB.createTable("Trips",
        Arrays.asList(new KeySchemaElement("trip_id", KeyType.HASH)),
        Arrays.asList(new AttributeDefinition("trip_id", ScalarAttributeType.N)),
        new ProvisionedThroughput(1L, 5L));
    table.waitForActive();
    for (Trip t : store.getAllTrips()) {
      if (!c_set.contains(t.getServiceId().getId())) {
        continue;
      }
      int tripId = Integer.parseInt(t.getId().getId());
      Item item = new Item().withPrimaryKey("trip_id", tripId)
          .withInt("route_id", Integer.parseInt(t.getRoute().getId().getId()))
          .withString("service_id", t.getServiceId().getId());
      table.putItem(item);
      t_set.add(tripId);
    }
    System.out.println("Inserted " + t_set.size() + " Trips");
  }

  static void importStops() throws InterruptedException {
    deleteTable("Stops");
    Table table = dynamoDB.createTable("Stops",
        Arrays.asList(new KeySchemaElement("stop_id", KeyType.HASH)),
        Arrays.asList(new AttributeDefinition("stop_id", ScalarAttributeType.N)),
        new ProvisionedThroughput(1L, 5L));
    table.waitForActive();
    for (Stop s : store.getAllStops()) {
      Item item = new Item().withPrimaryKey("stop_id", Integer.parseInt(s.getId().getId()))
          .withDouble("stop_lon", s.getLon())
          .withDouble("stop_lat", s.getLat())
          .withString("stop_name", s.getName());
      if (s.getDesc() != null) {
        item = item.withString("stop_desc", s.getDesc());
      }
      table.putItem(item);
    }
  }

  static void importTimes() throws InterruptedException {
    deleteTable("Times");
    Table table = dynamoDB.createTable("Times",
        Arrays.asList(new KeySchemaElement("trip_id", KeyType.HASH),
            new KeySchemaElement("stop_id", KeyType.RANGE)),
        Arrays.asList(new AttributeDefinition("trip_id", ScalarAttributeType.N),
            new AttributeDefinition("stop_id", ScalarAttributeType.N)),
        new ProvisionedThroughput(5L, 50L));
    table.waitForActive();
    int count = 0;
    for (StopTime st : store.getAllStopTimes()) {
      int tripId = Integer.parseInt(st.getTrip().getId().getId());
      count++;
      if (!t_set.contains(tripId)) {
        continue;
      }
      Item item = new Item()
          .withPrimaryKey("trip_id", tripId, "stop_id",
              Integer.parseInt(st.getStop().getId().getId()))
          .withInt("arrival_time", st.getArrivalTime())
          .withInt("stop_sequence", st.getStopSequence());
      table.putItem(item);
      if (count % 1000 == 0){
        System.out.println("Read through "+count+" times");
      }
    }
  }


  private static void deleteTable(String tableName) {
    Table table = dynamoDB.getTable(tableName);
    try {
      System.out.println("Issuing DeleteTable request for " + tableName);
      table.delete();

      System.out.println("Waiting for " + tableName + " to be deleted...this may take a while...");

      table.waitForDelete();
    } catch (Exception e) {
      System.err.println("DeleteTable request failed for " + tableName);
      System.err.println(e.getMessage());
    }
  }


}
*/
