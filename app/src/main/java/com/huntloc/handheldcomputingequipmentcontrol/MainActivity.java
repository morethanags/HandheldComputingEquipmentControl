package com.huntloc.handheldcomputingequipmentcontrol;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static long back_pressed;
    private NfcAdapter mNfcAdapter;
    private EditText mCredentialId;
    public static final String PERSONNEL_MESSAGE = "com.huntloc.handheldcomputingequipmentcontrol.PERSONNEL";
    private Button buttonCheck;
    final int REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable NFC.", Toast.LENGTH_LONG)
                    .show();
        }

        View view = findViewById(android.R.id.content);
        mCredentialId = (EditText) view
                .findViewById(R.id.editText_CredentialId);
        mCredentialId.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (mCredentialId.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this,
                                "Tap a Badge or Enter Credential ID",
                                Toast.LENGTH_LONG).show();
                    } else {
                        sendRequest();
                    }
                    return true;
                }
                return false;
            }
        });

        buttonCheck = (Button) view.findViewById(R.id.button_Register);
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCredentialId.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Tap a Badge or Enter Credential ID",
                            Toast.LENGTH_LONG).show();
                } else {
                    sendRequest();
                    hideKeyboard();
                }

            }
        });
        handleIntent(getIntent());
    }


    private void setCredentialId(String id) {
        mCredentialId.setText(id);
        if (!id.isEmpty()) {
            String serverURL = getResources().getString(
                    R.string.ccure_service_url)
                    + "/SwapBadge/"
                    + id
                    + "/"
                    + UUID.randomUUID().toString();
            new QueryBadgeTask().execute(serverURL);
            Log.d("URL Badge", serverURL);
        }
    }
    @Override
    public boolean onNavigateUpFromChild(Activity child) {
        setCredentialId("");
        return super.onNavigateUpFromChild(child);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }
   public void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(),
                activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                activity.getApplicationContext(), 0, intent, 0);
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
        filters[0] = new IntentFilter();
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);

        /*filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
         try {
             filters[0].addDataType(MIME_TEXT_PLAIN);
         } catch (IntentFilter.MalformedMimeTypeException e) {
             throw new RuntimeException("Check your mime type.");
         }*/

       /* filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        techList = new String[][]{new String[]{NfcA.class.getName()}, new String[]{MifareClassic.class.getName()}, new String[]{NdefFormatable.class.getName()}};*/

        adapter.enableForegroundDispatch(activity, pendingIntent, filters,  techList);
    }

    public void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d("action", action);
       if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable parcelable = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Tag tag = (Tag) parcelable;
            byte[] id = tag.getId();
            String code = getDec(id) + "";
            Log.d("Internal Code", code);
            setCredentialId(code);
        }

         /*   if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                NdefMessage ndefMessage = null;
                Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if ((rawMessages != null) && (rawMessages.length > 0)) {
                    ndefMessage = (NdefMessage) rawMessages[0];
                    String result = "";
                    byte[] payload = ndefMessage.getRecords()[0].getPayload();
                    String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                    int languageCodeLength = payload[0] & 0077;
                    //String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                    String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
                    Log.d("Internal Code", text);
                    setCredentialId(text);

                }
            }*/

       /* if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Parcelable parcelable = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Tag tag = (Tag) parcelable;
            byte[] id = tag.getId();
            String code = getDec(id) + "";
            Log.d("Internal Code", code);
            setCredentialId(code);

        }*/

    }
    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }
    protected void displayEquipment(String result) {
        Intent intent = new Intent(this, EquipmentActivity.class);
        intent.putExtra(PERSONNEL_MESSAGE, result);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void sendRequest() {
        String serverURL = getResources().getString(R.string.service_url)
                + "/PersonnelService/Retrieve/" + mCredentialId.getText().toString();
        Log.d("URL personnel", serverURL);
        new QueryPersonnelTask().execute(serverURL);
    }
    private class QueryBadgeTask extends AsyncTask<String, String, String> {
        String printedCode = "";
        HttpURLConnection urlConnection;


        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                MainActivity.this
                        .runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                alertDialogBuilder.setTitle("Computing Equipment Control");
                                alertDialogBuilder.setMessage("Red WiFi no Disponible");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                alertDialogBuilder.create().show();
                            }
                        });
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {
            try {
                Log.d("Result",result);
                if (!result.equals("")) {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.isNull("PrintedCode")) {
                        MainActivity.this
                                .runOnUiThread(new Runnable() {
                                    public void run() {
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                        alertDialogBuilder.setTitle("Computing Equipment Control");
                                        alertDialogBuilder.setMessage("Credencial no Válida");
                                        alertDialogBuilder.setCancelable(false);
                                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                        alertDialogBuilder.create().show();
                                    }
                                });
                        return;
                    } else {
                        printedCode = jsonResponse.optString("PrintedCode");
                        MainActivity.this
                                .runOnUiThread(new Runnable() {
                                    public void run() {
                                        MainActivity.this.mCredentialId.setText(printedCode);
                                        Log.d("Printed Code",printedCode);
                                        sendRequest();
                                    }
                                });
                    }
                }
            } catch (Exception ex) {

            }
        }
    }
    private class QueryPersonnelTask extends AsyncTask<String, String, String> {
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setTitle("Computing Equipment Control");
                        alertDialogBuilder.setMessage("Red WiFi no Disponible");
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        alertDialogBuilder.create().show();
                    }
                });
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }
        protected void onPostExecute(String result) {
            Log.d("Result", result);
            try {
                if (result!=null && !result.equals("")) {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.isNull("Credential")) {
                        MainActivity.this
                                .runOnUiThread(new Runnable() {
                                    public void run() {
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                        alertDialogBuilder.setTitle("Computing Equipment Control");
                                        alertDialogBuilder.setMessage("Credencial no Válida");
                                        alertDialogBuilder.setCancelable(false);
                                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                        alertDialogBuilder.create().show();
                                    }
                                });
                        return;
                    }
                    else{
                        MainActivity.this.displayEquipment(result);
                    }
                }
            } catch (Exception ex) {

            }
        }
    }
}
