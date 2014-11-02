package uk.ac.kent.jal33.caffeination;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BaseActivity extends Activity {
	
	public static boolean CONN_STATUS = false;
    
    public class NetworkReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	    ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	    NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        	TextView notConnectedText = (TextView) findViewById(R.id.not_connected);

            if ((networkInfo != null) && networkInfo.isConnected()) {
            	CONN_STATUS = true;
            	notConnectedText.setVisibility(View.GONE);
            	// enable the drawer
                getActionBar().setHomeButtonEnabled(true);
                getActionBar().setDisplayHomeAsUpEnabled(true);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            } else {
            	CONN_STATUS = false;
            	notConnectedText.setVisibility(View.VISIBLE);
            	// disable the drawer
                getActionBar().setHomeButtonEnabled(false);
                getActionBar().setDisplayHomeAsUpEnabled(false);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
    	}
    }
    
    NetworkReceiver receiver;

	private String[] drawerListViewItems;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // get list items from strings.xml
        drawerListViewItems = getResources().getStringArray(R.array.items);
        // get ListView defined in activity_main.xml
        drawerListView = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerListView.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_listview_item, drawerListViewItems));

        // 2. App Icon 
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // 2.1 create ActionBarDrawerToggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,                 /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description */
                R.string.drawer_close /* "close drawer" description */
                );

        // 2.2 Set actionBarDrawerToggle as the DrawerListener
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        // 2.3 enable and show "up" arrow
        getActionBar().setDisplayHomeAsUpEnabled(true);

        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
      
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		receiver = new NetworkReceiver();
		this.registerReceiver(receiver, filter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
         actionBarDrawerToggle.syncState();
    }

    @Override
	protected void onDestroy() {
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // call ActionBarDrawerToggle.onOptionsItemSelected(), if it returns true
        // then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            drawerListView.setItemChecked(position, true);
            drawerLayout.closeDrawer(drawerListView);
            
            Intent intent = null;
            switch (position) {
            case 0:
            	intent = new Intent(getApplicationContext(), MainActivity.class);
            	break;
            case 1:
            	intent = new Intent(getApplicationContext(), ProductListActivity.class);
    	    	intent.putExtra(MainActivity.SELECTED_CATEGORY, "drink");
            	break;
            case 2:
            	intent = new Intent(getApplicationContext(), ProductListActivity.class);
    	    	intent.putExtra(MainActivity.SELECTED_CATEGORY, "shot");
            	break;
            case 3:
            	intent = new Intent(getApplicationContext(), FavouritesActivity.class);
            	break;
            case 4:
            	intent = new Intent(getApplicationContext(), CartActivity.class);
            	break;
            }

	        if (intent != null) {
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivity(intent);
	        }
        }
    }

}
