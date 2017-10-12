package com.huntloc.handheldcomputingequipmentcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomAdapterLog extends BaseAdapter {
    private static LayoutInflater inflater = null;
    Context context;
    private JSONArray jsonArray;

    public CustomAdapterLog(MainActivity activity, JSONArray jsonArray) {
        this.jsonArray = jsonArray;
        context = activity;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return jsonArray.length();
    }

    @Override
    public Object getItem(int position) {
        Object toReturn = null;
        try {
            toReturn = jsonArray.getJSONObject(position);
        } catch (Exception e) {
        }
        return toReturn;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View row, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.equipment_log_list_row, null);
        holder.typebrand = (TextView) rowView.findViewById(R.id.typebrand);
        holder.name = (TextView) rowView.findViewById(R.id.name);
        holder.log = (TextView) rowView.findViewById(R.id.log);
        holder.time = (TextView) rowView.findViewById(R.id.time);
        String typebrand = "", name = "", log = "", time="", serial = "";

        try {
            name = jsonArray.getJSONObject(position).optString("Name");
            typebrand = jsonArray.getJSONObject(position).getJSONObject("ComputingEquipment").getJSONObject("ComputingEquipmentType").optString("Description") + " " +
                    jsonArray.getJSONObject(position).getJSONObject("ComputingEquipment").optString("Brand")+
             " Serial:"+    jsonArray.getJSONObject(position).getJSONObject("ComputingEquipment").optString("SerialNumber");
            log = jsonArray.getJSONObject(position).getJSONObject("LogType").optString("Description");
            time = jsonArray.getJSONObject(position).optString("LogDate");

            holder.typebrand.setText(typebrand);
            holder.name.setText(name);
            holder.log.setText(log);
            holder.time.setText(time);


        } catch (Exception e) {
        }

        return rowView;
    }

    public class Holder {
        TextView typebrand, name, log, time;
    }
}
