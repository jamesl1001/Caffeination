package uk.ac.kent.jal33.caffeination;

import java.text.DecimalFormat;
import java.util.List;
import com.edmodo.rangebar.RangeBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProductListActivity extends BaseActivity {
    
    private String selectedCategory;
	private int loadedDrinks;
	private int loadedShots;
    private ProductListAdapter adapter;
    private ListViewLoader loader;
    final Context context = this;

    int settingsFilterSortBy;
    int settingsFilterOrder;
    int settingsFilterSortByIdx;
    int settingsFilterOrderIdx;
    int settingsFilterRangebarLeft;
    int settingsFilterRangebarRight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
        stub.setLayoutResource(R.layout.activity_product_list);
        stub.inflate();
        
        loadedDrinks = 0;
        loadedShots  = 0;
        
        Intent intent = getIntent();
        selectedCategory = intent.getStringExtra(MainActivity.SELECTED_CATEGORY);
        
		// Change the title of the activity according to the selected category
        setTitle(selectedCategory.substring(0,1).toUpperCase() + selectedCategory.substring(1) + "s");
        
        getFilterPreferences();
        
        adapter = new ProductListAdapter(this);
        ListView listView = (ListView) findViewById(R.id.product_list);
        listView.setAdapter(adapter);
        
        loader = new ListViewLoader(adapter, selectedCategory, this);
        
        if(filterSettingsExist()) {
        	loader.execute(String.format(MainActivity.WEBSERVER_GETLIST, selectedCategory, getFilterOrder(settingsFilterOrderIdx), getFilterSortBy(settingsFilterSortByIdx), settingsFilterRangebarLeft*10, settingsFilterRangebarRight*10));
        } else {
        	loader.execute(String.format(MainActivity.WEBSERVER_GETLIST, selectedCategory, "ASC", "name", 0, 500));
        }
        
        listView.setOnItemClickListener(new OnItemClickListener() {

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
	
	public class ProductListAdapter extends BaseAdapter {
        private final Context context;
        private List<Product> itemList;
        
        public ProductListAdapter(Context c) {
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
        	CaffeinationApp app = (CaffeinationApp) getApplication();
            
            if (cell == null) {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                cell = inflater.inflate(R.layout.adapter_product_list, parent, false);
            }
            
            Product p = itemList.get(position);
            
            RelativeLayout list = (RelativeLayout) cell.findViewById(R.id.product_list);
            TextView name       = (TextView) cell.findViewById(R.id.product_list_name);
            TextView price      = (TextView) cell.findViewById(R.id.product_list_price);
            TextView size       = (TextView) cell.findViewById(R.id.product_list_size);
            TextView caffeine   = (TextView) cell.findViewById(R.id.product_list_caffeine);
            ImageView image     = (ImageView) cell.findViewById(R.id.product_list_image);
			ProgressBar progBar = (ProgressBar) cell.findViewById(R.id.product_list_image_progress);
			
    		DecimalFormat df = new DecimalFormat("0.00");

            name.setText(p.getProductName());
            price.setText("£"+df.format(p.getPrice()));
            size.setText(p.getSize()+"ml");
            caffeine.setText(p.getCaffeine()+"mg");
			image.setImageDrawable(null);
            p.loadImage(app, image, 54, 54, progBar);
            
            // Animate items slide in in only once onCreate()
            // This is to prevent the items from sliding in when the ListView is scrolled
            // due to items being recycled
            if(selectedCategory == "drink") {
            	// The value 7 is a little cheat because I know how big the screen is
            	// Because getLastVisiblePosition() always seems to return -1, even if I place it in onPostExecute() 
	            if(loadedDrinks < 7) {
		        	Animation animateList = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_left);
		        	animateList.setStartOffset(position*50);
		        	list.startAnimation(animateList);
	            }
	            
	        	loadedDrinks++;
            } else {
            	if(loadedShots < 7) {
		        	Animation animateList = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_left);
		        	animateList.setStartOffset(position*50);
		        	list.startAnimation(animateList);
	            }
	            
	        	loadedShots++;
            }
        	
            return cell;
        }
    }
	
	public void getFilterPreferences() {
		SharedPreferences settings  = getSharedPreferences("CaffeinationPrefs", 0);
		settingsFilterSortBy        = settings.getInt("filterSortBy", 0);
        settingsFilterOrder         = settings.getInt("filterOrder", 0);
		settingsFilterSortByIdx     = settings.getInt("filterSortByIdx", -1);
        settingsFilterOrderIdx      = settings.getInt("filterOrderIdx", -1);
        settingsFilterRangebarLeft  = settings.getInt("filterRangebarLeft", -1);
        settingsFilterRangebarRight = settings.getInt("filterRangebarRight", -1);
	}
	
	public void showFilterDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflator = getLayoutInflater();
		final View layout = inflator.inflate(R.layout.dialog_filter, null);

		final RadioGroup filterSortByGroup = (RadioGroup) layout.findViewById(R.id.filter_sort_by_group);
		final RadioGroup filterOrderGroup  = (RadioGroup) layout.findViewById(R.id.filter_order_group);
		final RangeBar rangebar            = (RangeBar) layout.findViewById(R.id.filter_rangebar);
        final TextView filterRangebarLeft  = (TextView) layout.findViewById(R.id.filter_rangebar_left);
        final TextView filterRangebarRight = (TextView) layout.findViewById(R.id.filter_rangebar_right);
        
        rangebar.setTickCount(51);
        
        getFilterPreferences();

        if(filterSettingsExist()) {
        	filterSortByGroup.check(settingsFilterSortBy);
        	filterOrderGroup.check(settingsFilterOrder);
        	rangebar.setThumbIndices(settingsFilterRangebarLeft, settingsFilterRangebarRight);
        	filterRangebarLeft.setText(""+settingsFilterRangebarLeft*10);
        	filterRangebarRight.setText(""+settingsFilterRangebarRight*10);
        } else {
        	filterSortByGroup.check(R.id.filter_sort_by_name);
        	filterOrderGroup.check(R.id.filter_order_asc);
        	rangebar.setThumbIndices(0, 50);
        }
        
        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) { 
            	filterRangebarLeft.setText(""+rangebar.getLeftIndex()*10); 
            	filterRangebarRight.setText(""+rangebar.getRightIndex()*10);
            }
        });
		
		builder.setView(layout)
		       .setTitle("Filter products")
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                int sortBySelected           = filterSortByGroup.getCheckedRadioButtonId();
                View sortBySelectedButton    = filterSortByGroup.findViewById(sortBySelected);
                int sortByIdx                = filterSortByGroup.indexOfChild(sortBySelectedButton);
                
                int orderSelected            = filterOrderGroup.getCheckedRadioButtonId();
                View orderSelectedButton     = filterOrderGroup.findViewById(orderSelected);
                int orderIdx                 = filterOrderGroup.indexOfChild(orderSelectedButton);

                int rangebarLeft             = rangebar.getLeftIndex() * 10;
                int rangebarRight            = rangebar.getRightIndex() * 10;

        		// Remember filter in SharedPreferences
        		SharedPreferences settings = getSharedPreferences("CaffeinationPrefs", 0);
        		SharedPreferences.Editor editor = settings.edit();
        		editor.putInt("filterSortBy", sortBySelected);
        		editor.putInt("filterOrder", orderSelected);
        		editor.putInt("filterSortByIdx", sortByIdx);
        		editor.putInt("filterOrderIdx", orderIdx);
        		editor.putInt("filterRangebarLeft", rangebarLeft/10);
        		editor.putInt("filterRangebarRight", rangebarRight/10);
        		editor.apply();

            	dialogListener(orderIdx, sortByIdx, rangebarLeft, rangebarRight);
            }
        })
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });
		
		AlertDialog dialog = builder.create();
		dialog.show();
		
	}
	
	public void dialogListener(int orderIdx, int sortByIdx, int rangebarLeft, int rangebarRight) {
		String order  = getFilterOrder(orderIdx);
		String sortBy = getFilterSortBy(sortByIdx);
		
		loader = new ListViewLoader(adapter, selectedCategory, this);
        loader.execute(String.format(MainActivity.WEBSERVER_GETLIST, selectedCategory, order, sortBy, rangebarLeft, rangebarRight));
	}
	
	public boolean filterSettingsExist() {
		return (settingsFilterSortBy != 0 && settingsFilterOrder != 0 && settingsFilterSortByIdx != -1 && settingsFilterOrderIdx != -1 && settingsFilterRangebarLeft != -1 && settingsFilterRangebarRight != -1) ? true : false;
	}
	
	public String getFilterOrder(int orderIdx) {
		String order = null;
		switch(orderIdx) {
			case 0:
				order = "ASC";
				break;
			case 1:
				order = "DESC";
				break;
		}
		return order;
	}
	
	public String getFilterSortBy(int sortByIdx) {
		String sortBy = null;
		switch(sortByIdx) {
			case 0:
			    sortBy = "name";
			    break;
			case 1:
				sortBy = "price";
			    break;
			case 2:
			    sortBy = "size";
			    break;
			case 3:
				sortBy = "caffeine";
			    break;
		}
		return sortBy;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.product_list, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_filter:
			this.showFilterDialog();
			return true;
		}
        return super.onOptionsItemSelected(item);
    }

}
