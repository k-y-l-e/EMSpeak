package edu.wit.seniorproject.emspeak.emspeak;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddQuestionActivity extends AppCompatActivity{

    String new_question = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_question_main);

        final EditText input1 = findViewById(R.id.new_question_input);

        Button submit_btn = findViewById(R.id.submit_button);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("EMSpeak", "Submit button clicked, navigating to MainActivity");
                Intent intent = new Intent();
                intent.setClass(AddQuestionActivity.this, MainActivity.class);

                Bundle bundle = new Bundle();
                new_question = input1.getText().toString();
                bundle.putString("new_question", new_question);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
