package com.google.api.services.samples.calendar.android;

//import com.google.api.services.calendar.model.Calendar;
import android.util.Log;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.services.calendar.model.Event;
import com.google.api.client.http.HttpHeaders;

import io.github.scola.birthday.Birthday;
import io.github.scola.birthday.BirthdayListFragment;

import java.io.IOException;
import java.util.List;

public class AsyncBatchInsertEvent extends CalendarAsyncTask {
	private final String calendar_id;
	private final List<Event> events;
	private final Birthday birthday;
	private final static String TAG = "AsyncBatchInsertEvent";
	
	public AsyncBatchInsertEvent(BirthdayListFragment calendarSample, String calendar_id, List<Event> events, Birthday birthday) {
	    super(calendarSample);
	    this.calendar_id = calendar_id;
	    this.events = events;
	    this.birthday = birthday;
	  }
	
//	  @Override
//	  protected void doInBackground() throws IOException {
//		  Event createEvent = client.events().insert(calendar_id, event).execute();
//		  birthday.getEventId().add(createEvent.getId());
//		  birthday.setIsSync(true);
//		  Log.d(TAG, "create event for " + birthday.getName() + " eventId=" + createEvent.getId());
//	  }
      
      @Override
      protected void doInBackground() throws IOException {
        BatchRequest batch = client.batch();
        for (Event event : events) {
          client.events().insert(calendar_id, event).setFields("id")
              .queue(batch, new JsonBatchCallback<Event>() {

                public void onSuccess(Event event, HttpHeaders headers) {
                    birthday.getEventId().add(event.getId());
                    Log.d(TAG, "create event for " + birthday.getName() + " eventId=" + event.getId());
                }

                @Override
                public void onFailure(GoogleJsonError err, HttpHeaders headers) throws IOException {
                  if(fragment.isCancelAyncTasks()) return;		
                  Utils.logAndShowError(fragment, TAG, err.getMessage());
                }
              });
        }
        batch.execute();
      }
      
      @Override
      protected final void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        fragment.getSyncingBirthdays().remove(birthday);
        if(success) birthday.setIsSync(true);
      }
}
