package uk.ac.kent.jal33.caffeination;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.os.AsyncTask;

public class FavouritesLoader extends AsyncTask<String, Void, List<Product>> {

	protected Context context;
	protected CaffeinationApp application;
	
	public FavouritesLoader(Context con, CaffeinationApp app) {
		context = con;
		application = app;
	}
	
	@Override
	protected List<Product> doInBackground(String... params) {
		// The output is a list of products
		List<Product> result = new ArrayList<Product>();
		
		try {
			// The first parameter is the url we should use
			URL u = new URL(params[0]);
			// Establish the connection
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.connect();
			if (conn.getResponseCode() != 200)
				return null;
			InputStream is = conn.getInputStream();
			
			// Read the stream
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			
			while (is.read(buffer) != -1) {
				bytestream.write(buffer);
			}
			
			// Extract as JSON
			String jsonStr = new String(bytestream.toByteArray());
			JSONArray arr = new JSONArray(jsonStr);
			
			// Get each product in the json list
			for (int i = 0; i < arr.length(); i++) {
				Product prod = new Product();
				prod.updateFromJSON(arr.getJSONObject(i));
				result.add(prod);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(List<Product> result) {
		if(result != null) {
			for (int i=0; i < result.size(); i++) {
				Product p = result.get(i);
				if(!application.getMatch(p.getProductId(), 2)) {
					application.favs.add(p);
				}
			}
		}
	}

}
