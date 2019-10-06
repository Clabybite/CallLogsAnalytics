package com.twenjez.calllogsanalytics;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Date;

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
import com.twenjez.calllogsanalytics.*;
import com.twenjez.calllogsanalytics.CallLogsAnalytics.CustomLog;
import com.twenjez.calllogsanalytics.CallLogsAnalytics.LogAdapter;
import com.twenjez.calllogsanalytics.CallLogsAnalytics.SummaryAdapter;



public class Reporting extends Activity {
	CallLogsAnalytics cla;
	ArrayList<CustomLog> clogs;
	TextView rTitle;
	ListAdapter rItems;
	
	int mc=0;
	
	DatabaseHelper dh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
			Log.i("ReportActivity",mc++ + " onCreate");
		
			int position=getIntent().getIntExtra("report",-1);
			String itemDay=getIntent().getStringExtra("itemDay");
			String itemPerson=getIntent().getStringExtra("itemPerson");
			if(itemPerson==null)itemPerson="%";
			String itemCarrier=getIntent().getStringExtra("itemCarrier");
			String itemCallType=getIntent().getStringExtra("itemCallType");
			cla=new CallLogsAnalytics();
        setContentView(R.layout.reports);
        Log.i("ReportActivity","ContentView initialized");
        
        //l2.setAdapter(new summaryAdapter(dh.getDaySummary()));
        
        String rTitle=cla.queryStrings[position];
    	//ListAdapter rItems;
    	
    	TextView title=(TextView)findViewById(R.id.rtitle);
		title.setText(rTitle);
        ListView report=(ListView)findViewById(R.id.report);
        
    	switch(position){
    		case 0: //Call logs
    			//rItems=new LogAdapter(R.layout.logitem,clogs);
    			//if(itemDay==null)itemDay="%";
    			clogs=new DatabaseHelper(this).fillLogs(this,itemDay,itemPerson,itemCarrier,itemCallType);
    	        report.setAdapter(cla.new LogAdapter(this,R.layout.logitem,clogs));
    	        break;
    		case 1: //Day Calls
    			ArrayList<String[]> summary=new DatabaseHelper(this).getDaySummary(this);
    	        report.setAdapter(cla.new SummaryAdapter(this,summary));
    	        report.setOnItemClickListener(new OnItemClickListener(){
 		    	   @Override
 		       		public void onItemClick(AdapterView<?> parent,View v, int position,long id){
	 		       		Intent i=new Intent(Reporting.this,Reporting.class);
	 		       		String[] data=(String[])parent.getItemAtPosition(position);
	 		       		i.putExtra("report", 0);//call logs
	 		       		i.putExtra("itemDay",data[0]);
	 		       		startActivity(i);
	 		       	}
	 		    });
    	        break;
    		case 2: //Week Calls
    			SummaryAdapter summaryAdapter=cla.new SummaryAdapter(this,new DatabaseHelper(this).getWeekSummary(this));
    	        report.setAdapter(summaryAdapter);
    	        
    	        break;
    		case 3: //Person Calls
    	        report.setAdapter(cla.new SummaryAdapter(this,new DatabaseHelper(this).getPersonSummary(this)));
    	        report.setOnItemClickListener(new OnItemClickListener(){
  		    	   @Override
  		       		public void onItemClick(AdapterView<?> parent,View v, int position,long id){
 	 		       		Intent i=new Intent(Reporting.this,Reporting.class);
 	 		       		String[] data=(String[])parent.getItemAtPosition(position);
 	 		       		i.putExtra("report", 0);//call logs
 	 		       		i.putExtra("itemPerson",data[0]);
 	 		       		startActivity(i);
 	 		       	}
 	 		    });
    	        break;
    		case 4: //Carrier Calls
    	        report.setAdapter(cla.new SummaryAdapter(this,new DatabaseHelper(this).getCarrierSummary(this)));
    	        report.setOnItemClickListener(new OnItemClickListener(){
  		    	   @Override
  		       		public void onItemClick(AdapterView<?> parent,View v, int position,long id){
 	 		       		Intent i=new Intent(Reporting.this,Reporting.class);
 	 		       		String[] data=(String[])parent.getItemAtPosition(position);
 	 		       		i.putExtra("report", 0);//call logs
 	 		       		i.putExtra("itemCarrier",data[0]);
 	 		       		startActivity(i);
 	 		       	}
 	 		    });
    	        break;
    		case 5: //Call Types
    	        report.setAdapter(cla.new SummaryAdapter(this,new DatabaseHelper(this).getCallTypeSummary(this)));
    	        report.setOnItemClickListener(new OnItemClickListener(){
  		    	   @Override
  		       		public void onItemClick(AdapterView<?> parent,View v, int position,long id){
 	 		       		Intent i=new Intent(Reporting.this,Reporting.class);
 	 		       		String[] data=(String[])parent.getItemAtPosition(position);
 	 		       		i.putExtra("report", 0);//call logs
 	 		       		i.putExtra("itemCallType",data[0]);
 	 		       		startActivity(i);
 	 		       	}
 	 		    });
    	        break;
    		case 6: //Carrier & Types
    	        report.setAdapter(cla.new SummaryAdapter(this,new DatabaseHelper(this).getSummary(this,5),1));
    	        report.setOnItemClickListener(new OnItemClickListener(){
  		    	   @Override
  		       		public void onItemClick(AdapterView<?> parent,View v, int position,long id){
 	 		       		Intent i=new Intent(Reporting.this,Reporting.class);
 	 		       		String[] data=(String[])parent.getItemAtPosition(position);
 	 		       		i.putExtra("report", 0);//call logs
 	 		       		i.putExtra("itemCarrier",data[0]);
 	 		       		startActivity(i);
 	 		       	}
 	 		    });
    	        break;
    	    
    	    default:
    	    	break;
    	}

        
    }
    public void showReport(String rTitle,ListAdapter rItems){
    	this.rTitle.setText(rTitle);
    	this.rItems=rItems;
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
    
}
