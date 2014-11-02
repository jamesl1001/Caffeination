package uk.ac.kent.jal33.caffeination;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import uk.ac.kent.jal33.caffeination.ProductListActivity.ProductListAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class ListViewLoader extends AsyncTask<String, Void, List<Product>> {
	
	protected ProductListAdapter adapter;
	protected String selectedCategory;
	protected Context context;
	protected ProgressDialog progressDialog;
	
	public ListViewLoader(ProductListAdapter ad, String catId, Context con) {
		adapter = ad;
		selectedCategory = catId;
		context = con;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setTitle("Please wait");
		progressDialog.setMessage("Loading products");
		progressDialog.show();
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
				prod.setCategoryId(selectedCategory);
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
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		adapter.setItemList(result);
		adapter.notifyDataSetChanged();

		if(result.isEmpty()) {
			Toast.makeText(context, "No results found. Either the filter you have chosen is too specific, or you have lost connection. Please try again.", Toast.LENGTH_LONG).show();
		}
	}

}
