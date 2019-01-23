package com.chughes.abqwtb;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import java.util.Date;
import java.util.List;

public class RealTimeInfo {

  public static void main(String[] args) {
    List<Vehicle> vehicles = new HTTPGetter().get();

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
        /*.withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))*/
        .withRegion(Regions.US_EAST_1)
        .build();

    DynamoDB dynamoDB = new DynamoDB(client);

    Table trips = dynamoDB.getTable("Trips");
    Table routes = dynamoDB.getTable("Routes");
    Table times = dynamoDB.getTable("Times");

    for (Vehicle vehicle:vehicles){
      Item trip = trips.getItem("trip_id",Integer.parseInt(vehicle.getTripId()));
      if (trip == null){
        System.out.println("Trip is null");
      }else {
        Item route = routes.getItem("route_id", trip.getInt("route_id"));
        System.out.println(vehicle.getRouteShortName() + " " + route.getString("route_short_name"));
        Item time = times.getItem("trip_id",Integer.parseInt(vehicle.getTripId()),"stop_id", Integer.parseInt(vehicle.getNextStopId()));
        System.out.println(vehicle.getNextStopName() + " " + new Date(time.getInt("arrival_time")*1000 + 7 * 60 * 60 * 1000) + " - " +vehicle.getNextStopSchedTime());
      }
    }
  }

}
