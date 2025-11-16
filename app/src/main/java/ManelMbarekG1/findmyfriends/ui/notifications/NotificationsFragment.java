package ManelMbarekG1.findmyfriends.ui.notifications;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.HashMap;

import ManelMbarekG1.findmyfriends.JSONParser;
import ManelMbarekG1.findmyfriends.R;
import ManelMbarekG1.findmyfriends.config;
import ManelMbarekG1.findmyfriends.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment implements OnMapReadyCallback {

    private FragmentNotificationsBinding binding;
    private GoogleMap mMap;
    private String numero;
    private String latitude;
    private String longitude;
    private TextView textInfo;
    private FloatingActionButton btnAddPosition; // ✅ correction ici
    private Marker currentMarker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textInfo = binding.textInfo;
        btnAddPosition = binding.btnAddPosition; // ✅ plus d’erreur ici

        // Récupérer les arguments
        if (getArguments() != null) {
            numero = getArguments().getString("numero");
            latitude = getArguments().getString("latitude");
            longitude = getArguments().getString("longitude");

            if (numero != null && latitude != null && longitude != null) {
                textInfo.setText("Position de " + numero);
            }
        }

        // Bouton pour ajouter une position manuellement
        btnAddPosition.setOnClickListener(v -> showAddPositionDialog());

        // Initialiser la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMapLongClickListener(this::showAddPositionAtLocationDialog);

        if (latitude != null && longitude != null) {
            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);
                LatLng location = new LatLng(lat, lon);

                currentMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(numero != null ? numero : "Position")
                        .snippet("Lat: " + latitude + ", Lon: " + longitude));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

            } catch (NumberFormatException e) {
                textInfo.setText("Erreur: Coordonnées invalides");
            }
        } else {
            LatLng defaultLocation = new LatLng(35.8256, 10.6369);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
            textInfo.setText("Appuyez longuement sur la carte pour ajouter une position");
        }
    }

    private void showAddPositionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView title = new TextView(getContext());
        title.setText("Entrez les informations");
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        final EditText editPseudo = new EditText(getContext());
        editPseudo.setHint("Pseudo");
        layout.addView(editPseudo);

        final EditText editNumero = new EditText(getContext());
        editNumero.setHint("Numéro de téléphone");
        editNumero.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(editNumero);

        final EditText editLatitude = new EditText(getContext());
        editLatitude.setHint("Latitude (ex: 35.8256)");
        layout.addView(editLatitude);

        final EditText editLongitude = new EditText(getContext());
        editLongitude.setHint("Longitude (ex: 10.6369)");
        layout.addView(editLongitude);

        builder.setView(layout)
                .setTitle("Ajouter une position")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String pseudo = editPseudo.getText().toString().trim();
                    String num = editNumero.getText().toString().trim();
                    String lat = editLatitude.getText().toString().trim();
                    String lon = editLongitude.getText().toString().trim();

                    if (pseudo.isEmpty() || num.isEmpty() || lat.isEmpty() || lon.isEmpty()) {
                        Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double latValue = Double.parseDouble(lat);
                        double lonValue = Double.parseDouble(lon);

                        new AddPositionTask(pseudo, num, lat, lon).execute();

                        if (mMap != null) {
                            LatLng location = new LatLng(latValue, lonValue);
                            mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(pseudo)
                                    .snippet("Tel: " + num));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Coordonnées invalides", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAddPositionAtLocationDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView textCoordinates = new TextView(getContext());
        textCoordinates.setText(String.format("Lat: %.6f, Lon: %.6f", latLng.latitude, latLng.longitude));
        textCoordinates.setPadding(0, 0, 0, 20);
        layout.addView(textCoordinates);

        final EditText editPseudo = new EditText(getContext());
        editPseudo.setHint("Pseudo");
        layout.addView(editPseudo);

        final EditText editNumero = new EditText(getContext());
        editNumero.setHint("Numéro de téléphone");
        layout.addView(editNumero);

        builder.setView(layout)
                .setTitle("Ajouter une position")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String pseudo = editPseudo.getText().toString().trim();
                    String num = editNumero.getText().toString().trim();

                    if (pseudo.isEmpty() || num.isEmpty()) {
                        Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String lat = String.valueOf(latLng.latitude);
                    String lon = String.valueOf(latLng.longitude);

                    new AddPositionTask(pseudo, num, lat, lon).execute();

                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(pseudo)
                                .snippet("Tel: " + num));
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    class AddPositionTask extends AsyncTask<Void, Void, Boolean> {
        private final String pseudo;
        private final String numero;
        private final String latitude;
        private final String longitude;

        public AddPositionTask(String pseudo, String numero, String latitude, String longitude) {
            this.pseudo = pseudo;
            this.numero = numero;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("pseudo", pseudo);
                params.put("numero", numero);
                params.put("latitude", latitude);
                params.put("longitude", longitude);

                JSONParser parser = new JSONParser();
                JSONObject response = parser.makeHttpRequest(config.URL_ADD_POSITION, "POST", params);

                if (response != null) {
                    int success = response.getInt("success");
                    return success == 1;
                }
            } catch (Exception e) {
                Log.e("NotificationsFragment", "Erreur: " + e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Toast.makeText(getContext(),
                    success ? "Position ajoutée avec succès!" : "Erreur lors de l'ajout de la position",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
