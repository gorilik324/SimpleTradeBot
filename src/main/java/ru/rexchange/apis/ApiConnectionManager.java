package ru.rexchange.apis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

public class ApiConnectionManager {
	private static final String CUSTOM_USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
	private static final String USER_AGENT_PARAM = "User-Agent";
	private String url = null;

	public ApiConnectionManager(String url) {
		this.url = url;
	}

	public String sendGet(String postfix) throws IOException {
		return sendGet(postfix, null);
	}

	public String sendGet(String postfix, Map<String, String> params) throws IOException {
		URL url = new URL(formUrl(this.url, postfix, params));
		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(USER_AGENT_PARAM, CUSTOM_USER_AGENT);
			// todo magic constants
			connection.setRequestMethod("GET");

			int HttpResult = connection.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				is = connection.getInputStream();
			} else {
				is = connection.getErrorStream();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			return response.toString();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			if (is != null) {
				is.close();
			}
		}
	}

	public String sendPost(String postfix, String request, Map<String, String> props) throws IOException {
		URL url = new URL(formUrl(this.url, postfix));
		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty(USER_AGENT_PARAM, CUSTOM_USER_AGENT);
			// todo magic constants
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			for (Entry<String, String> e : props.entrySet()) {
				connection.setRequestProperty(e.getKey(), e.getValue());
			}
			connection.setRequestMethod("POST");

			try (OutputStream os = connection.getOutputStream()) {
				os.write(request.getBytes());
				os.flush();
			}
			int HttpResult = connection.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				is = connection.getInputStream();
			} else {
				is = connection.getErrorStream();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			return response.toString();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			if (is != null) {
				is.close();
			}
		}
	}

	private final String formUrl(String base, String method) {
		return formUrl(base, method, null);
	}

	private final String formUrl(String base, String method, Map<String, String> params) {
		StringBuilder sb = new StringBuilder(base);
		if (!base.endsWith("/"))
			sb.append("/");
		sb.append(method);
		if (params != null && !params.isEmpty()) {
			sb.append("?");
			boolean first = true;
			for (Entry<String, String> e : params.entrySet()) {
				if (!first) {
					sb.append("&");
				} else {
					first = false;
				}
				sb.append(e.getKey()).append("=").append(e.getValue());
			}
		}
		return sb.toString();
	}
}
