package multico.in.btctrade;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Smmarat on 16.01.17.
 */

public class App extends Application {

    private static App instance;
    private SharedPreferences pref;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate();
    }

}
