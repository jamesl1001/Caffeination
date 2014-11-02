package uk.ac.kent.jal33.caffeination;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class CaffeinationApp extends Application {
	
	LruCache<String, Bitmap> mMemoryCache;

	public List<Product> cart = new ArrayList<Product>();
	public List<Integer> quan = new ArrayList<Integer>();
	public List<Product> favs = new ArrayList<Product>();
	
	public int getRow(int id, int which) {
		// which == 1 -> cart
		// which == 2 -> favs
		int r = 0;

		if(which == 1) {
			for (int i=0; i < cart.size(); i++) {
				Product p = cart.get(i);
				if (p.getProductId() == id)
					r = i;
			}
		} else {
			for (int i=0; i < favs.size(); i++) {
				Product p = favs.get(i);
				if (p.getProductId() == id)
					r = i;
			}
		}
		
		return r;
	}
	
	public boolean getMatch(int id, int which) {
		// which == 1 -> cart
		// which == 2 -> favs
		if(which == 1) {
			for (int i=0; i < cart.size(); i++) {
				Product p = cart.get(i);
				if (p.getProductId() == id)
					return true;
			}
		} else {
			for (int i=0; i < favs.size(); i++) {
				Product p = favs.get(i);
				if (p.getProductId() == id)
					return true;
			}
		}
		return false;
	}

	public double getCartTotal() {
		double total = 0;
		
		for (int i=0; i < cart.size(); i++) {
			Product p = cart.get(i);
			int q = quan.get(i);
			total += p.getPrice() * q;
		}
		
		return total;
	}
	
	public String getOrderString() {
		String order = new String();
		for (int i=0; i < cart.size(); i++) {
			Product p = cart.get(i);
			int q = quan.get(i);
			String productName = p.getProductName().replace(" ", "_");
			order += p.getProductId() + "_" + productName + "_x" + q + ";";
		}
		return order;
	}
	
	public String getFavsString() {
		String s = new String();
		for (int i=0; i < favs.size(); i++) {
			Product p = favs.get(i);
			s += p.getProductId() + ",";
		}
		return s;
	}
	
	public String loadFavs() {
		SharedPreferences settings = getSharedPreferences("CaffeinationPrefs", 0);
		String favsString = settings.getString("favs", "");
		return favsString;
	}
	
	public void saveFavs() {
    	String favsString = getFavsString();
		SharedPreferences settings = getSharedPreferences("CaffeinationPrefs", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("favs", favsString);
		editor.commit();
	}
	
	public int getQuantity(int id) {
		int q = 0;
		
		for (int i=0; i < cart.size(); i++) {
			Product p = cart.get(i);
			if (p.getProductId() == id)
				q++;
		}
		
		return q;
	}

	@Override
	public void onCreate() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
		super.onCreate();
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}
	
	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

}
