/*
 * Nom de classe : MyItemRecyclerViewAdapter
 *
 * Description   : adapter de la liste des lieux d'un utilisateur
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.settings;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fr.abitbol.service4night.MapLocation;
import fr.abitbol.service4night.databinding.FragmentUserLocationsBinding;
import fr.abitbol.service4night.listeners.OnItemClickedListener;


import java.util.List;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter logging";
    private final List<MapLocation> mValues;
    private OnItemClickedListener listener;
    public MyItemRecyclerViewAdapter(List<MapLocation> items, OnItemClickedListener _listener) {
        mValues = items;
        listener = _listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder called");
        return new ViewHolder(FragmentUserLocationsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder called ");
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getName());
        holder.mContentView.setText(mValues.get(position).getDescription());
        holder.view.setOnClickListener(view -> {
            Log.i(TAG, "onBindViewHolder: item clicked");
            listener.onItemClicked(position, holder.mItem);

        });

        
    }
    


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public MapLocation mItem;
        public View view;

        public ViewHolder(FragmentUserLocationsBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
            view = binding.getRoot();
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}