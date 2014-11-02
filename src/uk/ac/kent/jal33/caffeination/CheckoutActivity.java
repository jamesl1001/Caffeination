package uk.ac.kent.jal33.caffeination;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CheckoutActivity extends BaseActivity {

	AlertDialog.Builder builder;
	TextView checkoutTotal;
	EditText checkoutName;
	EditText checkoutHouseNumber;
	EditText checkoutPostcode;
	EditText checkoutEmail;
	DatePicker checkoutDate;
	Button checkout_btn;
	Button home_btn;
	Context context = this;
	
	String checkoutNameString;
	String checkoutHouseNumberString;
	String checkoutPostcodeString;
	String checkoutEmailString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
        stub.setLayoutResource(R.layout.activity_checkout);
        stub.inflate();

    	builder             = new AlertDialog.Builder(this);
    	checkoutTotal       = (TextView) findViewById(R.id.checkout_total);
    	checkoutName        = (EditText) findViewById(R.id.checkout_name);
    	checkoutHouseNumber = (EditText) findViewById(R.id.checkout_house_number);
    	checkoutPostcode    = (EditText) findViewById(R.id.checkout_postcode);
    	checkoutEmail       = (EditText) findViewById(R.id.checkout_email);
    	checkoutDate        = (DatePicker) findViewById(R.id.checkout_date);
    	checkout_btn        = (Button) findViewById(R.id.checkout_button);
    	home_btn            = (Button) findViewById(R.id.checkout_home);
    	
    	checkoutName.requestFocus();
    	
    	Intent intent = getIntent();
        String total = intent.getStringExtra(MainActivity.CHECKOUT_TOTAL);
        checkoutTotal.setText("Total price: " + total);

//		checkoutDate.setMinDate(System.currentTimeMillis() - 1000);
//		checkoutDate.setMaxDate(System.currentTimeMillis() + 86400*2);

		checkout_btn.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				checkoutNameString        = checkoutName.getText().toString().trim();
				checkoutHouseNumberString = checkoutHouseNumber.getText().toString().trim();
				checkoutPostcodeString    = checkoutPostcode.getText().toString().trim();
				checkoutEmailString       = checkoutEmail.getText().toString().trim();
				
				if(checkoutNameString.equals("") || checkoutHouseNumberString.equals("") || checkoutPostcodeString.equals("") || checkoutEmailString.equals("")) {    
					Toast.makeText(getBaseContext(), "Please fill out all the fields", Toast.LENGTH_SHORT).show();
				} else {
					if(checkoutPostcodeString.matches("^[a-zA-Z]{1,2}[0-9][0-9A-Za-z]{0,1} {0,1}[0-9][A-Za-z]{2}$")) {
						if(Patterns.EMAIL_ADDRESS.matcher(checkoutEmailString).matches()) {
							AlertDialog alert = builder.create();
							alert.show();
						} else {
							Toast.makeText(getBaseContext(), "The email you have entered is not a valid format", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getBaseContext(), "The postcode you have entered is not a valid format", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		home_btn.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				Intent intent = new Intent(getBaseContext(), MainActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    	startActivity(intent);
			}
		});
		
		builder.setMessage("Are you sure you want to send this order?")
		       .setPositiveButton("Yes",
		    		new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							checkout_btn.setEnabled(false);
							
							int day   = checkoutDate.getDayOfMonth();
						    int month = checkoutDate.getMonth() + 1;
						    int year  = checkoutDate.getYear();
						    
						    String checkoutDateString = day + "-" + month + "-" + year;
							
						    checkoutOrder(checkoutNameString.replace(" ", "_"), checkoutHouseNumberString, checkoutPostcodeString.replace(" ", ""), checkoutDateString);
							sendEmail(checkoutEmailString);
						}
					})
		       .setNegativeButton("No",
		    		new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
	}
	
	protected void checkoutOrder(String name, String houseNumber, String postcode, String date) {
		String url = String.format(MainActivity.WEBSERVER_PUTORDER, ((CaffeinationApp)getApplication()).getOrderString(), name, houseNumber, postcode, date);
		
		AsyncTask<String, Void, JSONObject> task = new AsyncTask<String, Void, JSONObject>() {
			protected ProgressDialog progressDialog;
			
			@Override
			protected void onPreExecute() {
				progressDialog = new ProgressDialog(context);
				progressDialog.setTitle("Please wait");
				progressDialog.setMessage("Submitting your order");
				progressDialog.show();
			}
			
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
				Button home_btn = (Button) findViewById(R.id.checkout_home);
				
				if (progressDialog != null) {
					progressDialog.dismiss();
				}

				if (result != null) {
		    		Toast.makeText(getBaseContext(), "Your purchase has been successfully submitted", Toast.LENGTH_SHORT).show();
					((CaffeinationApp)getApplication()).cart.clear();
				} else {
		    		Toast.makeText(getBaseContext(), "An error has occurred", Toast.LENGTH_SHORT).show();
					checkout_btn.setEnabled(true);
				}
				home_btn.setEnabled(true);
			}
			
		};
		task.execute(url);
	}
	
	protected void sendEmail(String email) {
		String url = String.format(MainActivity.WEBSERVER_SENDEMAIL, email);
		
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(String... params) {
				try {
					URL u = new URL(params[0]);
					HttpURLConnection conn = (HttpURLConnection) u.openConnection();
					conn.connect();
					if (conn.getResponseCode() != 200)
						return false;
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return true;
			}
			
		};
		task.execute(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
