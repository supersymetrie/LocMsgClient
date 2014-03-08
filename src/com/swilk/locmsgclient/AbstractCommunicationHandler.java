package com.swilk.locmsgclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public abstract class AbstractCommunicationHandler {
	
	private enum Actions {Get, Post};
	
	String host;
	
	public AbstractCommunicationHandler(String host) 
	{
		this.host = host;
	}
	
	@SuppressWarnings("unchecked")
	public void postJSON(Map<String, String> toPost) 
	{
		//Convert Map to JSON
		JSONObject jsonData = new JSONObject(toPost);
		String[] params = {this.host, jsonData.toString()};
		ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(params));
		SendMessage newPost = (SendMessage) new SendMessage(Actions.Post);
		newPost.execute(arguments);
	}
	
	private class SendMessage extends AsyncTask<ArrayList<String>, String, String>
	{
		Actions msgAction;
		
		protected SendMessage(Actions type)
		{
			this.msgAction = type;
		}

		
		protected String doInBackground(ArrayList<String>... params) {
			//Get Host & JSON list
			ArrayList<String> arguments = params[0];
			//Get Host Argument
			String host = arguments.get(0);
			//Create POST
			HttpUriRequest request = null;
			//Get JSON Argument
			String JSONString = arguments.get(1);
			try {
				switch(this.msgAction) {
				case Get:
					//Do http request
					request = generateGetRequest(host, JSONString);
					break;
				case Post:
					//Do the post stuff\
					request = generatePostRequest(host, JSONString);
					break;
					
				}
				HttpClient httpClient = new DefaultHttpClient();
				//Post the JSON.
				HttpResponse serverResponse = httpClient.execute(request);
				return serverResponse.toString();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Couldn't post for some reason
			return "Error sending message to server";
		}
		
		private HttpPost generatePostRequest(String host, String jsonData) throws UnsupportedEncodingException
		{
			HttpPost httpPost = new HttpPost(host);
			StringEntity entity = new StringEntity(jsonData);
			httpPost.setEntity(entity);
			return httpPost;
		}
		
		@SuppressWarnings("unchecked")
		private HttpGet generateGetRequest(String host, String jsonData) throws JSONException
		{
			String request_url = "";
			JSONObject parsedJSON = new JSONObject(jsonData);
			Iterator<String> jsonIterator;
			boolean firstRun = true; 
			for(jsonIterator = parsedJSON.keys(); jsonIterator.hasNext();) 
			{
				String key = jsonIterator.next();
				if(firstRun)
				{
					request_url += host + "?" + key + "=" + parsedJSON.getString(key);
                    firstRun = false;
				}
				else
				{
					request_url += request_url + "&" + key + "=" + parsedJSON.getString(key);
				}
			}
			HttpGet httpGet = new HttpGet(request_url);
			return httpGet;
		}
		
	}
	
}
