package com.jwt.rest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.jwt.components.Location;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.Hash;
import com.mongodb.util.JSON;

@Path("/weather")
public class WeatherAPI {	
	// Get list of users
	@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers() throws UnknownHostException, JSONException{
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		DB db = mongo.getDB("hockey");
		DBCollection users = db.getCollection("users");
		DBCursor cursor = users.find();
		List<JSONObject> response = new ArrayList<JSONObject>();
		while (cursor.hasNext()) {
			DBObject user = cursor.next();
			String name = (String) user.get("username");
			JSONObject json = new JSONObject();
			json.put("username", name);
			response.add(json);
		}
		return Response.status(200).entity(response.toString()).build();
	}
	// Add location to a user. By params receive json with user and location data.
	@POST
	@Path("/addLocation")
	@Consumes({MediaType.TEXT_PLAIN})
	public Response addLocation(String input) throws IOException, JSONException{
		//INPUT READER
		JSONObject json = new JSONObject(input);

		//DB CONNECTION
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		DB db = mongo.getDB("hockey");
		DBCollection users = db.getCollection("users");
		
		//UPDATE LOCATIONS
		BasicDBObject query = new BasicDBObject();
		query.put("username", json.get("username"));
		BasicDBObject push = new BasicDBObject();
		
		push.put("$push", new BasicDBObject("locations", new BasicDBObject("city",json.get("city"))));
		users.update(query, push);
		
		//RESPONSE
		return Response.status(200).entity("OK").build();
	}
	// Delete location to a user. By params receive json with user and location data.
	@POST
	@Path("/deleteLocation")
	@Consumes({MediaType.TEXT_PLAIN})
	public Response deleteLocation(String input) throws IOException, JSONException{
		//INPUT READER
		JSONObject json = new JSONObject(input);

		//DB CONNECTION
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		DB db = mongo.getDB("hockey");
		DBCollection users = db.getCollection("users");

		BasicDBObject query = new BasicDBObject("username", json.get("username"));
	    BasicDBObject fields = new BasicDBObject("locations", 
	        new BasicDBObject( "city", ((String)json.get("city")).toLowerCase()));
	    BasicDBObject update = new BasicDBObject("$pull",fields);
	    users.update( query, update );
		
		//RESPONSE
		return Response.status(200).entity("OK").build();
	}
	// Add User. By params receive json with user information.
	@POST
	@Path("/addUser")
	@Consumes({MediaType.TEXT_PLAIN})
	public Response addUser(String input) throws IOException, JSONException{
		//INPUT READER
		JSONObject json = new JSONObject(input);

		//DB CONNECTION
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		DB db = mongo.getDB("hockey");
		DBCollection users = db.getCollection("users");
		
		//UPDATE LOCATIONS
		BasicDBObject query = new BasicDBObject();
		//query.put("username", json.get("username"));
		BasicDBObject insert = new BasicDBObject();
		
		insert.put("username", json.get("username"));
		users.insert(insert);
		
		//RESPONSE
		return Response.status(200).entity("OK").build();
	}
	// Get Board data for a user. User is send by PathParam
	@GET
	@Path("board/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBoard(@PathParam("user") String user) throws UnknownHostException, JSONException {
		
		MongoClient mongo = new MongoClient("localhost", 27017);
		DB db = mongo.getDB("hockey");
		DBCollection users = db.getCollection("users");
		
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		BasicDBObject query = new BasicDBObject();
		BasicDBObject field = new BasicDBObject();
		query.put("username", user);
		field.put("locations", 1);
		DBCursor cursor = db.getCollection("users").find(query,field);
		while (cursor.hasNext()) {
		    BasicDBObject obj = (BasicDBObject) cursor.next();
		    JSONObject jsonobj = new JSONObject();
		    BasicDBList location = (BasicDBList) obj.get("locations");
		    // optional: break it into a native java array
		    if (location != null) {
			    BasicDBObject[] lightArr = location.toArray(new BasicDBObject[0]);
			    for(BasicDBObject dbObj : lightArr) {
			      // shows each item from the lights array
			      System.out.println(dbObj.get("city"));
			      result.add(getYahooWeather(dbObj.get("city").toString()));
			    }
		    }
		}
		JSONObject json = new JSONObject();
		json.put("data", result);
		return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json.toString()).build();
	}
	// Get weather information from YahooWeather Developer API.
	private JSONObject getYahooWeather(String city) throws JSONException {
		URL url;
		String resultado = "";
		try {
			city = URLEncoder.encode(city, "UTF-8");
			url = new URL("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22"+city+"%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				resultado+=output;
			}

			conn.disconnect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject json = new JSONObject(resultado);
		JSONObject result = new JSONObject();
		json = (JSONObject) json.get("query");
		json = (JSONObject)json.get("results");
		json = json.getJSONObject("channel");
		return json;
	}
	
}
