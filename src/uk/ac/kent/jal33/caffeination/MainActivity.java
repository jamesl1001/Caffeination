package uk.ac.kent.jal33.caffeination;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends BaseActivity {

	public static int FEATURED                           = 0;
	
	public final static String SELECTED_CATEGORY         = "uk.ac.kent.jal33.Caffeination.selectedCategory";

	public final static String SELECTED_PRODUCT_ID       = "uk.ac.kent.jal33.Caffeination.productId";
	public final static String SELECTED_PRODUCT_NAME     = "uk.ac.kent.jal33.Caffeination.productName";
	public final static String SELECTED_PRODUCT_SIZE     = "uk.ac.kent.jal33.Caffeination.productSize";
	public final static String SELECTED_PRODUCT_CAFFEINE = "uk.ac.kent.jal33.Caffeination.productCaffeine";
	public final static String SELECTED_PRODUCT_PRICE    = "uk.ac.kent.jal33.Caffeination.productPrice";

	public final static String CHECKOUT_TOTAL            = "uk.ac.kent.jal33.Caffeination.checkoutTotal";
	
	public final static String WEBSERVER_URL             = "http://jal33.student.eda.kent.ac.uk/Caffeination/";
	public final static String WEBSERVER_GETFEATURED     = WEBSERVER_URL + "getFeatured.php";
	public final static String WEBSERVER_GETLIST         = WEBSERVER_URL + "getProductsByCategory.php?cat=%s&sort=%s&by=%s&min=%d&max=%d";
	public final static String WEBSERVER_GETIMAGE        = WEBSERVER_URL + "getImage.php?id=%d&w=%d&h=%d&str=%d";
	public final static String WEBSERVER_GETDETAILS      = WEBSERVER_URL + "getProductById.php?id=%d";
	public final static String WEBSERVER_PUTORDER        = WEBSERVER_URL + "putOrder.php?ord=%s&n=%s&h=%s&p=%s&d=%s";
	public final static String WEBSERVER_SENDEMAIL       = WEBSERVER_URL + "sendEmail.php?e=%s";
	
	public final static String WEBSERVER_GETFAVS         = WEBSERVER_URL + "getFavourites.php?f=%s";
	
	public Product product;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
        stub.setLayoutResource(R.layout.activity_main);
        stub.inflate();

		CaffeinationApp app = (CaffeinationApp) getApplication();

		RelativeLayout featured = (RelativeLayout) findViewById(R.id.featured);
        Button drinksButton     = (Button) findViewById(R.id.button_drinks);
    	Button shotsButton      = (Button) findViewById(R.id.button_shots);
    	Button favouritesButton = (Button) findViewById(R.id.button_favourites);
    	Button cartButton       = (Button) findViewById(R.id.button_cart);
        
    	// Handle animations
    	Animation animateFeatured     = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_left);
    	Animation animateDrinksButton = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.drinks_button);
    	Animation animateShotsButton  = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shots_button);
    	Animation animateFavsButton   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.favs_button);
    	Animation animateCartButton   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.cart_button);

    	featured.startAnimation(animateFeatured);
        drinksButton.startAnimation(animateDrinksButton);
        shotsButton.startAnimation(animateShotsButton);
        favouritesButton.startAnimation(animateFavsButton);
        cartButton.startAnimation(animateCartButton);
        
        // Check connectivity status
	    ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = conn.getActiveNetworkInfo();
	    
	    if ((networkInfo != null) && networkInfo.isConnected()) {
	        // Load featured product
	        product = new Product();
	        product.getFeatured(this, app);
	    	
	        // Load favourites from SharedPreferences
	        loadFavourites();
	    }
        
        // Click handlers
    	drinksButton.setOnClickListener(new OnClickListener() {
    		public void onClick(final View v) {
    			Intent intent = new Intent(getBaseContext(), ProductListActivity.class);
    	    	intent.putExtra(MainActivity.SELECTED_CATEGORY, "drink");
    	    	startActivity(intent);
    		}
    	});
    	
    	shotsButton.setOnClickListener(new OnClickListener() {
    		public void onClick(final View v) {
    			Intent intent = new Intent(getBaseContext(), ProductListActivity.class);
    	    	intent.putExtra(MainActivity.SELECTED_CATEGORY, "shot");
    	    	startActivity(intent);
    		}
    	});
    	
    	favouritesButton.setOnClickListener(new OnClickListener() {
    		public void onClick(final View v) {
    			Intent intent = new Intent(getBaseContext(), FavouritesActivity.class);
    	    	startActivity(intent);
    		}
    	});
    	
    	cartButton.setOnClickListener(new OnClickListener() {
    		public void onClick(final View v) {
    			Intent intent = new Intent(getBaseContext(), CartActivity.class);
    	    	startActivity(intent);
    		}
    	});
    }
	
	public void loadFavourites() {
    	CaffeinationApp app = (CaffeinationApp) getApplication();
		String favsString = app.loadFavs();
        FavouritesLoader loader;
        loader = new FavouritesLoader(this, app);
        loader.execute(String.format(MainActivity.WEBSERVER_GETFAVS, favsString));
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    

}
