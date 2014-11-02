package uk.ac.kent.jal33.caffeination;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FavouritesActivity extends BaseActivity {

	public class FavouritesListAdapter extends BaseAdapter {
		private final Context context;
		private List<Product> itemList;
		
		public FavouritesListAdapter(Context c) {
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
				cell = inflater.inflate(R.layout.adapter_favourites, parent, false);
			}
			
			Product p = itemList.get(position);
			
			TextView name       = (TextView) cell.findViewById(R.id.product_list_name);
			ImageView image     = (ImageView) cell.findViewById(R.id.product_list_image);
			ProgressBar progBar = (ProgressBar) cell.findViewById(R.id.product_list_image_progress);
			
			name.setText(p.getProductName());
			image.setImageDrawable(null);
			p.loadImage(app, image, 54, 54, progBar);
			
			Button btn_remove = (Button) cell.findViewById(R.id.cart_button_remove);
			btn_remove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CaffeinationApp app = (CaffeinationApp)getApplication();
					Object toRemove = adapter.getItem(pos);
					app.favs.remove(toRemove);
					app.saveFavs();
					adapter.notifyDataSetChanged();
				}
			});
			
			return cell;
		}

	}
	
	private FavouritesListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
        stub.setLayoutResource(R.layout.activity_favourites);
        stub.inflate();

		adapter = new FavouritesListAdapter(this);
		ListView favsList = (ListView) findViewById(R.id.favourites_list);
		adapter.setItemList(((CaffeinationApp)getApplication()).favs);
		favsList.setAdapter(adapter);
		
		if(adapter.getCount() == 0) {
			Toast.makeText(getBaseContext(), "You don't have any favourites yet :(", Toast.LENGTH_SHORT).show();
		}
		
		favsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				Product p = (Product)adapter.getAdapter().getItem(position);
				Intent intent = new Intent(getBaseContext(), ProductActivity.class);
				intent.putExtra(MainActivity.SELECTED_PRODUCT_ID, p.getProductId());
				intent.putExtra(MainActivity.SELECTED_PRODUCT_NAME, p.getProductName());
				intent.putExtra(MainActivity.SELECTED_PRODUCT_SIZE, p.getSize());
				intent.putExtra(MainActivity.SELECTED_PRODUCT_CAFFEINE, p.getCaffeine());
				intent.putExtra(MainActivity.SELECTED_PRODUCT_PRICE, p.getPrice());
				startActivity(intent);
			}
        	
        });
	}
	
	

	@Override
	protected void onResume() {
		adapter.notifyDataSetChanged();
		super.onResume();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
