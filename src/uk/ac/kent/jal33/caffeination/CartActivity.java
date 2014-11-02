package uk.ac.kent.jal33.caffeination;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CartActivity extends BaseActivity {
	TextView subtotal;
	DecimalFormat df = new DecimalFormat("0.00");

	public class CartListAdapter extends BaseAdapter {
		private final Context context;
		private List<Product> itemList;
		
		public CartListAdapter(Context c) {
			context = c;
		}

		@Override
		public int getCount() {
			if (itemList == null) return 0;
			else return itemList.size();
		}

		@Override
		public Object getItem(int position) {
			if (itemList == null) return null;
			else return itemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			if (itemList == null) return 0;
			else return itemList.get(position).hashCode();
		}

		public List<Product> getItemList() {
			return itemList;
		}

		public void setItemList(List<Product> itemList) {
			this.itemList = itemList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View cell = convertView;
			final int pos = position;
			CaffeinationApp app = (CaffeinationApp) getApplication();
			
			if(cell == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				cell = inflater.inflate(R.layout.adapter_cart, parent, false);
			}
			
			final Product p = itemList.get(position);
			
			TextView name       = (TextView) cell.findViewById(R.id.product_list_name);
			TextView price      = (TextView) cell.findViewById(R.id.product_list_price);
			TextView quantity   = (TextView) cell.findViewById(R.id.product_list_quantity);
			ImageView image     = (ImageView) cell.findViewById(R.id.product_list_image);
			ProgressBar progBar = (ProgressBar) cell.findViewById(R.id.product_list_image_progress);
			Button btn_remove   = (Button) cell.findViewById(R.id.cart_button_remove);
			Button btn_minus    = (Button) cell.findViewById(R.id.product_list_quantity_minus);

			name.setText(p.getProductName());
			price.setText("£" + df.format(p.getPrice()));
			quantity.setText(""+app.quan.get(position));
			image.setImageDrawable(null);
			p.loadImage(app, image, 54, 54, progBar);
			
			if(app.quan.get(position) <= 1) {
				btn_minus.setEnabled(false);
			} else {
				btn_minus.setEnabled(true);
			}
			
			btn_remove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CaffeinationApp app = ((CaffeinationApp)getApplication());
					app.cart.remove(pos);
					app.quan.remove(pos);
					adapter.notifyDataSetChanged();
			        updateCheckoutButtonState();
					resetSubTotal();
				}
			});
			
			btn_minus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CaffeinationApp app = ((CaffeinationApp)getApplication());
			    	int row = app.getRow(p.getProductId(), 1);
		    		int quantityBefore = app.quan.get(row);
		    		app.quan.set(row, quantityBefore - 1);
					adapter.notifyDataSetChanged();
					resetSubTotal();
				}
			});
			
			Button btn_add = (Button) cell.findViewById(R.id.product_list_quantity_plus);
			btn_add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CaffeinationApp app = ((CaffeinationApp)getApplication());
			    	int row = app.getRow(p.getProductId(), 1);
		    		int quantityBefore = app.quan.get(row);
		    		app.quan.set(row, quantityBefore + 1);
					adapter.notifyDataSetChanged();
					resetSubTotal();
				}
			});
			
			return cell;
		}

	}
	
	CartListAdapter adapter;
	
	public void updateCheckoutButtonState() {
    	CaffeinationApp app = (CaffeinationApp) getApplication();
		Button btn_checkout = (Button) findViewById(R.id.cart_button_checkout);
		
		if(app.cart.isEmpty()) {
			btn_checkout.setEnabled(false);
		} else {
			btn_checkout.setEnabled(true);
		}
	}
	
	public void resetSubTotal() {
		subtotal.setText("£"+df.format(((CaffeinationApp)getApplication()).getCartTotal()));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
        stub.setLayoutResource(R.layout.activity_cart);
        stub.inflate();

		subtotal = (TextView) findViewById(R.id.cart_text_subtotal);
		
        updateCheckoutButtonState();
        resetSubTotal();
		
		adapter = new CartListAdapter(this);
		ListView cartList = (ListView) findViewById(R.id.cart_list);
		adapter.setItemList(((CaffeinationApp)getApplication()).cart);
		cartList.setAdapter(adapter);
		
		Button btn = (Button) findViewById(R.id.cart_button_checkout);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				Intent intent = new Intent(getBaseContext(), CheckoutActivity.class);
    	    	intent.putExtra(MainActivity.CHECKOUT_TOTAL, subtotal.getText());
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
