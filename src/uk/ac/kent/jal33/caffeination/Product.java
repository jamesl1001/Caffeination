package uk.ac.kent.jal33.caffeination;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Product {
	private String selectedCategory;
	private int productId;
	private String productName;
	private int size;
	private int caffeine;
	private double price;
	private String description;
	private ImageView imgView;
	private Activity activity;
	
	public void getFeatured(Activity act, final CaffeinationApp app) {
		activity = act;
		
		String url = String.format(MainActivity.WEBSERVER_GETFEATURED);
		
		AsyncTask<String, Void, JSONObject> task = new AsyncTask<String, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(String... params) {
				// The output is a list of products
				JSONObject obj = new JSONObject();
				
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
					
					while (is.read(buffer) != -1)
						bytestream.write(buffer);
					
					// Extract as JSON
					String jsonStr = new String(bytestream.toByteArray());
					obj = new JSONObject(jsonStr);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				
				return obj;
			}

			@Override
			protected void onPostExecute(JSONObject result) {
				// Update the Product
				updateFromJSON(result);
				
				// Refresh the UI
				if (activity != null) {
					DecimalFormat df = new DecimalFormat("0.00");

					RelativeLayout featured = (RelativeLayout) activity.findViewById(R.id.featured);
		            ImageView image         = (ImageView) activity.findViewById(R.id.featured_image);
		    		ProgressBar progBar     = (ProgressBar) activity.findViewById(R.id.featured_image_progress);
					TextView name           = (TextView) activity.findViewById(R.id.featured_name);
					TextView price          = (TextView) activity.findViewById(R.id.featured_price);

					name.setText(getProductName());
					price.setText("£"+df.format(getPrice()));
		            loadImage(app, image, 54, 54, progBar);

					featured.setOnClickListener(new OnClickListener() {
			    		public void onClick(final View v) {
			    			Intent intent = new Intent(activity, ProductActivity.class);
							intent.putExtra(MainActivity.SELECTED_PRODUCT_ID, getProductId());
							intent.putExtra(MainActivity.SELECTED_PRODUCT_NAME, getProductName());
							intent.putExtra(MainActivity.SELECTED_PRODUCT_SIZE, getSize());
							intent.putExtra(MainActivity.SELECTED_PRODUCT_CAFFEINE, getCaffeine());
							intent.putExtra(MainActivity.SELECTED_PRODUCT_PRICE, getPrice());
			    	    	activity.startActivity(intent);
			    		}
			    	});
				}
			}
		};
		
		task.execute(url);
	}
	
	public void loadDetails(Activity act, final Context con) {
		activity = act;
		
		String url = String.format(MainActivity.WEBSERVER_GETDETAILS, productId);
		
		AsyncTask<String, Void, JSONObject> task = new AsyncTask<String, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(String... params) {
				// The output is a list of products
				JSONObject obj = new JSONObject();
				
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
					
					while (is.read(buffer) != -1)
						bytestream.write(buffer);
					
					// Extract as JSON
					String jsonStr = new String(bytestream.toByteArray());
					obj = new JSONObject(jsonStr);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				
				return obj;
			}

			@Override
			protected void onPostExecute(JSONObject result) {
				// Update the Product
				updateFromJSON(result);
				
				// Refresh the UI
				if (activity != null) {
					TextView desc = (TextView) activity.findViewById(R.id.product_description);
					desc.setText(getDescription());

			    	Animation animateFeatured = AnimationUtils.loadAnimation(con, R.anim.fade_in);
			    	desc.startAnimation(animateFeatured);
			    	
					desc.setMovementMethod(new ScrollingMovementMethod());
				}
			}
		};
		
		task.execute(url);
	}
	
	public void loadImage(final CaffeinationApp app, ImageView view, int width, int height, final ProgressBar progBar) {
		// Store the ImageView element we need to update
		imgView = view;
		
		// Create the URL string
		final String url = String.format(MainActivity.WEBSERVER_GETIMAGE, productId, width, height, 0);
		
		final Bitmap image = app.getBitmapFromMemCache(url);

		if (image != null) {
			imgView.setImageBitmap(image);
			progBar.setVisibility(View.INVISIBLE);
		} else {
			AsyncTask<String, Void, Bitmap> task = new AsyncTask<String, Void, Bitmap>() {
	
				@Override
				protected Bitmap doInBackground(String... params) {
					Bitmap image = null;
					
					try {
						URL url = new URL(params[0]);
						// Connect
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.connect();
						// Problems with the server
						if (conn.getResponseCode() != 200) return null;
						// Read the image into a bitmap
						InputStream is = conn.getInputStream();
						image = BitmapFactory.decodeStream(is);
					} catch (Throwable t) {
						t.printStackTrace();
					}

					app.addBitmapToMemoryCache(url, image);
					return image;
				}

				@Override
				protected void onPostExecute(Bitmap result) {
					if(result != null) {
						// Show the new image
						imgView.setImageBitmap(result);
						progBar.setVisibility(View.INVISIBLE);
					}
				}
				
			};
			
			task.execute(url);
		}
	}
	
	public int getProductId() {                          return productId; }
	public void setProductId(int productId) {            this.productId = productId; }
	public String getCategoryId() {                      return selectedCategory; }
	public void setCategoryId(String selectedCategory) { this.selectedCategory = selectedCategory; }
	public String getProductName() {                     return productName; }
	public void setProductName(String productName) {     this.productName = productName; }
	public double getPrice() {                           return price; }
	public void setPrice(double price) {                 this.price = price; }
	public int getSize() {                               return size; }
	public void setSize(int size) {                      this.size = size; }
	public int getCaffeine() {                           return caffeine; }
	public void setCaffeine(int caffeine) {              this.caffeine = caffeine; }
	public String getDescription() {                     return description; }
	public void setDescription(String description) {     this.description = description; }
	
	public void updateFromJSON(JSONObject jsonObj) {
		try {
			if (jsonObj.has("id"))
				productId = jsonObj.getInt("id");
			if (jsonObj.has("name"))
				productName = jsonObj.getString("name");
			if (jsonObj.has("price"))
				price = jsonObj.getDouble("price");
			if (jsonObj.has("size"))
				size = jsonObj.getInt("size");
			if (jsonObj.has("caffeine"))
				caffeine = jsonObj.getInt("caffeine");
			if (jsonObj.has("description"))
				description = jsonObj.getString("description");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
