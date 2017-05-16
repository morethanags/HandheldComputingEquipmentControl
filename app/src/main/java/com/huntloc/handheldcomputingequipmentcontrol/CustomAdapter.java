package com.huntloc.handheldcomputingequipmentcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Debug;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class CustomAdapter extends BaseAdapter {
    Context context;
    private static LayoutInflater inflater = null;
    private JSONArray jsonArray;

    public CustomAdapter(EquipmentActivity activity, JSONArray jsonArray) {
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

    public class Holder {
        TextView typebrand, serial, observation;
        ImageView photo;
        ImageButton button;
    }

    public View getView(final int position, View row, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.equipment_list_row, null);
        holder.typebrand = (TextView) rowView.findViewById(R.id.typebrand);
        holder.serial = (TextView) rowView.findViewById(R.id.serial);
        holder.observation = (TextView) rowView.findViewById(R.id.observation);
        holder.photo = (ImageView) rowView.findViewById(R.id.photo);
        holder.button = (ImageButton) rowView.findViewById(R.id.buttonMenu);
        String typebrand = "", serial = "", observation = "", photo = "";
        final ImageButton popUp_btn = holder.button;

        try {
            typebrand = jsonArray.getJSONObject(position).getJSONObject("Type").optString("Description") + " " + jsonArray.getJSONObject(position).optString("Brand");
            if (!jsonArray.getJSONObject(position).isNull("SerialNumber") && !jsonArray.getJSONObject(position).optString("SerialNumber").equals("null")) {
                serial = jsonArray.getJSONObject(position).optString("SerialNumber");
            }
            if (!jsonArray.getJSONObject(position).isNull("Observation") && !jsonArray.getJSONObject(position).optString("Observation").equals("null")) {
                observation = jsonArray.getJSONObject(position).optString("Observation");
            }
            holder.typebrand.setText(typebrand);
            holder.serial.setText(serial);
            holder.observation.setText(observation);

            if (!jsonArray.getJSONObject(position).isNull("Photo") && !jsonArray.getJSONObject(position).optString("Photo").equals("null")) {
                byte[] byteArray;
                Bitmap bitmap;
                byteArray = Base64
                        .decode(jsonArray.getJSONObject(position).optString("Photo"), 0);
                bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                        byteArray.length);
                holder.photo.setImageBitmap(bitmap);
            } else {
                holder.photo.setImageResource(R.mipmap.ic_photo_default);
            }

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popup = new PopupMenu(context, popUp_btn);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int i = item.getItemId();
                            JSONObject equipment = null;
                            try {
                                equipment = jsonArray.getJSONObject(position);
                            } catch (Exception e) {
                            }
                            if (i == R.id.action_entrance) {
                                ((EquipmentActivity)context).logEquipment(equipment, 1);
                                return true;
                            } else if (i == R.id.action_exit) {
                                ((EquipmentActivity)context).logEquipment(equipment, 0);
                                return true;
                            } else if (i == R.id.action_edit) {
                                ((EquipmentActivity)context).showPopupWindow(equipment);
                                return true;
                            } else if (i == R.id.action_delete) {
                                ((EquipmentActivity)context).deleteEquipment(equipment);
                                return true;
                            } else {
                                return onMenuItemClick(item);
                            }
                        }
                    });

                    //popup.getMenuInflater().inflate(R.menu.equipment_item_menu, popup.getMenu());
                    popup.inflate(R.menu.equipment_item_menu);
                    View menuItemView = popUp_btn;//((EquipmentActivity)context).findViewById(R.id.buttonMenu);
                    MenuPopupHelper menuHelper = new MenuPopupHelper(context, (MenuBuilder) popup.getMenu(), menuItemView);
                    menuHelper.setForceShowIcon(true);
                    menuHelper.show();
                    //popup.show();
                }
            });

        } catch (Exception e) {
        }

        return rowView;
    }

}
