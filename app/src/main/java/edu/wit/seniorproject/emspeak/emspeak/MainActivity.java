package edu.wit.seniorproject.emspeak.emspeak;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    LinearLayout btnLin;
    EditText et;
    TextView tv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        /*
        // test, programmatically generate buttons
        btnLin=(LinearLayout)findViewById(R.id.btnSV);
        Button[] btn = new Button[10];
        for (int i = 0; i < 10; i++) {
            btn[i] = new Button(this);
            //btn[i].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            btn[i].setText("This is the button" + i);
            btnLin.addView(btn[i]);
        } */




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

                    et.setText(bTxt);
                    tv.setText(bTxt);

                }
            });



        }






        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

        if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
