package edu.wit.seniorproject.emspeak.emspeak;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    LinearLayout btnLin;
    EditText et;
    TextView tv;
    ImageButton playBtn;








    // --------------------------------------------------------------------
    // All the microsoft info we need

    static String subscriptionKey = "cbb3735223de414aae6bc3c5885e8630";

    static String host = "https://api.cognitive.microsofttranslator.com";
    static String path = "/translate?api-version=3.0";

    // Translate to German and Italian.
    static String params = "&to=es";//&to=it";
    static String text;// = "Hello my name is Kyle";
    // --------------------------------------------------------------------







    // --------------------------------------------------------------------
    // all of Microsofts required methods


    public static class RequestBody {
        String Text;

        public RequestBody(String text) {
            this.Text = text;
        }
    }

    public static String Post (URL url, String content) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", content.length() + "");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setRequestProperty("X-ClientTraceId", java.util.UUID.randomUUID().toString());
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        byte[] encoded_content = content.getBytes("UTF-8");
        wr.write(encoded_content, 0, encoded_content.length);
        wr.flush();
        wr.close();

        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    public static String Translate () throws Exception {

        URL url = new URL (host + path + params);


        List<RequestBody> objList = new ArrayList<RequestBody>();
        objList.add(new RequestBody(text));
        String content = new Gson().toJson(objList);

        return Post(url, content);
    }

    // unnecessary, puts json in newline format
    public static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }


    // parse json string to only get translated text
    public static String getTranslatedText(String json_text) {
        String str00 = json_text;
        String [] arrOfStr00 = str00.split("text");
        String [] arrOfStr001 = arrOfStr00[1].split(",");
        String [] arrOfStr002 = arrOfStr001[0].split(":");
        //return arrOfStr002[1]; //includes quotes but not too important to worry about

        String transdTxt = arrOfStr002[1].replace("\"", "");
        return transdTxt;

    }


    // --------------------------------------------------------------------








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);






        btnLin=(LinearLayout)findViewById(R.id.btnSV);
        et = (EditText) findViewById(R.id.inputTxt);
        tv = (TextView) findViewById(R.id.outputTxt);





        /*
            this was placed inside the button creation so each button
            will make call for translation
         */
        /*
        // cannot run on main thread so run as async task
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    String response = Translate();
                    tv.setText(response);

                } catch (Exception e) {
                    Log.e("TAG", String.valueOf(e));
                }

            }

        });
        */



        /* Programmatically create buttons based off quickBtn array */

        String quickBtn[] = {"Do you speak English?",
                            "What is your name?",
                            "Are you under the influence of drugs or alcohol?",
                            "Show me where the pain is.",
                            "Is there anyone else with you?",
                            "Can you feel this?",
                            "What is your blood type?",
                            "Do you know where you are?",
                            "Do you remember what happened?",
                            "Do you have any allergies?",
                            "Are you taking any medication?",
                            "Are you having difficulty breathing?",
                            "How does your head feel?",
                            "Is your vision normal?"};








        btnLin=(LinearLayout)findViewById(R.id.btnSV);
        et = (EditText) findViewById(R.id.inputTxt);
        tv = (TextView) findViewById(R.id.outputTxt);


        int n = quickBtn.length;

        Button[] btn = new Button[n];


        for (int i = 0; i < n; i++) {
            btn[i] = new Button(this); //create button object
            btn[i].setText(quickBtn[i]); //sets button's text
            btnLin.addView(btn[i]); //adds button to LinearLayout inside ScrollView

            /* creates onClickListener for each button so when clicked,
               its text will populate the text fields */
            btn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Button b = (Button)v;
                    String bTxt = b.getText().toString();

                    // change variable globally
                    text = bTxt;

                    et.setText(bTxt);
                    //tv.setText(bTxt);


                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                String response = Translate();
                                //tv.setText(response); //prints stringified json
                                tv.setText(getTranslatedText(response));


                            } catch (Exception e) {
                                Log.e("TAG", String.valueOf(e));
                            }

                        }

                    });


                    //tv.setText(bTxt);

                }
            });
        }









        playBtn = (ImageButton) findViewById(R.id.txtBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Button b = (Button)v;
                String bTxt = et.getText().toString();

                // change variable globally
                text = bTxt;


                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            String response = Translate();
                            //tv.setText(response); //prints stringified json
                            tv.setText(getTranslatedText(response));


                        } catch (Exception e) {
                            Log.e("TAG", String.valueOf(e));
                        }

                    }

                });


                //tv.setText(bTxt);

            }
        });











        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NavigationView navigationView1 = findViewById(R.id.lang_select_view);
        navigationView1.setNavigationItemSelectedListener(this);



    }





    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }







    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        if (id == R.id.nav_settings) {
            Log.v("myApp", "settings is clicked");

        } else if (id == R.id.nav_help) {
            Log.v("myApp", "Help is clicked");


        } else if (id == R.id.nav_about) {
            Log.v("myApp", "About is clicked");


        }
        else if (id == R.id.Arabic){
            Log.v("myApp", "Arabic is clicked");
            drawer.closeDrawer(GravityCompat.END);



        }
        else if (id == R.id.Spanish){
            Log.v("myApp", "Spanish is clicked");
            drawer.closeDrawer(GravityCompat.END);

        }
        else if (id == R.id.Chinese){
            Log.v("myApp", "Chinese is clicked");
            drawer.closeDrawer(GravityCompat.END);
        }
        else if (id == R.id.Vietnamese){
            Log.v("myApp", "Vietnamese is clicked");
            drawer.closeDrawer(GravityCompat.END);
        }
        else if (id == R.id.German){
            Log.v("myApp", "German is clicked");
            drawer.closeDrawer(GravityCompat.END);

        }
        else if (id == R.id.Portuguese){
            Log.v("myApp", "Portuguese is clicked");
            drawer.closeDrawer(GravityCompat.END);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }





}
