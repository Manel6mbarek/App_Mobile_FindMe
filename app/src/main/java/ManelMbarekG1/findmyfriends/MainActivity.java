package ManelMbarekG1.findmyfriends;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ManelMbarekG1.findmyfriends.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        //  Supprimé : pas d'ActionBar car tu utilises un design personnalisé
        // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        //  On garde uniquement la liaison avec la BottomNavigationView
        NavigationUI.setupWithNavController(binding.navView, navController);

        //  Évite la recréation inutile des fragments lors du changement d’onglet
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                if (navController.getCurrentDestination().getId() != R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home);
                }
                return true;
            } else if (itemId == R.id.navigation_dashboard) {
                if (navController.getCurrentDestination().getId() != R.id.navigation_dashboard) {
                    navController.navigate(R.id.navigation_dashboard);
                }
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                if (navController.getCurrentDestination().getId() != R.id.navigation_notifications) {
                    navController.navigate(R.id.navigation_notifications);
                }
                return true;
            }
            return false;
        });

        //  Gestion des permissions
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
        }, 1);

        // Si l’appli a été ouverte depuis une notification
        if (getIntent().getBooleanExtra("navigate_to_notifications", false)) {
            Bundle bundle = new Bundle();
            bundle.putString("numero", getIntent().getStringExtra("numero"));
            bundle.putString("latitude", getIntent().getStringExtra("latitude"));
            bundle.putString("longitude", getIntent().getStringExtra("longitude"));
            navController.navigate(R.id.navigation_notifications, bundle);
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getBooleanExtra("navigate_to_notifications", false)) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putString("numero", intent.getStringExtra("numero"));
            bundle.putString("latitude", intent.getStringExtra("latitude"));
            bundle.putString("longitude", intent.getStringExtra("longitude"));

            navController.popBackStack(R.id.navigation_home, false);
            navController.navigate(R.id.navigation_notifications, bundle);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
