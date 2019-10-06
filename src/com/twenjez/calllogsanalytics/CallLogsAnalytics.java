package com.twenjez.calllogsanalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.provider.CallLog;
import com.twenjez.calllogsanalytics.DatabaseHelper;
import com.google.i18n.phonenumbers.*;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;


public class CallLogsAnalytics extends Activity {
	PhoneNumberUtil phoneUtil=PhoneNumberUtil.getInstance();
	PhoneNumberToCarrierMapper p2c=PhoneNumberToCarrierMapper.getInstance();
	private ArrayList<CustomLog> clogs;
	public String[] queryStrings={"Call Logs","Day Summary","Week Summary","Person Summary","Carrier Summary","Call Type Summary","Carrier & Type"};
	int mc=0;
	
	DatabaseHelper dh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
			Log.i("MainActivity",mc++ + " onCreate");
        
		
		clogs=new ArrayList<CustomLog>();
        Log.i("MainActivity","clogs initialized");
        
        dh=new DatabaseHelper(this);
        Log.i("MainActivity","DB helper initialized");
        getCallDetails(this);
        Log.i("MainActivity","CallDetails Retrieved from phone");
        	
        setContentView(R.layout.activity_call_logs_analytics);
        Log.i("MainActivity","ContentView initialized");
        
        //TextView callLogs=(TextView)findViewById(R.id.callLogs);
        //callLogs.setText(getCallDetails(getBaseContext()));
        
