package alexlushiku.faka;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class UpdateWordsService extends Service {

    private URL url;
    private HttpURLConnection connection;
    private InputStream is;
    private Scanner scanner;
    private StringBuilder JSON;

    public UpdateWordsService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    url = new URL("https://www.jasonbase.com/things/mKky");
                    connection = (HttpURLConnection) url.openConnection();

                    is = connection.getInputStream();
                    scanner = new Scanner(is);

                    JSON = new StringBuilder();

                    while (scanner.hasNextLine())
                        JSON.append(scanner.nextLine());

                    // Save JSON to file
                    SharedPreferences prefs = getApplicationContext()
                            .getSharedPreferences("words", 0);
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.clear();
                    editor.putString("words", JSON.toString());
                    editor.apply();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
