package com.chughes.abqwtb;


import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HTTPGetter {

  public static void main(String[] args) throws InterruptedException {


    Observable.interval(15, TimeUnit.SECONDS, Schedulers.io())
        .subscribe(aLong -> System.out.println(new HTTPGetter().get().size()));

    Thread.sleep(50000);
  }

  public List<Vehicle> get() {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://data.cabq.gov/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    AbqDataService service = retrofit.create(AbqDataService.class);

    try {
      return service.listVehicles().execute().body().getAllroutes();
    } catch (IOException e) {
      throw new RuntimeException("Error getting data");
    }
  }

}
