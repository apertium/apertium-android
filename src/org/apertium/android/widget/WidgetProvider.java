/*
 * Copyright (C) 2012 Arink Verma
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package org.apertium.android.widget;

import org.apertium.android.ApertiumActivity;
import org.apertium.android.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	WidgetHandler widgetHandler = null;
	RemoteViews remoteViews = null;
	String []Modes = null;
	int []ButtonCode = null;
	
	@Override
	public void onEnabled(Context context) {
		Intent intent = new Intent(context, WidgetConfigActivity.class);		   
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		context.startActivity(intent);
	}
		
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		
		// Get all ids
		ComponentName thisWidget = new ComponentName(context,WidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			
			Log.i("Widgets",widgetId+"");
			
			//Initializing view variables
			initView(context,widgetId);	
			
			//Setting up view variables
			fillView(context);		
			
			//Adding event listener to buttons
			addClickListner(context,widgetId);
			
			//Updating widgetview
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}
	
	
	void initView(Context context,int widgetId){
		widgetHandler = new WidgetHandler(context,widgetId);
		remoteViews = new RemoteViews(context.getPackageName(),	R.layout.widget_layout);
		Modes = widgetHandler.getWidgetModes();
		
		//Storing Mode buttons Layout code in Array
		ButtonCode = new int [Modes.length];
		ButtonCode[0] = R.id.widgetmode1;
		ButtonCode[1] = R.id.widgetmode2;
		ButtonCode[2] = R.id.widgetmode3;
		ButtonCode[3] = R.id.widgetmode4;
		ButtonCode[4] = R.id.widgetmode5;		
	}
	
	
	void fillView(Context context){
		
		for(int i=0;i<5;i++){
			Log.i("Mode",Modes[i]);
			if(Modes[i].equals("+")){
				remoteViews.setViewVisibility(ButtonCode[i], View.GONE);
			}else{
				remoteViews.setViewVisibility(ButtonCode[i],View.VISIBLE);
			}
			
			remoteViews.setTextViewText(ButtonCode[i], Modes[i]);
		}	
		
	}
	
	
	
	void addClickListner(Context context,int widgetId){
		
		// Intent and PendingIntent arrays for Mode Buttons
		Intent []intent = new Intent[Modes.length];
		PendingIntent []pendingIntent = new PendingIntent[Modes.length];
		
		//Setting up Mode button [1,..,5]
		for(int i=0;i<intent.length;i++){
			intent[i] = new Intent(context, ApertiumActivity.class);
			intent[i].setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent[i].putExtra("Mode",Modes[i]);
			pendingIntent[i] = PendingIntent.getActivity(context,i, intent[i], PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(ButtonCode[i], pendingIntent[i]);
		}
		
		// Config button
		Intent ConfigIntent = new Intent(context, WidgetConfigActivity.class);
		ConfigIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		ConfigIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		PendingIntent ConfigpendingIntent = PendingIntent.getActivity(context,0, ConfigIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.WidgetConfigButton, ConfigpendingIntent);
	}
	
	
}		