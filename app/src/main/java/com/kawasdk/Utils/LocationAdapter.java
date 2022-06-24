package com.kawasdk.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kawasdk.Fragment.fragmentFarmLocation;
import com.kawasdk.Model.LocationModel.LocationModel;
import com.kawasdk.R;

import java.util.List;

import retrofit2.Callback;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private LocationItemClickListener mClickListener;

    // data is passed into the constructor
    public LocationAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.search_cutom_view, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String animal = mData.get(position);
        holder.myTextView.setText(animal);

    }

    public void setItems(List<String> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.placeName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onLocationItemClick1(view, getBindingAdapterPosition());

        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(fragmentFarmLocation itemClickListener) {
        this.mClickListener = itemClickListener;
        //        this.mClickListener = (LocationItemClickListener) itemClickListener;

    }

    // parent activity will implement this method to respond to click events
    public interface LocationItemClickListener {
        void onLocationItemClick1(View view, int position);
    }
}
