package edu.wit.seniorproject.emspeak.emspeak;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    LinearLayout btnLin;
    EditText et;
    TextView tv;
    ImageButton playBtn;
    String new_question;
    TextToSpeech t1;


    // --------------------------------------------------------------------
    // All the microsoft info we need

    static String subscriptionKey = "ad592664cd2c4190b0732d2bbcdaa12c";//"cbb3735223de414aae6bc3c5885e8630";

    static String host = "https://api.cognitive.microsofttranslator.com";
    static String path = "/translate?api-version=3.0";


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
//        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestProperty("Content-Length", content.length() + "");
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

        String path = "/data/data/" + getPackageName() + "/questions.db";

        SQLiteDatabase db;
        db = SQLiteDatabase.openOrCreateDatabase(path, null);

        String sql = "CREATE TABLE IF NOT EXISTS questions" + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, question TEXT);";
        db.execSQL(sql);

//        db.execSQL("select question from questions");
//        db.execSQL("delete from questions");

        String[] columns = {"question"};
        String where = null;
        String[] where_args = null;
        String having = null;
        String group_by = null;
        String order_by = "_id DESC";
        Cursor cursor = db.query("questions", columns, where, where_args, group_by, having, order_by);
        List questions_database = new ArrayList<>();
        while(cursor.moveToNext()){
            String question = cursor.getString(cursor.getColumnIndex("question"));
            questions_database.add(question);
            Log.v("questions_database", question);
        }
        cursor.close();
        int n = questions_database.size();
        Log.v("arraylistsize", String.valueOf(n));


        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });


        Button[] btn = new Button[n];

        btnLin=(LinearLayout)findViewById(R.id.btnSV);
        et = (EditText) findViewById(R.id.inputTxt);
        tv = (TextView) findViewById(R.id.outputTxt);


        for(int i = 0; i < n; i++){
            btn[i] = new Button(this); //create button object
            btn[i].setText(questions_database.get(i).toString()); //sets button's text
            btnLin.addView(btn[i]); //adds button to LinearLayout inside ScrollView
            btn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
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
                                //tv.setText(response); //prints stringfield json
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
        //Close the database
        //db.close();


        // ADD INITIAL VALUES TO THE DATABASE
//        ContentValues values = new ContentValues();
//        for (int i = 0; i < quickBtn.length; i++){
//            values.put("question", quickBtn[i]);
//            db.insert("questions", null, values);
//        }

        // NOT NEEDED CODE - Just here if we duplicate all of the entries and need to delete them.
//        for(int i = 0; i < questions_database.size(); i++ ){
//            String [] deleteArgs = {Integer.toString(i)};
//            db.delete("questions", "_id=?", deleteArgs);
//        }

//        db.close();



        Bundle bundle = this.getIntent().getExtras();
        if(getIntent().getExtras() != null){
            new_question = bundle.getString("new_question");
            Log.v("EMSpeak", new_question);
            ContentValues values = new ContentValues();
            values.put("question", new_question);

            db.insert("questions", null, values);
            db.close();
            questions_database.add(new_question);
            finish();
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);


        }


        et = (EditText) findViewById(R.id.inputTxt);
        tv = (TextView) findViewById(R.id.outputTxt);

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

                String toSpeak = tv.getText().toString();
                Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);


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
    protected void onResume() {
        super.onResume();
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


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (id == R.id.nav_help) {
            Log.v("myApp", "Help is clicked");
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, HelpActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about) {
            Log.v("myApp", "About is clicked");
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, AboutActivity.class);
            startActivity(intent);

        }
        else if (id == R.id.nav_help) {

        }
        else if (id == R.id.nav_about) {


        }
        else if(id == R.id.add_question){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, AddQuestionActivity.class);
            startActivity(intent);


        } /*else if (id == R.id.Spanish){
            params = "&to=es";
            Toast.makeText(getApplicationContext(), "Spanish", Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.END);


        } else if (id == R.id.Portuguese){
            params = "&to=pt";
            Toast.makeText(getApplicationContext(), "Portuguese", Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.END);

        } else if (id == R.id.Chinese){
            params = "&to=zh-Hans";
            Toast.makeText(getApplicationContext(), "Chinese", Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.END);

        } else if (id == R.id.Vietnamese){
            params = "&to=vi";
            Toast.makeText(getApplicationContext(), "Vietnamese", Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.END);

        } else if (id == R.id.German){
            params = "&to=de";
            Toast.makeText(getApplicationContext(), "German", Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.END);

        } else if (id == R.id.Arabic){
            params = "&to=ar";
            Toast.makeText(getApplicationContext(), "Arabic", Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.END);

        } else if (id == R.id.Hindi){
            params = "&to=hi";
            Toast.makeText(getApplicationContext(), "Hindi", Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.END);

        }
        else if (id == R.id.English) {
            params = "&to=en";
            Toast.makeText(getApplicationContext(), "English", Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.END);
        }*/
        
        
        
        switch (id) {
            case R.id.English:
                params = "&to=en";
                Toast.makeText(getApplicationContext(), "English", Toast.LENGTH_SHORT).show();
                t1.setLanguage(Locale.US);
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Afrikaans:
                params = "&to=af";
                Toast.makeText(getApplicationContext(), "Afrikaans", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Arabic:
                params = "&to=ar";
                Toast.makeText(getApplicationContext(), "Arabic", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Arabic_Levantine:
                params = "&to=apc";
                Toast.makeText(getApplicationContext(), "Arabic, Levantine", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Bangla:
                params = "&to=bn";
                Toast.makeText(getApplicationContext(), "Bangla", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Bosnian:
                params = "&to=bs";
                Toast.makeText(getApplicationContext(), "Bosnian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Bulgarian:
                params = "&to=bg";
                Toast.makeText(getApplicationContext(), "Bulgarian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Cantonese:
                params = "&to=yue";
                Toast.makeText(getApplicationContext(), "Cantonese ", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Catalan:
                params = "&to=ca";
                Toast.makeText(getApplicationContext(), "Catalan", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Chinese_Simplified:
                params = "&to=zh-Hans";
                Toast.makeText(getApplicationContext(), "Chinese Simplified", Toast.LENGTH_SHORT).show();
                t1.setLanguage(Locale.CHINESE);
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Chinese_Traditional:
                params = "&to=zh-Hant";
                Toast.makeText(getApplicationContext(), "Chinese Traditional", Toast.LENGTH_SHORT).show();
                t1.setLanguage(Locale.TRADITIONAL_CHINESE);
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Croatian:
                params = "&to=hr";
                Toast.makeText(getApplicationContext(), "Croatian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Czech:
                params = "&to=cs";
                Toast.makeText(getApplicationContext(), "Czech", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Danish:
                params = "&to=da";
                Toast.makeText(getApplicationContext(), "Danish", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Dutch:
                params = "&to=nl";
                Toast.makeText(getApplicationContext(), "Dutch", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Estonian:
                params = "&to=et";
                Toast.makeText(getApplicationContext(), "Estonian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Fijian:
                params = "&to=fj";
                Toast.makeText(getApplicationContext(), "Fijian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Filipino:
                params = "&to=fil";
                Toast.makeText(getApplicationContext(), "Filipino", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Finnish:
                params = "&to=fi";
                Toast.makeText(getApplicationContext(), "Finnish", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.French:
                params = "&to=fr";
                Toast.makeText(getApplicationContext(), "French", Toast.LENGTH_SHORT).show();
                t1.setLanguage(Locale.FRANCE);
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.German:
                params = "&to=de";
                Toast.makeText(getApplicationContext(), "German", Toast.LENGTH_SHORT).show();
                t1.setLanguage(Locale.GERMANY);
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Greek:
                params = "&to=el";
                Toast.makeText(getApplicationContext(), "Greek", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Haitian_Creole:
                params = "&to=ht";
                Toast.makeText(getApplicationContext(), "Haitian Creole", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Hebrew:
                params = "&to=he";
                Toast.makeText(getApplicationContext(), "Hebrew", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Hindi:
                params = "&to=hi";
                Toast.makeText(getApplicationContext(), "Hindi", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Hmong_Daw:
                params = "&to=mww";
                Toast.makeText(getApplicationContext(), "Hmong Daw", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Hungarian:
                params = "&to=hu";
                Toast.makeText(getApplicationContext(), "Hungarian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Icelandic:
                params = "&to=is";
                Toast.makeText(getApplicationContext(), "Icelandic", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Indonesian:
                params = "&to=id";
                Toast.makeText(getApplicationContext(), "Indonesian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Italian:
                params = "&to=it";
                Toast.makeText(getApplicationContext(), "Italian", Toast.LENGTH_SHORT).show();
                t1.setLanguage(Locale.ITALY);
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Japanese:
                params = "&to=ja";
                Toast.makeText(getApplicationContext(), "Japanese", Toast.LENGTH_SHORT).show();
                t1.setLanguage(Locale.JAPAN);
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Kiswahili:
                params = "&to=sw";
                Toast.makeText(getApplicationContext(), "Kiswahili", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Klingon:
                params = "&to=tlh";
                Toast.makeText(getApplicationContext(), "Klingon", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Klingon_plqaD:
                params = "&to=tlh-Qaak";
                Toast.makeText(getApplicationContext(), "Klingon (plqaD)", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Korean:
                params = "&to=ko";
                Toast.makeText(getApplicationContext(), "Korean", Toast.LENGTH_SHORT).show();
                t1.setLanguage(Locale.KOREAN);
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Latvian:
                params = "&to=lv";
                Toast.makeText(getApplicationContext(), "Latvian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Lithuanian:
                params = "&to=lt";
                Toast.makeText(getApplicationContext(), "Lithuanian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Malagasy:
                params = "&to=mg";
                Toast.makeText(getApplicationContext(), "Malagasy", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Malay:
                params = "&to=ms";
                Toast.makeText(getApplicationContext(), "Malay", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Maltese:
                params = "&to=mt";
                Toast.makeText(getApplicationContext(), "Maltese", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Norwegian:
                params = "&to=nb";
                Toast.makeText(getApplicationContext(), "Norwegian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Persian:
                params = "&to=fa";
                Toast.makeText(getApplicationContext(), "Persian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Polish:
                params = "&to=pl";
                Toast.makeText(getApplicationContext(), "Polish", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Portuguese:
                params = "&to=pt";
                Toast.makeText(getApplicationContext(), "Portuguese", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Queretaro_Otomi:
                params = "&to=otq";
                Toast.makeText(getApplicationContext(), "Queretaro Otomi", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Romanian:
                params = "&to=ro";
                Toast.makeText(getApplicationContext(), "Romanian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Russian:
                params = "&to=ru";
                Toast.makeText(getApplicationContext(), "Russian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Samoan:
                params = "&to=sm";
                Toast.makeText(getApplicationContext(), "Samoan", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Serbian_Cyrillic:
                params = "&to=sr-Cyrl";
                Toast.makeText(getApplicationContext(), "Serbian (Cyrillic)", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Serbian_Latin:
                params = "&to=sr-Latn";
                Toast.makeText(getApplicationContext(), "Serbian (Latin)", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Slovak:
                params = "&to=sk";
                Toast.makeText(getApplicationContext(), "Slovak", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Slovenian:
                params = "&to=sl";
                Toast.makeText(getApplicationContext(), "Slovenian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Spanish:
                params = "&to=es";
                Toast.makeText(getApplicationContext(), "Spanish", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Swedish:
                params = "&to=sv";
                Toast.makeText(getApplicationContext(), "Swedish", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Tahitian:
                params = "&to=";
                Toast.makeText(getApplicationContext(), "Tahitian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Tamil:
                params = "&to=ta";
                Toast.makeText(getApplicationContext(), "Tamil", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Thai:
                params = "&to=th";
                Toast.makeText(getApplicationContext(), "Thai", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Tongan:
                params = "&to=to";
                Toast.makeText(getApplicationContext(), "Tongan", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Turkish:
                params = "&to=";
                Toast.makeText(getApplicationContext(), "Turkish", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Ukrainian:
                params = "&to=uk";
                Toast.makeText(getApplicationContext(), "Ukrainian", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Urdu:
                params = "&to=ur";
                Toast.makeText(getApplicationContext(), "Urdu", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Vietnamese:
                params = "&to=vi";
                Toast.makeText(getApplicationContext(), "Vietnamese", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Welsh:
                params = "&to=cy";
                Toast.makeText(getApplicationContext(), "Welsh", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            case R.id.Yucatec_Maya:
                params = "&to=yua";
                Toast.makeText(getApplicationContext(), "Yucatec Maya", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;

            default:
                params = "&to=es";
                Toast.makeText(getApplicationContext(), "Spanish", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.END);
                break;
        }
        
        
        
        

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.v("EMSpeak", "text input is: " + result.get(0));
                    et.setText((result.get(0)));


                }
                break;
        }
    }
}
