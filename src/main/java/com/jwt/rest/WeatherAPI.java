package com.jwt.rest;

import java.awt.PageAttributes.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/weather")
public class WeatherAPI {
	

//    @GET
//    @Path("/{name}")
//    public Response getMsg(@PathParam("name") String name) {
//  
//        String output = "VAmo   : " + name;
//  
//        return Response.status(200).entity(output).build();
//  
//    }
	
    /*Servicio para obtener el servicio de Yahoo pasando como parametro una ciudad especifica.*/
    @GET
    @Path("/{city}")
    public Response getYahooWeather(@PathParam("city") String city) {
    	try {
    		URL url = new URL("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22"+city+"%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setRequestMethod("GET");
    		conn.setRequestProperty("Accept", "application/json");
    		if (conn.getResponseCode() != 200) {
    			throw new RuntimeException("Failed : HTTP error code : "
    					+ conn.getResponseCode());
    		}
    		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
    		String output;
    		String resultado = "";
    		System.out.println("Output from Server .... \n");
    		while ((output = br.readLine()) != null) {
    			System.out.println(output);
    			resultado += output;
    			
    		}
    		conn.disconnect();
   	        return Response.status(200).entity(resultado).build();
   	        
    	  } catch (MalformedURLException e) {
    		e.printStackTrace();
    	  } catch (IOException e) {
    		e.printStackTrace();
    	  }
    	  return null;
    }

}
