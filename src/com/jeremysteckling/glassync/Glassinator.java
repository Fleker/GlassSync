package com.jeremysteckling.glassync;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class Glassinator {

	public static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	public static final String SCOPE = "https://www.googleapis.com/auth/glass.timeline https://www.googleapis.com/auth/userinfo.profile";

	private static final String TIMELINE_URL = "https://www.googleapis.com/mirror/v1/timeline";

	private static final String API_KEY = "";
	private static final String CLIENT_ID = "";
	private static final String CLIENT_SECRET = "";

	private String token;
	private HandlerThread handlerThread;
	private Handler handler;
	private Handler mainHandler;
	
	String TAG = this.getClass().getSimpleName();

	public Glassinator(String token, Handler mainHandler) {
		this.token = token;
		this.mainHandler = mainHandler;

		handlerThread = new HandlerThread("glassinator");
		handlerThread.start();

		handler = new Handler(handlerThread.getLooper());
	}

	public void getTimeline(final Callback callback) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				try {
					URL url = new URL(TIMELINE_URL + "?key=" + API_KEY);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.addRequestProperty("client_id", CLIENT_ID);
					conn.addRequestProperty("client_secret", CLIENT_SECRET);
					conn.setRequestProperty("Authorization", "OAuth " + token);

					mainHandler.post(callback.setStatus(readResponse(conn)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void postTimelineCard(final String title, final String text, final String appname) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				try {
					URL url = new URL(TIMELINE_URL + "?key=" + API_KEY);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("POST");
					conn.addRequestProperty("client_id", CLIENT_ID);
					conn.addRequestProperty("client_secret", CLIENT_SECRET);
					conn.setRequestProperty("Authorization", "OAuth " + token);
					conn.setRequestProperty("Content-Type", "application/json");

					//String item = "{\"text\": \"<article><section><p class=\"text-x-large\">"+title+"</p><p>"+text+"</p></div></section><footer><p>via "+appname+"</p></footer></article>\", \"speakableText\": \""
					//		+ text
					//		+ "\", \"menuItems\": [{\"action\": \"READ_ALOUD\"}]}";
					
					JSONObject json = new JSONObject();
					JSONArray actions = new JSONArray();
					try {
						json.put("html", "<article><section><p class=\"text-x-large\">"+title+"</p><p>"+text+"</p></div></section><footer><p>via "+appname+"</p></footer></article>");
						json.put("speakableText", "Notification from "+appname+". "+text);
						
						JSONObject read_action = new JSONObject();
						read_action.put("action", "READ_ALOUD");
						
						actions.put(read_action);
						
						json.put("menuItems", actions);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String item = json.toString();
					
					Log.d(TAG, "Data sent: "+item);
					
					OutputStreamWriter writer = new OutputStreamWriter(conn
							.getOutputStream());
					BufferedWriter bufferedWriter = new BufferedWriter(writer);
					try {
						bufferedWriter.write(item);
						bufferedWriter.newLine();
						bufferedWriter.flush();
						writer.flush();
					} finally {
						bufferedWriter.close();
						writer.close();
					}

					Log.d(TAG, readResponse(conn));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private String readResponse(HttpURLConnection conn) throws IOException {
		InputStreamReader reader = new InputStreamReader(conn.getInputStream());
		
		BufferedReader bufferedReader = new BufferedReader(reader);
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(
					conn.getResponseCode() + ": " + conn.getResponseMessage()
							+ ":").append(LINE_SEPARATOR);
			for (String s = bufferedReader.readLine(); s != null; s = bufferedReader
					.readLine()) {
				builder.append(s).append(LINE_SEPARATOR);
			}

			return builder.toString();
		} finally {
			bufferedReader.close();
			reader.close();
		}
	}

	public static abstract class Callback implements Runnable {

		protected String status;

		public Callback setStatus(String status) {
			this.status = status;

			return this;
		}

		@Override
		public abstract void run();
	}
}
