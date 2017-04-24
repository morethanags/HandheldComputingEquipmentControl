package com.huntloc.handheldcomputingequipmentcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;

public class CustomAdapter extends BaseAdapter{
    Context context;
    private static LayoutInflater inflater=null;
    private JSONArray jsonArray;
    public CustomAdapter(EquipmentActivity activity, JSONArray jsonArray) {
       this.jsonArray = jsonArray;
        context = activity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
       return jsonArray.length();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
         return position;
    }

    public class Holder
    {
        TextView typebrand, serial, observation;
        ImageView photo;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.equipment_list_row, null);
        holder.typebrand = (TextView) rowView.findViewById(R.id.typebrand);
        holder.serial = (TextView) rowView.findViewById(R.id.serial);
        holder.observation = (TextView) rowView.findViewById(R.id.observation);
        holder.photo=(ImageView) rowView.findViewById(R.id.photo);
        String typebrand = "", serial = "", observation = "", photo = "";
        try {
            typebrand = jsonArray.getJSONObject(position).getJSONObject("Type").optString("Description") + " "+jsonArray.getJSONObject(position).optString("Brand");
            if(!jsonArray.getJSONObject(position).isNull("SerialNumber")&&!jsonArray.getJSONObject(position).optString("SerialNumber").equals("null")){
                serial = jsonArray.getJSONObject(position).optString("SerialNumber");
            }
            if(!jsonArray.getJSONObject(position).isNull("Observation")&&!jsonArray.getJSONObject(position).optString("Observation").equals("null")){
                observation = jsonArray.getJSONObject(position).optString("Observation");
            }
            holder.typebrand.setText(typebrand);
            holder.serial.setText(serial);
            holder.observation.setText(observation);

            if ( !jsonArray.getJSONObject(position).isNull("Photo")&&!jsonArray.getJSONObject(position).optString("Photo").equals("null")) {
                byte[] byteArray;
                Bitmap bitmap;
                byteArray = Base64
                        .decode(jsonArray.getJSONObject(position).optString("Photo"), 0);
                bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                        byteArray.length);
                holder.photo.setImageBitmap(bitmap);
            }
            else{
                holder.photo.setImageResource(R.mipmap.ic_photo_default);
            }
        }catch (Exception e){
        }

        return rowView;
    }

}
