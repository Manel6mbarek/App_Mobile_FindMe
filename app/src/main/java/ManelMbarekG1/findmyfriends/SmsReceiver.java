package ManelMbarekG1.findmyfriends;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "location_channel";
    private static final String PREFS_NAME = "LocationHistory";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody();

                    // Cas 1: Vérifier si quelqu'un demande notre position
                    if (messageBody.toLowerCase().contains("envoyez-moi votre position") ||
                            messageBody.toLowerCase().contains("envoyer moi votre position") ||
                            messageBody.toLowerCase().contains("envoi moi ta position") ||
                            messageBody.toLowerCase().contains("ta position")) {

                        sendMyLocation(context, sender);
                    }

                    // Cas 2: Vérifier si le message contient une position
                    if (messageBody.toLowerCase().contains("latitude") &&
                            messageBody.toLowerCase().contains("longitude")) {

                        String latitude = extractCoordinate(messageBody, "latitude");
                        String longitude = extractCoordinate(messageBody, "longitude");

                        if (latitude != null && longitude != null) {
                            // Sauvegarder dans l'historique local
                            saveToHistory(context, sender, latitude, longitude);

                            // Sauvegarder dans la base de données
                            saveToDatabase(context, sender, latitude, longitude);

                            // Créer une notification
                            createNotification(context, sender, latitude, longitude);
                        }
                    }
                }
            }
        }
    }

    private void sendMyLocation(Context context, String recipient) {
        LocationHelper locationHelper = new LocationHelper(context);

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                String message = "Ma position est latitude: " + latitude + " longitude: " + longitude;
                sendSms(recipient, message);
            }

            @Override
            public void onLocationError(String error) {
                sendSms(recipient, "Impossible d'obtenir ma position: " + error);
            }
        });
    }

    private void sendSms(String numeroNumber, String message) {
        try {
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
            smsManager.sendTextMessage(numeroNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractCoordinate(String message, String type) {
        try {
            Pattern pattern = Pattern.compile(type + "\\s*:?\\s*(-?\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveToHistory(Context context, String numero, String latitude, String longitude) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        int count = prefs.getInt("count", 0);

        editor.putString("numero_" + count, numero);
        editor.putString("latitude_" + count, latitude);
        editor.putString("longitude_" + count, longitude);
        editor.putString("timestamp_" + count, timestamp);
        editor.putInt("count", count + 1);
        editor.apply();
    }

    // Nouvelle méthode pour sauvegarder dans la base de données
    private void saveToDatabase(Context context, String numero, String latitude, String longitude) {
        new SavePositionTask(context, numero, latitude, longitude).execute();
    }

    // AsyncTask pour sauvegarder la position dans la base de données
    private static class SavePositionTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private String numero;
        private String latitude;
        private String longitude;

        public SavePositionTask(Context context, String numero, String latitude, String longitude) {
            this.context = context;
            this.numero = numero;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("pseudo", "SMS_" + numero);
                params.put("numero", numero.replaceAll("[^0-9]", "")); // Enlever les caractères non numériques
                params.put("longitude", longitude);
                params.put("latitude", latitude);

                JSONParser parser = new JSONParser();
                JSONObject response = parser.makeHttpRequest(config.URL_ADD_POSITION, "POST", params);

                if (response != null) {
                    Log.d("SmsReceiver", "Response: " + response.toString());
                    int success = response.getInt("success");
                    return success == 1;
                }
            } catch (Exception e) {
                Log.e("SmsReceiver", "Error saving to database: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d("SmsReceiver", "Position saved to database successfully");
            } else {
                Log.e("SmsReceiver", "Failed to save position to database");
            }
        }
    }

    private void createNotification(Context context, String numero, String latitude, String longitude) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("numero", numero);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Nouvelle position reçue")
                .setContentText(numero + " a partagé sa position")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}