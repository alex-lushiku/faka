package alexlushiku.faka;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GridLayoutAdapter extends RecyclerView.Adapter<GridLayoutAdapter.ViewHolder> {

    private Word[] mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<String> favoriteWords = new ArrayList<>();
    private String selectedColor = "#FFF3C5";

    // Size
    private int rows;

    // data is passed into the constructor
    GridLayoutAdapter(Context context, Word[] data, int rows) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.rows = rows;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grid_cell, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        WindowManager wm = (WindowManager) holder.itemView.
                getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point psize = new Point();
        display.getSize(psize);

        int screenWidth = psize.x;

        holder.word.setText(mData[position].getWord());
        holder.word.setMinWidth(screenWidth/rows);

        holder.word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                LayoutInflater inflater = (LayoutInflater) holder.itemView.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                final View bView = inflater.inflate(R.layout.word_descriptions, null);

                RecyclerView recyclerView = bView.findViewById(R.id.list_descriptions);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(bView.getContext());
                recyclerView.setLayoutManager(layoutManager);

                DescriptionsAdapter adapter = new DescriptionsAdapter(mData[position]);
                recyclerView.setAdapter(adapter);

                // Add description
                Button add = bView.findViewById(R.id.addMeaning);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                                Uri.parse("mailto:faka.suggestie@outlook.com"));

                        emailIntent.putExtra("subject", "Omschrijving: " + mData[position].getWord());
                        emailIntent.putExtra("body", "Omscrhijving:\n\n\nVoorbeeldzin:");

                        try {
                            view.getContext().startActivity(emailIntent);
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(view.getContext(),
                                    "Geen e-mail applicatie gevonden.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setView(bView);
                final AlertDialog dialog = builder.create();
                dialog.show();

                // Ads
                AdView mAdView = bView.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        });

        // Get SharedPrefs
        SharedPreferences prefs = holder.itemView.getContext()
                .getSharedPreferences("favorites", 0);
        int size = prefs.getInt("favorites_size", 0);

        String array[] = new String[size];

        for (int i = 0;i < size;i++)
            array[i] = prefs.getString("favorites_" + i, null);

        // Make favorite
        for (int i = 0;i < array.length;i++) {
            if (mData[position].getWord().equals(array[i])) {
                mData[position].setFavorite(true);

                if (favoriteWords.isEmpty())
                    favoriteWords.add(array[i]);
            }
        }

        if (mData[position].isFavorite())
            holder.word.setBackgroundColor(Color.parseColor(selectedColor));
        else
            holder.word.setBackgroundColor(Color.WHITE);

        holder.word.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // SharedPreferences
                SharedPreferences prefs =
                        holder.itemView.getContext().getSharedPreferences("favorites", 0);
                SharedPreferences.Editor editor = prefs.edit();

                if (mData[position].isFavorite()) {
                    favoriteWords.remove(mData[position].getWord());

                    mData[position].setFavorite(false);
                    holder.word.setBackgroundColor(Color.WHITE);

                    editor.putInt("favorites_size", favoriteWords.size());
                    for (int i = 0;i < favoriteWords.size();i++)
                        editor.putString("favorites_" + i, favoriteWords.get(i));

                    editor.commit();
                } else {
                    favoriteWords.add(mData[position].getWord());

                    mData[position].setFavorite(true);
                    holder.word.setBackgroundColor(Color.parseColor(selectedColor));


                    editor.putInt("favorites_size", favoriteWords.size());
                    for (int i = 0;i < favoriteWords.size();i++)
                        editor.putString("favorites_" + i, favoriteWords.get(i));

                    editor.commit();
                }

                return true;
            }
        });

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button word;
        StringBuilder sb;

        ViewHolder(View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.word);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Word getItem(int id) {
        return mData[id];
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setFilter(Word[] titles) {
        mData = titles;
        notifyDataSetChanged();
    }
}