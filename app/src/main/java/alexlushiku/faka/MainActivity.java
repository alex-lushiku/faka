package alexlushiku.faka;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.media.tv.TvContract;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    static ArrayList<Word> words = new ArrayList<>();
    static GridLayoutAdapter adapter;
    static ProgressBar progressBar;
    boolean favoritesChecked = false;

    private static Context context;
    private static Intent service;

    public static void fetchJSONFromFile() {
        Resources res = context.getResources();
        InputStream inputStream = res.openRawResource(R.raw.words);
        Scanner scanner = new Scanner(inputStream);
        StringBuilder builder = new StringBuilder();

        words.clear();

        SharedPreferences prefs = context.getSharedPreferences("words", 0);
        String pref = prefs.getString("words", null);

        if (pref == null || pref.isEmpty()) {
            while (scanner.hasNextLine())
                builder.append(scanner.nextLine());

            parseJSON(builder.toString());
        } else {
            parseJSON(prefs.getString("words", null));
        }
    }

    public static void parseJSON(String string) {

        words.clear();

        try {
            JSONObject root = new JSONObject(string);
            JSONArray JSONwords = root.getJSONArray("words");

            ArrayList<String> desc = new ArrayList<>();
            ArrayList<String> ex = new ArrayList<>();

            String[] descArr;
            String[] exArr;

            for (int i = 0;i < JSONwords.length();i++) {
                JSONObject word = JSONwords.getJSONObject(i);
                JSONArray description =  word.getJSONArray("description");
                JSONArray example = word.getJSONArray("example");

                for (int d = 0; d < description.length(); d++){
                    if (description.get(d).toString() != "null")
                        desc.add(description.get(d).toString());
                    else
                        desc.add("");

                    if (example.get(d).toString() != "null")
                        ex.add(example.get(d).toString());
                    else
                        ex.add("");
                }

                descArr = new String[desc.size()];
                exArr = new String[ex.size()];

                words.add(new Word(
                        word.getString("word"),
                        desc.toArray(descArr),
                        ex.toArray(exArr)));

                desc.clear();
                ex.clear();
            }

            adapter.setFilter(words.toArray(new Word[words.size()]));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(service);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Straatwoordenboek");
        setSupportActionBar(toolbar);

        service = new Intent(this, UpdateWordsService.class);
        startService(service);

        MainActivity.context = getApplicationContext();

        Word[] wordsArr = new Word[words.size()];
        wordsArr = words.toArray(wordsArr);

        // Grid
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenWidth = size.x;
        int rows = (int) Math.ceil(screenWidth/2/200/2+1);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_words);
        recyclerView.setLayoutManager(new GridLayoutManager(this, rows));
        adapter = new GridLayoutAdapter(this, wordsArr, rows);
        recyclerView.setAdapter(adapter);

        fetchJSONFromFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Search
        MenuItem item = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) item.getActionView();

        if (!favoritesChecked)
            searchView.setQueryHint("Zoek een woord...");
        else
            searchView.setQueryHint("Zoek in favorieten...");

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO: Show word on enter if it's equal to the query

                for (int i = 0;i < words.size();i++) {
                    String word = words.get(i).getWord().toLowerCase();

                    if (word.equals(query)) {
                        return false;
                    }

                    return false;
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                query = query.toLowerCase();
                ArrayList<Word> filter = new ArrayList<>();

                for (int i = 0;i < words.size();i++) {
                    if (favoritesChecked) {
                        if (words.get(i).isFavorite()) {
                            String word = words.get(i).getWord().toLowerCase();

                            if (word.contains(query))
                                filter.add(words.get(i));
                        }
                    } else {
                        String word = words.get(i).getWord().toLowerCase();

                        if (word.contains(query))
                            filter.add(words.get(i));
                    }
                }

                Word[] filtered = new Word[filter.size()];
                adapter.setFilter(filter.toArray(filtered));
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_suggestion:
                sendEmail();
                break;
            case R.id.action_favorites:
                filter(item);
                return true;
            case R.id.rate:
                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }
                break;
            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void filter(MenuItem item) {
        if (favoritesChecked) {
            item.setChecked(false);
            favoritesChecked = false;
        } else {
            favoritesChecked = true;
            item.setChecked(true);
        }

        ArrayList<Word> filter = new ArrayList<>();

        for (int i = 0;i < words.size();i++) {
            if (words.get(i).isFavorite())
                filter.add(words.get(i));
        }

        if (filter.isEmpty() && favoritesChecked)
            Toast.makeText(getApplicationContext(),
                    "Houd een woord ingedrukt om het toe te voegen aan je favorieten.",
                    Toast.LENGTH_LONG).show();

        Word[] filtered = new Word[filter.size()];

        Word[] wordsArr = new Word[words.size()];
        wordsArr = words.toArray(wordsArr);

        if (favoritesChecked) adapter.setFilter(filter.toArray(filtered));
        else adapter.setFilter(wordsArr);
    }


    public void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse("mailto:faka.suggestie@outlook.com"));

        emailIntent.putExtra("subject", "Woord");
        emailIntent.putExtra("body", "Woord:\n\n\nBetekenis:\n\n\nVoorbeeldzin:\n");

        try {
            startActivity(emailIntent);
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "Geen e-mail applicatie gevonden.", Toast.LENGTH_SHORT).show();
        }
    }
}
