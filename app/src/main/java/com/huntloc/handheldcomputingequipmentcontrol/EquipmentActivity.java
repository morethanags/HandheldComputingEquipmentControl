package com.huntloc.handheldcomputingequipmentcontrol;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EquipmentActivity extends AppCompatActivity {
    ListView equipmentListView = null;
    private String documentId = null, credential = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = findViewById(android.R.id.content);
        Intent intent = getIntent();
        String response = intent.getStringExtra(MainActivity.PERSONNEL_MESSAGE);
        String personnelName = "";
        try {
            JSONObject jsonResponse = new JSONObject(response);
            personnelName = jsonResponse.optString("PersonnelName");
            credential = jsonResponse.optString("Credential");
            documentId = jsonResponse.optString("DocumentId");
        } catch (Exception e) {
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(personnelName);
        actionBar.setSubtitle("Badge Id: " + credential);
        setContentView(R.layout.activity_equipment);
        equipmentListView = (ListView) view.findViewById(R.id.list_Equipment);
        requestEquipment();
        /*new Handler().postDelayed(new Runnable() {
            public void run() {
                listEquipments();
            }
        }, 1000);*/

    }

    public void newEquipment(MenuItem item) {
        try
        {
            JSONObject newEquipment =  new JSONObject();
            newEquipment.put("CredentialId",credential);
            newEquipment.put("OwnerDocumentId",documentId);
            showPopupWindow(newEquipment);
        }
        catch(JSONException je){}
    }

    public void deleteEquipment(JSONObject equipment){

        if(!equipment.isNull("GUID")){
            final String GUID =  equipment.optString("GUID");
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            Log.d("GUID",GUID);
                            String serverURL = getResources().getString(R.string.service_url)
                                    + "/ComputingEquipmentService/Delete/"+GUID;
                            new DeleteEquipmentTask().execute(serverURL);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Delete this equipment?");
            builder.setPositiveButton("Yes", dialogClickListener);
            builder.setNegativeButton("No", dialogClickListener);
            builder.show();
        }
    }
    public void logEquipment(JSONObject equipment, int log){
        String serverURL = getResources().getString(R.string.service_url)
                + "/ComputingEquipmentLogService/"
                + equipment.optString("GUID")
                + "/"
                + log;
        new LogOperationTask().execute(serverURL);
    }
    public void showPopupWindow(JSONObject equipment) {
        FragmentManager manager = getFragmentManager();
        EquipmentDialogFragment equipmentDialog = new EquipmentDialogFragment();
        equipmentDialog.setEquipment(equipment);
        equipmentDialog.setActivity(this);
        equipmentDialog.show(manager, "equipmentDialog");
    }
    public void requestEquipment(MenuItem item) {
        requestEquipment();
    }
    private void requestEquipment() {
        String serverURL = getResources().getString(R.string.service_url)
                + "/ComputingEquipmentService/Retrieve/" + documentId + "/_";
        Log.d("URL personnel", serverURL);
        new QueryEquipmentTask().execute(serverURL);
    }
    private void showEquipment(JSONArray jsonArray) {
        equipmentListView.setAdapter(new CustomAdapter(this, jsonArray));
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.equipment_main_menu, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
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
                Log.d("Exception", e.getMessage());
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {
            Log.d("Result", result);
            try {
                if (result != null && !result.equals("")) {
                    JSONArray jsonResponse = new JSONArray(result);
                    if (jsonResponse.length() > 0) {
                        EquipmentActivity.this.showEquipment(jsonResponse);
                        return;
                    } else {
                        Toast.makeText(getBaseContext(), "No equipment registered.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {

            }
        }
    }
    public static class EquipmentDialogFragment extends DialogFragment implements View.OnClickListener {
        Button saveButton, cancelButton, pickButton;
        Spinner spinner;
        JSONObject equipment;
        EditText brand, serial, observations;
        ImageView photo;
        EquipmentActivity activity;
        public void setEquipment(JSONObject equipment) {
            this.equipment = equipment;
        }
        public void setActivity(EquipmentActivity activity) {
            this.activity = activity;
        }
        public EquipmentDialogFragment() {
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            setCancelable(false);
            getDialog().setTitle("Computing Equipment");
            View view = inflater.inflate(R.layout.equipment_popup_window, null, false);

            brand = (EditText) view.findViewById(R.id.editText_Brand);
            serial = (EditText) view.findViewById(R.id.editText_Serial);
            observations = (EditText) view.findViewById(R.id.editText_Observations);
            photo = (ImageView) view.findViewById(R.id.imageView_photo);

            saveButton = (Button) view.findViewById(R.id.ib_save);
            cancelButton = (Button) view.findViewById(R.id.ib_cancel);
            pickButton = (Button) view.findViewById(R.id.ib_picture);
            saveButton.setOnClickListener(this);
            cancelButton.setOnClickListener(this);
            pickButton.setOnClickListener(this);
            photo.setOnClickListener(this);
            spinner = (Spinner) view.findViewById(R.id.types_spinner);
            String serverURL = getResources().getString(R.string.service_url)
                    + "/ComputingEquipmentService/Retrieve/Types";
            Log.d("URL types", serverURL);
            new QueryTypesTask().execute(serverURL);

            if(this.equipment!=null && !this.equipment.isNull("GUID")){

                brand.setText(this.equipment.optString("Brand"));
                if (!this.equipment.isNull("SerialNumber") && !this.equipment.optString("SerialNumber").equals("null")) {
                    serial.setText(this.equipment.optString("SerialNumber"));
                }
                if (!this.equipment.isNull("Observation") && !this.equipment.optString("Observation").equals("null")) {
                    observations.setText(this.equipment.optString("Observation"));
                }
                if (!this.equipment.isNull("Photo") && !this.equipment.optString("Photo").equals("null")) {
                    byte[] byteArray;
                    Bitmap bitmap;
                    byteArray = Base64
                            .decode(this.equipment.optString("Photo"), 0);
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                            byteArray.length);
                    photo.setImageBitmap(bitmap);
                } else {
                    photo.setImageResource(R.mipmap.ic_photo_default);
                }

            }
            return view;
        }
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ib_save:
                    save();
                    break;
                case R.id.ib_cancel:
                    dismiss();
                    break;
                case R.id.ib_picture:
                    pickImage();
                    break;
                case R.id.imageView_photo:
                    pickImage();
                    break;
            }
        }
        private void save(){

        try {
            equipment.put("Brand",brand.getText());
            equipment.put("SerialNumber",serial.getText());
            equipment.put("Observation", observations.getText());
            JSONObject type = new JSONObject();
            Type selected = (Type) spinner.getSelectedItem();
            type.accumulate("Id", selected.getId());
            type.accumulate("Description", selected.getDescription());
            equipment.put("Type", type);
        }catch (JSONException je){

        }
        if(!this.equipment.isNull("GUID")){
            String serverURL = getResources().getString(R.string.service_url)
                    + "/ComputingEquipmentService/Update/"+equipment.optString("GUID");
            new SaveEquipmentTask().execute(serverURL);
        }
        else
        {
            String serverURL = getResources().getString(R.string.service_url)
                    + "/ComputingEquipmentService/Create";
            new SaveEquipmentTask().execute(serverURL);
        }
        }
        static final int REQUEST_IMAGE_CAPTURE = 11;

        private String  pictureImagePath = "";

        private void pickImage() {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = timeStamp + ".jpg";

            pictureImagePath = Environment.getExternalStorageDirectory().toString()+"/ComputingEquipment/" + imageFileName;

            File file = new File(pictureImagePath);
            Uri outputFileUri = Uri.fromFile(file);

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
            super.onActivityResult(requestCode, resultCode, intent);
            Log.d("result code",resultCode+"");
            if(resultCode==0){
                return;
            }
            if (requestCode == REQUEST_IMAGE_CAPTURE ) {
                try {
                    Log.d("path",pictureImagePath);
                    File imgFile = new  File(pictureImagePath);
                    Log.d("exists",imgFile.exists()+"");
                    if(imgFile.exists()){
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.5), (int)(bitmap.getHeight()*0.5), true);
                        photo.setImageBitmap(resized);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        Log.d("Encoded",encoded);
                        equipment.put("Photo", encoded);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        }

        private void showTypes(JSONArray jsonArray) {
            List<Type> list = new ArrayList<>();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(new Type(jsonArray.getJSONObject(i).getString("Description"), jsonArray.getJSONObject(i).getInt("Id")));
                }
            } catch (Exception e) {
            }
            ArrayAdapter<Type> adapter = new ArrayAdapter<Type>(getContext(),android.R.layout.simple_spinner_item, list );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            if(this.equipment!=null && !this.equipment.isNull("GUID")){
                for (int position = 0; position < adapter.getCount(); position++) {
                    try{
                        if(((Type)adapter.getItem(position)).getId() ==  this.equipment.getJSONObject("Type").optInt("Id")) {
                            spinner.setSelection(position);
                        }}
                    catch (Exception e){}
                }
            }
        }

        private class Type {
            private String Description;
            private int Id;

            public Type(String description, int id) {
                Description = description;
                Id = id;
            }

            public String getDescription() {
                return Description;
            }

            public int getId() {
                return Id;
            }

            public String toString() {
                return Description;
            }
        }

        private class QueryTypesTask extends AsyncTask<String, String, String> {
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
                    Log.d("Exception", e.getMessage());
                } finally {
                    urlConnection.disconnect();
                }
                return result.toString();
            }

            protected void onPostExecute(String result) {
                try {
                    if (result != null && !result.equals("")) {
                        JSONArray jsonResponse = new JSONArray(result);
                        if (jsonResponse.length() > 0) {
                            EquipmentDialogFragment.this.showTypes(jsonResponse);
                            return;
                        }
                    }
                } catch (Exception ex) {

                }
            }
        }
        private class SaveEquipmentTask extends AsyncTask<String, String, String>{
            HttpURLConnection urlConnection;
            @SuppressWarnings("unchecked")
            protected String doInBackground(String... args) {
                StringBuilder result = new StringBuilder();
                try {
                    URL url = new URL(args[0]);
                    Log.d("URL",url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    Log.d("ComputingEquipment", equipment.toString());
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(equipment.toString().getBytes("UTF-8"));
                    os.close();

                    /*OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(equipment.toString());*/

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                } finally {
                    urlConnection.disconnect();
                }
                return result.toString();
            }
            protected void onPostExecute(String result) {
                try {
                    if (result != null && !result.equals("")) {
                        Log.d("Result", result);
                        Toast.makeText(
                                activity, "Equipment Saved", Toast.LENGTH_LONG)
                                .show();
                        activity.requestEquipment();
                        EquipmentDialogFragment.this.dismiss();
                    }
                } catch (Exception ex) {

                }
            }
        }
    }
    private class DeleteEquipmentTask extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;

        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                Log.d("Exception", e.getMessage());
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {
            Log.d("Result", result);
            try {
                if (result != null && !result.equals("")) {
                    Toast.makeText(
                            EquipmentActivity.this, "Equipment Deleted", Toast.LENGTH_LONG)
                            .show();
                    EquipmentActivity.this.requestEquipment();
                }
            } catch (Exception ex) {

            }
        }
    }
    private class LogOperationTask extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                Log.d("Log URL", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jsonResponse = new JSONObject(result);

                String log = jsonResponse.optString("log")
                        .contains("Entry") ? "Entrada" : "Salida";
                String response = jsonResponse.optString("records") + " " + log
                        + " Registrada";

                Toast.makeText(
                        EquipmentActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            } catch (JSONException e) {
            }

        }
    }
}
