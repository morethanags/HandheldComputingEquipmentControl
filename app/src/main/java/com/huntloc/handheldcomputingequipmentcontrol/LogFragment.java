package com.huntloc.handheldcomputingequipmentcontrol;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class LogFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout swipeRefreshLayout;
    private OnLogFragmentInteractionListener mListener;

    public LogFragment() {
        // Required empty public constructor
    }


    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void onRefresh() {
        updateLog();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.list_Equipment_Log_Layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        updateLog();
        return view;
    }
    private void updateLog() {

        String serverURL = getResources().getString(R.string.service_url)
                + "/ComputingEquipmentLogService";
        Log.d("URL Log", serverURL);
        LogOperationTask logOperationTask = new LogOperationTask(
                this);

        logOperationTask.execute(serverURL);
        swipeRefreshLayout.setRefreshing(false);
    }
    // TODO: Rename method, update argument and hook method into UI event

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLogFragmentInteractionListener) {
            mListener = (OnLogFragmentInteractionListener) context;
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


    public interface OnLogFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLogFragmentInteraction(Uri uri);
    }
    private static class LogOperationTask extends
            AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        ArrayList<HashMap<String, String>> list;
        ListView entranceList = null;
        private WeakReference<LogFragment> logFragmentWeakReference;

        private LogOperationTask(LogFragment fragment) {
            this.logFragmentWeakReference = new WeakReference<LogFragment>(
                    fragment);

        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
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
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();

        }

        protected void onPostExecute(String result) {
            try {
                entranceList = (ListView) logFragmentWeakReference
                        .get().getView().findViewById(R.id.list_Log);
            } catch (NullPointerException nullPointerException) {
                return;
            }
            try {
                if (!result.equals("")) {
                    JSONArray jsonArray = new JSONArray(result);
                    entranceList.setAdapter(new CustomAdapterLog((MainActivity) logFragmentWeakReference
                            .get().getActivity(), jsonArray));
                }
            } catch (Exception e) {
            }
        }
    }
}
