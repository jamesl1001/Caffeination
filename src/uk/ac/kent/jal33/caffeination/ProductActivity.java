package uk.ac.kent.jal33.caffeination;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProductActivity extends BaseActivity {
	private Product product;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
        stub.setLayoutResource(R.layout.activity_product);
        stub.inflate();
        
        Context context = this;
        
        Intent intent = getIntent();
        
		product = new Product();
		
		CaffeinationApp app = (CaffeinationApp) getApplication();
		
		product.setProductId(intent.getIntExtra(MainActivity.SELECTED_PRODUCT_ID, 1));
		product.setProductName(intent.getStringExtra(MainActivity.SELECTED_PRODUCT_NAME));
		product.setSize(intent.getIntExtra(MainActivity.SELECTED_PRODUCT_SIZE, 1));
		product.setCaffeine(intent.getIntExtra(MainActivity.SELECTED_PRODUCT_CAFFEINE, 1));
		product.setPrice(intent.getDoubleExtra(MainActivity.SELECTED_PRODUCT_PRICE, 1));

		DecimalFormat df = new DecimalFormat("0.00");
		
		TextView productName     = (TextView) findViewById(R.id.product_name);
		TextView productSize     = (TextView) findViewById(R.id.product_size);
		TextView productCaffeine = (TextView) findViewById(R.id.product_caffeine);
		TextView productPrice    = (TextView) findViewById(R.id.product_price);
		
		productName.setText(product.getProductName());
		productSize.setText(product.getSize() + "ml");
		productCaffeine.setText(product.getCaffeine() + "mg");
		productPrice.setText("£"+df.format(product.getPrice()));
		
		// Get image - resize for height: 480px
		ImageView imgView = (ImageView) findViewById(R.id.product_image);
		ProgressBar progBar = (ProgressBar) findViewById(R.id.product_image_progress);
		product.loadImage(app, imgView, 0, 480, progBar);
		
		product.loadDetails(this, context);
		
		// Add to cart event handler
		final Button button = (Button) findViewById(R.id.add_to_cart);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	CaffeinationApp app = (CaffeinationApp) getApplication();
		    	EditText product_quantity_value = (EditText) findViewById(R.id.product_quantity_value);
		    	int quantityValue = Integer.parseInt(product_quantity_value.getText().toString());
		    	
		    	boolean existsInCart = app.getMatch(product.getProductId(), 1);
		    	
		    	if(existsInCart) {
			    	int row = app.getRow(product.getProductId(), 1);
		    		int quantityBefore = app.quan.get(row);
		    		app.quan.set(row, quantityBefore + quantityValue);
		    		int quantityAfter = app.quan.get(row);
		    		Toast.makeText(getBaseContext(), "Added to cart ("+quantityAfter+")", Toast.LENGTH_SHORT).show();
		    	} else {
		    		app.cart.add(product);
		    		app.quan.add(quantityValue);
		    		Toast.makeText(getBaseContext(), "Added to cart (" + quantityValue + ")", Toast.LENGTH_SHORT).show();
		    	}
			}
		});
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_favourites:
	    	CaffeinationApp app  = (CaffeinationApp) getApplication();
	    	boolean existsInFavs = app.getMatch(product.getProductId(), 2);
	    	int row              = app.getRow(product.getProductId(), 2);
			Vibrator v           = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			if (existsInFavs) {
				app.favs.remove(row);
				app.saveFavs();
				item.setIcon(R.drawable.ic_action_favorite_plus);
				Toast.makeText(getBaseContext(), "Removed from favourites", Toast.LENGTH_SHORT).show();
				v.vibrate(100);
			} else {
				app.favs.add(product);
				app.saveFavs();
				item.setIcon(R.drawable.ic_action_favorite_minus);
				Toast.makeText(getBaseContext(), "Added to favourites", Toast.LENGTH_SHORT).show();
				v.vibrate(100);
			}
			
			return true;
		}
        return super.onOptionsItemSelected(item);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.product, menu);
		
    	CaffeinationApp app  = (CaffeinationApp) getApplication();
    	boolean existsInFavs = app.getMatch(product.getProductId(), 2);
	    MenuItem item        = menu.findItem(R.id.action_favourites);
    	
		if (existsInFavs) {
			item.setIcon(R.drawable.ic_action_favorite_minus);
		} else {
			item.setIcon(R.drawable.ic_action_favorite_plus);
		}
    	
		return true;
	}

}
