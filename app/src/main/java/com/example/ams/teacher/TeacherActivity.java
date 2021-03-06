package com.example.ams.teacher;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ams.others.AppStatus;
import com.example.ams.others.BaseActivity;
import com.example.ams.R;
import com.example.ams.others.RecyclerTouchListener;
import com.example.ams.student.StudentDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class TeacherActivity extends BaseActivity {
    FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    ArrayList<String> stringArrayList = new ArrayList<>();
    String verified;

    private SwipeRefreshLayout pullToRefresh;
    private IntentIntegrator qrScan;
    ArrayList<TeacherSubjectDetail> recievedList = new ArrayList<>();
    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mAuth = FirebaseAuth.getInstance();
        //to display the list of subjects of the teacher
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setVisibility(View.VISIBLE);
        ImageButton profileButton = (ImageButton)findViewById(R.id.profileButton);
        Button scanStudentQr = (Button)findViewById(R.id.scanStudentQr);
        pullToRefresh = (SwipeRefreshLayout)findViewById(R.id.pullToRefresh);
        qrScan = new IntentIntegrator(this);

        scanStudentQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.initiateScan();
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

                    Toast.makeText(getApplicationContext(),"You are not online!!!!",Toast.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(TeacherActivity.this, TeacherProfile.class);
                    startActivity(intent);
                }
            }
        });

        GetProfileDetails getProfileDetails = new GetProfileDetails();
        getProfileDetails.execute();

        final LinearLayout linlaHeaderProgress = (LinearLayout) findViewById(R.id.progressBar);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        String userid=user.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("teachers");


        reference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                linlaHeaderProgress.setVisibility(View.GONE);
                //fetch data from Firebase database corresponding to current user
                GenericTypeIndicator<ArrayList<TeacherSubjectDetail>> t = new GenericTypeIndicator<ArrayList<TeacherSubjectDetail>>() {};
                 recievedList = dataSnapshot.getValue(t);
              //  stringArrayList = new ArrayList<>();

                TeacherSubjectAdapter adapter = new TeacherSubjectAdapter(getApplicationContext(), recievedList);

                //setting adapter to recyclerview
                recyclerView.setAdapter(adapter);
                TextView textView = (TextView) findViewById(R.id.countSubjectTextView);
                if(recievedList!=null)
                    textView.setText(recievedList.size() + " subjects");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

                        Toast.makeText(getApplicationContext(),"You are not online!!!!",Toast.LENGTH_LONG).show();
                    }else {
                        Intent intent = new Intent(TeacherActivity.this, TeacherTakeAttendance.class);
                        intent.putExtra("TeacherSubjectDetail", recievedList.get(position));
                        startActivity(intent);
                    }
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));


        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                pullToRefresh.setRefreshing(false);
                finish();
                startActivity(getIntent());
            }
        });


        String SHOWCASE_ID = "Teacher Activity";
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);
        sequence.addSequenceItem(pullToRefresh, "Pull to refresh", "GOT IT");
        sequence.addSequenceItem(recyclerView, "Click To manage attendance", "GOT IT");
        sequence.addSequenceItem(profileButton, "Click To view Your Profile", "GOT IT");
        sequence.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(result.getContents());
                    String deviceId = obj.getString("deviceId");
                    Intent intent = new Intent(TeacherActivity.this, StudentDetail.class);
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    private class GetProfileDetails extends AsyncTask<String, String , String > {
        String userId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Please Wait..\nWhile we verify");
            userId = mAuth.getCurrentUser().getUid();
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userId", userId);

            String link = BASE_URL + "get_teacher_details.php";

            Set set = params.entrySet();
            Iterator iterator = set.iterator();
            try {
                String data = "";
                while (iterator.hasNext()) {
                    Map.Entry mEntry = (Map.Entry) iterator.next();
                    data += URLEncoder.encode(mEntry.getKey().toString(), "UTF-8") + "=" +
                            URLEncoder.encode(mEntry.getValue().toString(), "UTF-8");
                    data += "&";
                }

                if (data != null && data.length() > 0 && data.charAt(data.length() - 1) == '&') {
                    data = data.substring(0, data.length() - 1);
                }
                Log.d("Debug", data);

                URL url=new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Accept","*/*");
                OutputStream out = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                Log.d("data", data);
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());


                String result = convertStreamToString(inputStream);
                httpURLConnection.disconnect();
                Log.d("TAG",result);
                return result;
            }
            catch(Exception e){
                Log.d("debug",e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        private String convertStreamToString(InputStream inputStream) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder("");
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //if string returned from doinbackground is null, that means Exception occured while connectioon to server
            hideProgressDialog();
            if(s==null){
                Toast.makeText(TeacherActivity.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
                //if could not know whether the current user is student or teacher
            }
            else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = null;
                try {
                    jsonObject = (JSONObject) parser.parse(s);
                }catch(ParseException e){
                    e.printStackTrace();
                }
                int successCode = 0;
                if(jsonObject!=null) {
                    Object p = jsonObject.get("success");
                    successCode = Integer.parseInt(p.toString());
                }
                if( jsonObject!=null && successCode==0){
                    Toast.makeText(TeacherActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
                    //if could not know whether the current user is student or teacher

                }
                else{

                    verified = jsonObject.get("verified").toString();
                    //Toast.makeText(getApplicationContext(), verified, Toast.LENGTH_LONG).show();
                    Log.d("verify", verified);
                    if(verified.equals("0")){
                        TextView displayText = (TextView)findViewById(R.id.displayText);
                        displayText.setText("You are a teacher\n and yet to be verified");
                        TextView textView = (TextView) findViewById(R.id.countSubjectTextView);
                        textView.setText("");
                        displayText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                        //recyclerView.setClickable(false);
                    }

                    else {
                        recyclerView.setVisibility(View.VISIBLE);
                        TextView displayText = (TextView)findViewById(R.id.displayText);
                        displayText.setVisibility(View.GONE);
                    }

                }
            }
        }
    }
}
