package com.huntloc.handheldcomputingequipmentcontrol;

import android.content.Intent;
import android.os.AsyncTask;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class EquipmentActivity extends AppCompatActivity {
    ListView equipmentListView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = findViewById(android.R.id.content);
        Intent intent = getIntent();
        String response = intent.getStringExtra(MainActivity.PERSONNEL_MESSAGE);
        String personnelName = "", credential = "", documentId ="";
        try {
            JSONObject jsonResponse = new JSONObject(response);
            personnelName = jsonResponse.optString("PersonnelName");
            credential = jsonResponse.optString("Credential");
            documentId = jsonResponse.optString("DocumentId");
        } catch (Exception e) {
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(personnelName);
        actionBar.setSubtitle("Badge Id: "+credential);
        setContentView(R.layout.activity_equipment);
        equipmentListView = (ListView) view.findViewById(R.id.list_Equipment);
        requestEquipment(documentId);
        /*new Handler().postDelayed(new Runnable() {
            public void run() {
                listEquipments();
            }
        }, 1000);*/
    }

    private void requestEquipment(String documentId) {

        String serverURL = getResources().getString(R.string.service_url)
                + "/ComputingEquipmentService/Retrieve/"+documentId+"/_";
        Log.d("URL personnel", serverURL);
        new QueryEquipmentTask().execute(serverURL);
    }
    private void showEquipment(JSONArray jsonArray){
        equipmentListView.setAdapter(new CustomAdapter(this, jsonArray));

    }
    private class QueryEquipmentTask extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                Log.d("Exception",e.getMessage());
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }
        protected void onPostExecute(String result) {
            Log.d("Result", result);
            try {
                if (result!=null && !result.equals("")) {
                    JSONArray jsonResponse = new JSONArray(result);
                    if (jsonResponse.length()>0) {
                        EquipmentActivity.this.showEquipment(jsonResponse);
                        return;
                    }
                    else{
                        Toast.makeText(getBaseContext(), "No equipment registered.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

}
