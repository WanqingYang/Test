import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.text.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class SimpleLifetimeValue {

  public SimpleLifetimeValue() {
    user_data_map_ = new HashMap<String, UserData>();
    latest_time_ = null;

    // Only need to be accurate to hours.
    date_format_ = new SimpleDateFormat("yyyy-MM-dd:HH");
    //date_format_.setTimeZone(TimeZone.getTimeZone("Zulu"));
  }

  // Compare how many days has passed from the day of epoch.
  // Return the number week passed from that day.
  public static long getWeekId(Date date) {
    long days = date.getTime() / 1000000 / 3600 / 24;
    // The day of epoch is 01-01-1970, it is Thursday
    return (days + 4) / 7;
  }

  public class UserData {
    public UserData(String customer_id_in) {
      total_site_visit = 0;
      total_expense = 0.0;
      start_time = null;
      order_info_map = new HashMap<String, OrderInfo>();
      
      site_visits = new ArrayList<JSONObject>();
      image_uploads = new ArrayList<JSONObject>();
      orders = new ArrayList<JSONObject>();
      last_updated_time = null;
      customer_id = customer_id_in;
      last_name = new String();
    }

    // Ingest an event related to current user.
    // For different type of events, we update user data correspondingly.
    public void addEvent(JSONObject event) throws ParseException,
                                                  java.text.ParseException {
      // If the timestamp of this event is before start_time, replace it with
      // new timestamp. 
      String event_time_str = (String) event.get("event_time");
      Date event_time = date_format_.parse(event_time_str);
      if (start_time == null || event_time.before(start_time)) {
        start_time = event_time;
      }

      String type = (String) event.get("type");

      if (type.equals("CUSTOMER")) {

        // Update user infomation if the events has a newer timestamp.
        // Otherwise ignore this event.
        if (last_updated_time == null ||
            last_updated_time.before(event_time)) {
          last_updated_time = event_time;
          last_name = (String) event.get("last_name");
          adr_city = (String) event.get("adr_city");
          adr_state = (String) event.get("adr_state");
        }

      } else if (type.equals("SITE_VISIT")) {

        // Update records for site visit.
        site_visits.add(event);

        // Update site visit count.
        total_site_visit += 1;

      } else if (type.equals("IMAGE")) {

        // Update records for image upload.
        image_uploads.add(event);

      } else if (type.equals("ORDER")) {

        // Update records for orders.
        orders.add(event);

        // Get order id.
        String order_id = (String) event.get("key");

        // Get string format of total_amount and parse the value out.
        String total_amount_str = (String) event.get("total_amount");
        String usd_amount = total_amount_str.split(" ")[0]; 
        double total_amount = Double.parseDouble(usd_amount);

        // Update order info map and total expense of all events.
        OrderInfo order_info = order_info_map.get(order_id);
        if (order_info == null) {

          // Create order info object and put it in map.
          order_info = new OrderInfo();
          order_info.order_id = order_id;
          order_info.total_amount = total_amount;
          order_info.event_time = event_time;
          order_info_map.put(order_id, order_info); 

          // Update total expense of all events.
          total_expense += order_info.total_amount; 

        } else if (event_time.after(order_info.event_time)) {
            // Update total expense.
            double expense_diff = total_amount - order_info.total_amount; 
            total_expense += expense_diff; 

            // Update order info.
            order_info.total_amount = total_amount;
            order_info.event_time = event_time;
        }
      }
    }

    // SLV related.

    // Total count of site visit of this customer.
    public long total_site_visit;
    
    // Total of expense the customer spend among all events. 
    public double total_expense;


    // Information of most recent order.
    public class OrderInfo {
      public String order_id;
      public double total_amount;
      public Date event_time;
    }

    // Map from order id to the information of the most recent order. 
    public HashMap<String, OrderInfo> order_info_map;

    // The oldest timestamp among all the events of this customer..
    public Date start_time; 

    // SLV unrelated.
    // Customer basic information.
    public String customer_id;
    public Date last_updated_time;
    public String last_name;
    public String adr_city;
    public String adr_state;

    // Other event records.
    ArrayList<JSONObject> site_visits;
    ArrayList<JSONObject> image_uploads;
    ArrayList<JSONObject> orders;
  }

  // Map from user id to all the data of this user.
  private HashMap<String, UserData> user_data_map_;

  // Latest timestamp among all events.
  private Date latest_time_;

  // Constans.
  // LifeSpan for ShutterFly.
  private static final double life_span_avg_ = 10.0;

  // Date format used to translate date from string.
  private static DateFormat date_format_;

  // 1.Read input file and parse out the information of events(list of json dictionary).
  // 2.Store those information in user_data_map_. 
  public void ReadEventFromFile(String file_path)
    throws FileNotFoundException,
           IOException,
           ParseException,
           java.text.ParseException {
    JSONParser parser = new JSONParser();
    // TODO: Pass input path as command line arguments.
    JSONArray arr = (JSONArray) parser.parse(new FileReader(file_path)); 
    for (Object obj : arr) {
      JSONObject event = (JSONObject) obj;
      ingestEvent(event);
    }
  }

 
  // Ingest all the information of the event passed in.
  public void ingestEvent(JSONObject event) throws IOException,
                                                   ParseException,
                                                   java.text.ParseException {
    // Get user id.
    String type = (String) event.get("type");
    String user_id;
    if (type.equals("CUSTOMER")) {
      user_id = (String) event.get("key");
    } else {
      user_id = (String) event.get("customer_id");
    }

    // Find user data associated with this id.
    // If it does not exist yet, create one.
    UserData user_data;
    if (!user_data_map_.containsKey(user_id)) {
      user_data = new UserData(user_id);
      //user_data_map_.put(user_id, user_data);
    } else {
      user_data = user_data_map_.get(user_id);
    }
    user_data.addEvent(event);
    user_data_map_.put(user_id, user_data);
 
    // Update latest time.
    String event_time_str = (String) event.get("event_time");
    Date event_time = date_format_.parse(event_time_str);
    if (latest_time_ == null || latest_time_.before(event_time)) {
       latest_time_ = event_time;
    }
  }


  class UserSLV implements Comparable<UserSLV> {
    @Override
    public int compareTo(UserSLV user_slv) {
      Double d1 = new Double(user_slv.slv);
      Double d2 = new Double(slv);
      return d1.compareTo(d2);
    }
    public String user_id;
    public double slv;
  }

  // Get Custormer id and SLV pairs with top x SLV values. 
  // Print out the result.
  public void topXSimpleLifetimeValue(int x) {

    // Calculate SLV for all customer.
    ArrayList<UserSLV> user_slv_list = new ArrayList<UserSLV>();
    for (String user_id : user_data_map_.keySet()) {

      if (user_id == null) { continue; }

      // Get user data.
      UserData user_data = user_data_map_.get(user_id);

      // Calculate week count. 
      long week_start = getWeekId(user_data.start_time);
      long week_end = getWeekId(latest_time_);
      long weeks = week_end - week_start + 1;

      // Calculate slv.
      double slv = user_data.total_expense / (double)weeks * life_span_avg_;
 
      // Associate SLV and user id together and put into the list.
      UserSLV user_slv = new UserSLV();
      user_slv.user_id = user_id;
      user_slv.slv = slv;
      user_slv_list.add(user_slv);
      //System.out.println("id:" + user_data.customer_id);
    } 

    // Sort the list.
    Collections.sort(user_slv_list);

    // Print out result.
    int size = Math.min(x, user_slv_list.size());
    for (int i = 0; i < size; ++i) {
      double slv = user_slv_list.get(i).slv;
      String user_id = user_slv_list.get(i).user_id;
      UserData user_data = user_data_map_.get(user_id);
      String last_name = user_data.last_name;
      String adr_city = user_data.adr_city;
      String adr_state = user_data.adr_state;
      System.out.println("User id: " + user_id + ", Last name: " + last_name + ", city: " + adr_city + ", state: " + adr_state +
                         ", Simple Lifetime Value: " + slv + ".");
    } 
  } 


  public static void main(String[] args) throws FileNotFoundException,
                                                IOException,
                                                ParseException,
                                                java.text.ParseException {
    SimpleLifetimeValue slv = new SimpleLifetimeValue();
    System.out.println("Start read file " + args[0]);
    slv.ReadEventFromFile(args[0]); 
    slv.topXSimpleLifetimeValue(10);
  }
}
