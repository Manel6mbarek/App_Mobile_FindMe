package ManelMbarekG1.findmyfriends;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

public class LocationHelper {

    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String error);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void getCurrentLocation(LocationCallback callback) {
        // Vérifier les permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Permission de localisation non accordée");
            return;
        }

        // Essayer d'obtenir la dernière position connue
        Location lastKnownLocation = getLastKnownLocation();

        if (lastKnownLocation != null) {
            callback.onLocationReceived(
                    lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude()
            );
        } else {
            // Demander une nouvelle localisation
            requestNewLocation(callback);
        }
    }

    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gpsLocation != null && networkLocation != null) {
            // Retourner la position la plus récente
            if (gpsLocation.getTime() > networkLocation.getTime()) {
                return gpsLocation;
            } else {
                return networkLocation;
            }
        }

        if (gpsLocation != null) {
            return gpsLocation;
        }

        return networkLocation;
    }

    private void requestNewLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Permission de localisation non accordée");
            return;
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    stopLocationUpdates();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
                callback.onLocationError("Service de localisation désactivé");
            }
        };

        // Essayer GPS d'abord
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        }
        // Sinon utiliser le réseau
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        } else {
            callback.onLocationError("Aucun service de localisation disponible");
        }

        // Timeout après 10 secondes
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopLocationUpdates();
                callback.onLocationError("Timeout - impossible d'obtenir la position");
            }
        }, 10000);
    }

    public void stopLocationUpdates() {
        if (locationListener != null && locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(locationListener);
            }
            locationListener = null;
        }
    }
}