package com.lostnfound.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
    private final Context context;
    private List<ItemModel> itemList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(ItemModel item); }
    public ItemsAdapter(Context context, List<ItemModel> itemList, OnItemClickListener listener) {
        this.context = context; this.itemList = itemList; this.listener = listener;
    }
    public void updateList(List<ItemModel> list) { this.itemList = list; notifyDataSetChanged(); }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list_row, p, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        ItemModel item = itemList.get(pos);
        holder.title.setText(item.getTitle());
        holder.cat.setText(item.getCategory().toUpperCase());
        holder.loc.setText("Location: " + item.getLocation());
        holder.date.setText("Published: " + item.getDateString());
        holder.status.setText(item.getStatus());
        if ("LOST".equalsIgnoreCase(item.getStatus())) {
            holder.status.setBackgroundColor(Color.parseColor("#F9DEDC"));
            holder.status.setTextColor(Color.parseColor("#B3261E"));
        } else {
            holder.status.setBackgroundColor(Color.parseColor("#E8DEF8"));
            holder.status.setTextColor(Color.parseColor("#1D192B"));
        }
        String b64 = item.getBase64Image();
        if (b64 != null && !b64.isEmpty()) {
            try {
                byte[] dec = Base64.decode(b64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                if (bmp != null) holder.thumbnail.setImageBitmap(bmp);
            } catch (Exception e) { holder.thumbnail.setImageResource(android.R.drawable.ic_menu_gallery); }
        } else {
            holder.thumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }
    @Override
    public int getItemCount() { return itemList.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, cat, status, loc, date; ImageView thumbnail;
        public ViewHolder(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.txtRowTitle);
            cat = v.findViewById(v.getContext().getResources().getIdentifier("txtRowCategory", "id", v.getContext().getPackageName()));
            status = v.findViewById(R.id.txtRowStatus);
            loc = v.findViewById(R.id.txtRowLocation);
            date = v.findViewById(R.id.txtRowDate);
            thumbnail = v.findViewById(R.id.itemThumbnail);
        }
    }
}