package com.twenjez.calllogsanalytics;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import com.twenjez.calllogsanalytics.CallLogsAnalytics.*;

import android.database.*;
import android.provider.CallLog;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.google.i18n.phonenumbers.*;;

public class DatabaseHelper extends SQLiteOpenHelper {
	PhoneNumberUtil phoneUtil=PhoneNumberUtil.getInstance();
	PhoneNumberToCarrierMapper p2c=PhoneNumberToCarrierMapper.getInstance();
	
	private static final String dbName="callLogs.db";
	private static final int dbVersion=3;
	String[] callTypes=new String[]{"INCOMING","OUTGOING","MISSED","VOICEMAIL","REJECTED","BLOCKED","ANSWERED EXTERNALLY"};
	private Context c=null;
	int mc=0;
	
	public DatabaseHelper(Context context) {
		super(context, dbName, null, dbVersion);
		// TODO Auto-generated constructor stub
		this.c=context;
		Log.i("DBHelper",mc++ + "Constructor");
	}	

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i("DBHelper",mc++ + "onCreate");
		try{
			db.beginTransaction();
			db.execSQL(
				"Create table if not exists logs (" +
						//"id integer primary key," +
						"date datetime, " +
						"contact_name text," +
						"number text," +
						"call_type text," +
						"duration integer," +
						"carrier text);"
			);
			/*
			db.execSQL(
					"Create table networks (" +
							//"id integer primary key," +
							"prefix text, " +
							"network_name text," +
							"company_name text," +
				);
			*/
			db.setTransactionSuccessful();
			Log.i("DBHelper","Table Created");
		
		}
		finally{
			db.endTransaction();
		}
	}
	//void loadLogs(ArrayList<CustomLog> clogs){
	void loadLogs(Cursor cursor){
		Log.i("DBHelper",mc++ + "loadLogs");
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		Long lastCallTime=0l;
		try {
			db.beginTransaction();
			
			//get last call time logged
			Cursor logt=db.rawQuery("Select max(date) from logs",null);
			if(logt.moveToFirst()){
				lastCallTime=Long.valueOf(logt.getString(0));
				Log.i("DBHelper","Last Calls at "+lastCallTime+": "+new Date(lastCallTime));
			}
			db.setTransactionSuccessful();
		}
		catch(Exception e){
			Log.e("DB Helper","Checking last record"+e.getLocalizedMessage());
			Log.e("DB Helper",e.fillInStackTrace().toString());
		}
		finally{
			db.endTransaction();
		}

		int counter=0;
		try{
			db.beginTransaction();
			
			while(cursor.moveToNext()){
				//String lId = cursor.getString(0);
				String cName = cursor.getString(0);
	            String phNumber = cursor.getString(1);
	            String callType = cursor.getString(2);
	            String callDate = cursor.getString(3);
	            Date callDayTime = new Date(Long.valueOf(callDate));
	            String callDuration = cursor.getString(4);
	            String dir = null;
	            int dircode = Integer.parseInt(callType);
				
	            dir=callTypes[dircode-1];
	            
	            if(Long.valueOf(callDate)>lastCallTime){
		            cv.put("date",callDate);
		            //cv.put("date",callDayTime);
		            cv.put("contact_name",cName);
		            cv.put("number", phNumber);
		            cv.put("call_type",dir);
		            cv.put("duration", callDuration);
		            
					String carrier=null;
					try{
						carrier=p2c.getNameForNumber(phoneUtil.parse(phNumber,"TZ"),Locale.ENGLISH);
					}catch(Exception e){
						Log.e("DBHelper",e.getLocalizedMessage());
					}
					cv.put("carrier",carrier );
		            db.insertOrThrow("logs",null,cv);
					//Log.i("DBHelper",counter++ +" Inserting details from cursor "+phNumber+":"+carrier);
	            }else{
	            	//Log.i("DBHelper",counter++ +" Record exists"+phNumber);
	            }
			}
			db.setTransactionSuccessful();
			Log.i("DBHelper","Details Loaded, records = "+cursor.getCount());
			cursor.close();
		}
		catch(Exception e){
			Log.e("DB Helper",e.getLocalizedMessage());
			Log.i("DBHelper","Record "+counter +" with PK "+cv.getAsString("date"));
			Log.e("DB Helper",e.fillInStackTrace().toString());
		}
		finally{
			db.endTransaction();
		}
	}
	ArrayList<String[]> getDaySummary(Context context){
		Log.i("DBHelper",mc++ + "getDaySummary");
		return getSummary(context,0);
	}
	ArrayList<String[]> getWeekSummary(Context context){
		Log.i("DBHelper",mc++ + "getWeekSummary");
		return getSummary(context,1);
	}
	ArrayList<String[]> getPersonSummary(Context context){
		Log.i("DBHelper",mc++ + "getPersonSummary");
		return getSummary(context,2);
	}
	ArrayList<String[]> getCarrierSummary(Context context){
		Log.i("DBHelper",mc++ + "getCarrierSummary");
		return getSummary(context,3);
	}
	ArrayList<String[]> getCallTypeSummary(Context context){
		Log.i("DBHelper",mc++ + "getCallTypeSummary");
		return getSummary(context,4);
	}
	ArrayList<String[]> getSummary(Context context,int code){
		Log.i("DBHelper",mc++ + "getSummary");
		SQLiteDatabase db=this.getReadableDatabase();
		ArrayList<String[]> csums=new ArrayList<String[]>();
		try{
			db.beginTransaction();
			String query="";
			switch(code){
			case 0://day summary
				query="Select date,count(duration),sum(duration) from (select date(date/1000,'unixepoch') date,duration from logs) s group by date order by date desc";
				break;
			case 1://week summary
				query="Select date,count(duration),sum(duration) from (select strftime('%Y-%W',date/1000,'unixepoch') date,duration from logs) s group by date order by date desc";
				break;
			case 2:// personal summary
				query="Select contact_name,count(duration),sum(duration) from logs group by contact_name order by 3 desc";
				break;
			case 3://mobile
				query="Select carrier,count(duration),sum(duration) from logs group by carrier order by 3 desc";
				break;
			case 4://call type
				query="Select call_type,count(duration),sum(duration) from logs group by call_type order by 3 desc";
				break;
			case 5://carrier & type
				//query="Select carrier,call_type,count(duration),sum(duration) from logs group by carrier,call_type order by 4 desc";
				query="Select carrier,SUM(CASE call_type when 'OUTGOING' THEN 1 else 0 end) out_calls," +
						"sum(CASE call_type when 'OUTGOING' THEN duration else 0 end) out_duration," +
						"SUM(CASE call_type when 'INCOMING' THEN 1 else 0 end) in_calls," +
						"sum(CASE call_type when 'INCOMING' THEN duration else 0 end) in_duration," +
						"SUM(CASE call_type when 'MISSED' THEN 1 else 0 end) mis_calls," +
						"SUM(CASE call_type when 'REJECTED' THEN 1 else 0 end) rej_calls," +
						"SUM(CASE call_type when 'BLOCKED' THEN 1 else 0 end) blc_calls " +
						"from logs group by carrier order by 2 DESC,4 desc";
				break;
			}
			Cursor c=db.rawQuery(query,null);
			while(c.moveToNext()){
				if(code==5){
					/*
					String type=c.getString(1);
					csums.add(new String[]{type+": "+c.getString(0),c.getString(2),c.getString(3)});*/
					csums.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6),c.getString(7)});
					Log.i("DBHelper",c.getString(0)+" "+c.getString(1));
				}
				else{
					csums.add(new String[]{c.getString(0),c.getString(1),c.getString(2)});
					Log.i("DBHelper",c.getString(0)+" "+c.getString(1));	
				}
				
			}
			c.close();
			db.setTransactionSuccessful();
		}
		catch(Exception e){
			Log.e("DB Helper",e.getLocalizedMessage());
			Log.e("DB Helper",e.fillInStackTrace().toString());
		}
		finally{
			db.endTransaction();
		}
		return csums;
	}
	
	public Cursor getLogs(Context context,String day,String contact,String carrier,String callType){
		Log.i("DBHelper",mc++ + "getLogs");
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor logs=null;
		try{
			db.beginTransaction();
			//String ds=(day==null)?"%":"date("+(Integer.parseInt(day)/1000)+",'unixepoch')";
			String ds=(day==null)?"%":day;
			contact=(contact=="%")?"%":contact;
			carrier=(carrier==null)?"%":carrier;
			callType=(callType==null)?"%":callType;
			
			String table="logs";
			String columns=null;
			String whereClause=" date like ? and contact_name like ?";
			String[] whereArgs=new String[]{ds,contact};
			Log.i("DBHelper","filters to query day: "+day+", contact: "+contact+", carrier: "+carrier+" cl "+carrier.length());
			String whereCarrier="and carrier "+((carrier.length()==0)?" not in (select distinct carrier from logs where length(carrier)>0) ":" like \""+carrier+"\"");
			String query="Select * from logs where date(date/1000,'unixepoch') like \""+ds+"\" and contact_name like \""+contact+"\" "+whereCarrier+" and call_type like \""+callType+"\" order by date DESC,carrier";
			Log.i("DBHelper","query to db = "+query);
			logs=db.rawQuery(query,null);
			Log.i("DBHelper","Logs from db = "+logs.getCount());
			
			db.setTransactionSuccessful();
			Log.i("DBHelper","Cursor size "+logs.getColumnNames().toString());
		}
		catch(Exception e){
			Log.e("DB Helper",e.getLocalizedMessage());
			Log.e("DB Helper",e.fillInStackTrace().toString());
		}
		finally{
			db.endTransaction();
		}
		return logs;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("DBHelper",mc++ + "onUpgrade");
		// TODO Auto-generated method stub
		switch(oldVersion){
		case 1:
			//db.execSQL("DROP TABLE IF EXISTS logs");
			//onCreate(db);
		case 2:
			//add new columns country,carrier
			//db.execSQL("Alter table logs add country text");
			db.execSQL("alter table logs add carrier text");
			//update existing records with new column info
			try{
				db.beginTransaction();
				Cursor nums=db.rawQuery("select distinct number from logs",null);
				ContentValues cv=new ContentValues();
				while(nums.moveToNext()){
					String number=nums.getString(0);
					String carrier=null;
					try{
						carrier=p2c.getNameForNumber(phoneUtil.parse(number,"TZ"),Locale.ENGLISH);
					}catch(Exception e){
						Log.e("DBHelper",e.getLocalizedMessage());
						Log.e("DBHelper",e.getStackTrace().toString());
					}
					cv.put("carrier",carrier );
					String[] whereArgs={number};
					db.update("logs",cv,"number = ?",whereArgs);
				}
				nums.close();
				db.setTransactionSuccessful();
			}catch(Exception e2){
				Log.e("DBHelper",e2.getLocalizedMessage());
				Log.e("DBHelper","Update error "+e2.fillInStackTrace().toString()+" "+e2.getCause());
			}finally{
				db.endTransaction();
			}
			onCreate(db);
			break;
			
			
			
		}
		
	}
	
	public ArrayList<CustomLog> fillLogs(Context context,String day,String contact,String carrier,String callType){
		Log.i("DatabaseHelper",mc++ + " fillLogs");
    	Cursor c=this.getLogs(context,day,contact,carrier,callType);
    	ArrayList<CustomLog> clogs=new ArrayList<CustomLog>();
    	int counter=0;
    	while(c.moveToNext()){
    		Date d=new Date(c.getLong(1-1));
			CustomLog cl=new CustomLog(c.getString(3-1),c.getString(2-1),c.getString(4-1),new Date(c.getLong(1-1)),c.getInt(5-1));
			//if (counter<1000) Log.i("DatabaseHelper",counter++ +" Date: "+c.getLong(1-1)+" "+new Date(c.getLong(1-1))+" "+d.toString());//+", "+Long.valueOf(c.getString(1)));
			clogs.add(cl);
		}
		c.close();
		return clogs;
    }
}