        ListView l=(ListView)findViewById(R.id.queries);
        l.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,queryStrings));
        l.setOnItemClickListener(new OnItemClickListener(){
        	@Override
        	public void onItemClick(AdapterView<?> parent,View v, int position,long id){
        		Intent i=new Intent(CallLogsAnalytics.this,Reporting.class);
        		i.putExtra("report", position);
        		startActivity(i);
        	}}
        );
        
        /*
        l.setAdapter(new LogAdapter(R.layout.logitem,clogs));
        Log.i("MainActivity","Adapter added to listview");
        ListView l2=(ListView)findViewById(R.id.daySummary);
        l2.setAdapter(new summaryAdapter(dh.getDaySummary()));
        */
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("MainActivity",this.mc++ + " onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.call_logs_analytics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("MainActivity",mc++ + " onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void readCallLogs(){
		Log.i("MainActivity",mc++ + " readCallLogs");
    	//CallLog.Calls calls;
    }
    public static class CustomLog{
    	String name;
    	String number;
    	String type;
    	Date dtime;
    	int duration;

    	public CustomLog(){
    	}
    	public CustomLog(String n1,String n,String t, Date t2,int d2){
    		this.number=n1;
    		this.name=n;
    		this.type=t;
    		this.dtime=t2;
    		this.duration=d2;
    	}
    	@Override
    	public String toString(){
    		StringBuffer stringBuffer = new StringBuffer();
    		stringBuffer.append("\nPhone Number:--- " + number + " Call Type:--- "
                    + type + " Call Date:--- "+dtime
                    + " Call duration in sec :--- " + duration);
            stringBuffer.append("\n----------------------------------");
            return stringBuffer.toString();
    	}
    	
    }
    public CustomLog instance=new CallLogsAnalytics.CustomLog();
    
    class SummaryAdapter extends ArrayAdapter<String[]>{
    	ArrayList<String[]> data;
    	int flag;//for type of report
    	SummaryAdapter(Context context,ArrayList<String[]> summaries,int flag){
    		super(context,0,summaries);
    		data=summaries;
    		this.flag=flag;
    	}
    	SummaryAdapter(Context context,ArrayList<String[]> summaries){
    		super(context,0,summaries);
    		data=summaries;
    		flag=0;
    	}
    	public String[] getItem(int position){
    		return data.get(position);
    	}
    	@Override
    	public View getView(int p,View v,ViewGroup vg){
    		// Get the data item for this position
	       String[] s = getItem(p);    
	       // Check if an existing view is being reused, otherwise inflate the view
	       if (v == null) {
	    	   int layout=0;
	    	  switch(flag){
	    	  case 0:
	    		  layout=R.layout.calllog;
	    		  break;
	    	  case 1:
	    		  layout=R.layout.carrier_type;
	    		  break;
	    	  }
	          v = LayoutInflater.from(getContext()).inflate(layout, vg, false);
	       }
	       // Lookup view for data population
	       
	       switch(flag){
	       case 0:
	    	   TextView datetime = (TextView) v.findViewById(R.id.datetime);
		       TextView calls = (TextView) v.findViewById(R.id.calls);
		       TextView duration = (TextView) v.findViewById(R.id.duration);
		     
		       datetime.setText(s[0]);
		       calls.setText(s[1]);
		       duration.setText(timeString(Integer.parseInt(s[2])));
		       break;
	       case 1:
	    	   TextView carrier = (TextView) v.findViewById(R.id.carrier);
		       TextView out_calls = (TextView) v.findViewById(R.id.out_calls);
		       TextView out_duration = (TextView) v.findViewById(R.id.out_duration);
		       TextView in_calls = (TextView) v.findViewById(R.id.in_calls);
		       TextView in_duration = (TextView) v.findViewById(R.id.in_duration);
		       TextView mis_calls = (TextView) v.findViewById(R.id.mis_calls);
		       TextView rej_calls = (TextView) v.findViewById(R.id.rej_calls);
		       TextView blc_calls = (TextView) v.findViewById(R.id.blc_calls);
		       
		       carrier.setText(s[0]);
		       out_calls.setText(s[1]);
		       out_duration.setText(timeString(Integer.parseInt(s[2])));
		       in_calls.setText(s[3]);
		       in_duration.setText(timeString(Integer.parseInt(s[4])));
		       mis_calls.setText(s[5]);
		       rej_calls.setText(s[6]);
		       blc_calls.setText(s[7]);
	       }
	         
	       return v;
    	}
    	
    }
    public class LogAdapter extends ArrayAdapter<CustomLog>{
    	LogAdapter(Context context,int view,ArrayList<CustomLog> logs){
    		super(context,view,logs);
    		//Log.i("LogAdapter","Log Adapter Instantiated "+logs.toString());
    	}
    	@Override
    	public View getView(int p,View v,ViewGroup vg){
    		// Get the data item for this position
	       CustomLog log = getItem(p);    
	       // Check if an existing view is being reused, otherwise inflate the view
	       if (v == null) {
	    	   //Log.i("LogAdapter ","null view "+v);
	          v = LayoutInflater.from(vg.getContext()).inflate(R.layout.logitem, vg, false);
	       }
	       //Log.i("LogAdapter","reused view "+v);
	       // Lookup view for data population
	       TextView name = (TextView) v.findViewById(R.id.name);
	       TextView number = (TextView) v.findViewById(R.id.number);
	       TextView provider = (TextView) v.findViewById(R.id.provider);
	       TextView type = (TextView) v.findViewById(R.id.type);
	       TextView datetime = (TextView) v.findViewById(R.id.datetime);
	       TextView duration = (TextView) v.findViewById(R.id.duration);
	       // Populate the data into the template view using the data object
	       name.setText(log.name);
	       number.setText(log.number);
	       try{
	    	   PhoneNumber pNumber=phoneUtil.parse(log.number,"TZ");
	    	   String providerName=p2c.getNameForNumber(pNumber,Locale.ENGLISH)+pNumber.getRawInput();
	    	   //Log.i("MainActivity",Thread.getAllStackTraces().toString());
	    	   provider.setText(providerName);
	    	   Log.i("MainActivity",log.name+", "+log.number+", "+providerName);
	       }catch(Exception e){
	    	   Log.e("MainActivity",e.getLocalizedMessage());
	       }
	 
	       type.setText(log.type);
	       datetime.setText(""+log.dtime);
	       duration.setText(timeString(log.duration));
	       // Return the completed view to render on screen
	       //Log.i("LogAdapter","Loading view "+p);
	       return v;
    	}
    }
    private void getCallDetails(Context context) {
        Log.i("MainActivity",mc++ + " getCallDetails");
        //Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
          //      null, null, null, CallLog.Calls.DATE + " DESC");
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                new String[]{
        			//CallLog.Calls._ID,
        			CallLog.Calls.CACHED_NAME,
        			CallLog.Calls.NUMBER,
        			CallLog.Calls.TYPE,
        			CallLog.Calls.DATE,
        			CallLog.Calls.DURATION//,CallLog.Calls.VIA_NUMBER
        		}, null, null, CallLog.Calls.DATE + " ASC");
        dh.loadLogs(cursor);
		Log.i("MainActivity","Call details saved to db");   
    }
    public String timeString(int sec){
    	int s=sec%60;
    	int m=sec/60;
    	int h=m/60;m%=60;
    	return (((h>0) ? h+"h ":"")+((h>0||m>0) ? m+"m ":"")+s+"s");
    	
    }
    
}
