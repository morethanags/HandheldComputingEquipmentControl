package com.huntloc.handheldcomputingequipmentcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VisitorsFragment extends Fragment {

    private EditText mCredentialId;
    private Button buttonCheck;
    private OnVisitorsFragmentInteractionListener mListener;


    // TODO: Rename and change types and number of parameters
    public static VisitorsFragment newInstance() {
        VisitorsFragment fragment = new VisitorsFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private void sendRequest() {
        String serverURL = getResources().getString(R.string.service_url)
                + "/VisitorService/Retrieve/" + mCredentialId.getText().toString();
        Log.d("URL personnel", serverURL);
        new VisitorsFragment.QueryVisitorTask(this).execute(serverURL);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visitors, container, false);
        Log.d("onCreateView", "hera");
        mCredentialId = (EditText) view
                .findViewById(R.id.editText_VisitorId);
        mCredentialId.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (mCredentialId.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(),
                                "Enter Id Number",
                                Toast.LENGTH_LONG).show();
                    } else {
                        sendRequest();
                    }
                    return true;
                }
                return false;
            }
        });
        buttonCheck = (Button) view.findViewById(R.id.button_Visitor);
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCredentialId.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(),
                            "Enter Id Number",
                            Toast.LENGTH_LONG).show();
                } else {
                    sendRequest();
                }

            }
        });
        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVisitorsFragmentInteractionListener) {
            mListener = (OnVisitorsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void displayEquipment(String result) {
        Intent intent = new Intent(getActivity(), EquipmentActivity.class);
        intent.putExtra(MainActivity.PERSONNEL_MESSAGE, result);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        mCredentialId.setText("");
    }
    private void createVisitor() {
        FragmentManager manager = getActivity().getFragmentManager();
        CreateVisitorDialogFragment dialog = new CreateVisitorDialogFragment();
        dialog.setFragment(this);
        dialog.show(manager, "dialog");
    }
    public interface OnVisitorsFragmentInteractionListener {

        void onVisitorsFragmentInteraction(Uri uri);
    }

    private class QueryVisitorTask extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        private WeakReference<VisitorsFragment> visitorsFragmentWeakReference;

        private QueryVisitorTask(VisitorsFragment fragment) {
            this.visitorsFragmentWeakReference = new WeakReference<VisitorsFragment>(fragment);
        }
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
                visitorsFragmentWeakReference.get().getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(visitorsFragmentWeakReference.get().getActivity());
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
                    if (jsonResponse.isNull("DocumentId")) {
                        visitorsFragmentWeakReference.get().getActivity()
                            .runOnUiThread(new Runnable() {
                                public void run() {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(visitorsFragmentWeakReference.get().getActivity());
                                    alertDialogBuilder.setTitle("Computing Equipment Control");
                                    alertDialogBuilder.setMessage("Visitor not registered, create new.");
                                    alertDialogBuilder.setCancelable(false);
                                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            visitorsFragmentWeakReference.get().createVisitor();
                                        }
                                    });
                                    alertDialogBuilder.create().show();
                                }
                            });
                        return;
                    }
                    else{
                        JSONObject visitor;
                        visitor = new JSONObject();
                        visitor.put("PersonnelName", jsonResponse.opt("LastName")+", "+jsonResponse.opt("Name"));
                        visitor.put("Credential", "0000");
                        visitor.put("DocumentId", jsonResponse.opt("DocumentId"));
                        visitorsFragmentWeakReference.get().displayEquipment(visitor.toString());
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    public static class CreateVisitorDialogFragment extends DialogFragment implements View.OnClickListener {
        Button saveButton, cancelButton;
        Spinner documentType;
        EditText name, lastName, documentId;
        JSONObject visitor;
        private WeakReference<VisitorsFragment> visitorsFragmentWeakReference;
        public void setFragment(VisitorsFragment fragment){
            this.visitorsFragmentWeakReference = new WeakReference<VisitorsFragment>(fragment);
        }
        public CreateVisitorDialogFragment() {

        }
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            setCancelable(false);
            getDialog().setTitle("New Visitor");

            View view = inflater.inflate(R.layout.visitor_popup_window, null, false);

            name = (EditText) view.findViewById(R.id.editText_Name);
            lastName = (EditText) view.findViewById(R.id.editText_LastName);
            documentId = (EditText) view.findViewById(R.id.editText_DocumentId);

            saveButton = (Button) view.findViewById(R.id.ib_save_visitor);
            cancelButton = (Button) view.findViewById(R.id.ib_cancel_visitor);
            saveButton.setOnClickListener(this);
            cancelButton.setOnClickListener(this);
            documentType = (Spinner) view.findViewById(R.id.document_types_spinner);
            String serverURL = getResources().getString(R.string.service_url)
                    + "/DocumentTypeService/Retrieve";
            Log.d("URL types", serverURL);

            new  CreateVisitorDialogFragment.QueryTypesTask().execute(serverURL);


            return view;
        }
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ib_save_visitor:
                    save();
                    break;
                case R.id.ib_cancel_visitor:
                    dismiss();
                    break;

            }
        }
        private void save(){

            if(TextUtils.isEmpty(lastName.getText().toString().trim())){
                lastName.setError("Enter last name.");
                return;
            }
            if(TextUtils.isEmpty(name.getText().toString().trim())){
                name.setError("Enter name.");
                return;
            }
            if(TextUtils.isEmpty(documentId.getText().toString().trim())){
                documentId.setError("Enter equipment serial number.");
                return;
            }
            try {
                this.visitor = new JSONObject();
                this.visitor.put("Name", name.getText().toString().toUpperCase());
                this.visitor.put("LastName", lastName.getText().toString().toUpperCase());
                DocumentType selected = (DocumentType) documentType.getSelectedItem();
                this.visitor.put("DocumentTypeId", selected.getDocumentTypeId());
                this.visitor.put("DocumentId", documentId.getText().toString().toUpperCase());
            } catch (JSONException je) {

            }

                String serverURL = getResources().getString(R.string.service_url)
                        + "/VisitorService/Create";
                new SaveVisitorTask().execute(serverURL);

        }
        private void showTypes(JSONArray jsonArray) {
            List<VisitorsFragment.CreateVisitorDialogFragment.DocumentType> list = new ArrayList<>();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(new VisitorsFragment.CreateVisitorDialogFragment.DocumentType(jsonArray.getJSONObject(i).getString("Description"), jsonArray.getJSONObject(i).getInt("DocumentTypeId")));
                }
            } catch (Exception e) {
            }
            ArrayAdapter<VisitorsFragment.CreateVisitorDialogFragment.DocumentType> adapter = new ArrayAdapter<VisitorsFragment.CreateVisitorDialogFragment.DocumentType>(getActivity().getApplicationContext(),android.R.layout.simple_spinner_item, list );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            documentType.setAdapter(adapter);
        }
        private class DocumentType {
            private String Description;
            private int DocumentTypeId;


            public int getDocumentTypeId() {
                return DocumentTypeId;
            }

            public DocumentType(String description, int id) {
                Description = description;
                DocumentTypeId = id;
            }

            public String getDescription() {
                return Description;
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
                            VisitorsFragment.CreateVisitorDialogFragment.this.showTypes(jsonResponse);
                            return;
                        }
                    }
                } catch (Exception ex) {

                }
            }
        }
        private class SaveVisitorTask extends AsyncTask<String, String, String>{
            HttpURLConnection urlConnection;
            @SuppressWarnings("unchecked")
            protected String doInBackground(String... args) {
                StringBuilder result = new StringBuilder();
                try {
                    URL url = new URL(args[0]);
                    //Log.d("URL",url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    //Log.d("ComputingEquipment", equipment.toString());
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(visitor.toString().getBytes("UTF-8"));
                    os.close();

                    /*OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(equipment.toString());*/

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                } catch (IOException e) {
                    Log.d("Exception1", e.toString());
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
                                CreateVisitorDialogFragment.this.getActivity(), "Visitor saved", Toast.LENGTH_LONG)
                                .show();
                        JSONObject jsonResponse = new JSONObject(result);
                        JSONObject visitor;
                        visitor = new JSONObject();
                        visitor.put("PersonnelName", jsonResponse.opt("LastName")+", "+jsonResponse.opt("Name"));
                        visitor.put("Credential", "0000");
                        visitor.put("DocumentId", jsonResponse.opt("DocumentId"));

                        CreateVisitorDialogFragment.this.visitorsFragmentWeakReference.get().displayEquipment(visitor.toString());
                        CreateVisitorDialogFragment.this.dismiss();
                    }
                } catch (Exception ex) {

                }
            }
        }
    }
}
