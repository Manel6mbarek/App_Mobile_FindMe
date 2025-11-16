package ManelMbarekG1.findmyfriends.ui.home;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ManelMbarekG1.findmyfriends.JSONParser;
import ManelMbarekG1.findmyfriends.LocationAdapter;
import ManelMbarekG1.findmyfriends.LocationItem;
import ManelMbarekG1.findmyfriends.Position;
import ManelMbarekG1.findmyfriends.R;
import ManelMbarekG1.findmyfriends.config;
import ManelMbarekG1.findmyfriends.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private LocationAdapter adapter;
    private List<LocationItem> locationList;
    private ArrayList<Position> data = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        locationList = new ArrayList<>();

        adapter = new LocationAdapter(locationList, new LocationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LocationItem item) {
                Bundle bundle = new Bundle();
                bundle.putString("numero", item.getNumero());
                bundle.putString("latitude", item.getLatitude());
                bundle.putString("longitude", item.getLongitude());

                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                navController.navigate(R.id.navigation_notifications, bundle,
                        new androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.navigation_home, false)
                                .build()
                );
            }

            @Override
            public void onDeleteClick(LocationItem item, int position) {
                new DeleteTask(item, position).execute();
            }

            @Override
            public void onEditClick(LocationItem item, int position, String newPseudo, String newNumero, String newLatitude, String newLongitude) {
                new EditTask(item, position, newPseudo, newNumero, newLatitude, newLongitude).execute();
            }
        });

        recyclerView.setAdapter(adapter);

        loadHistory();
        new Download().execute();

        return root;
    }

    private void loadHistory() {
        SharedPreferences prefs = getContext().getSharedPreferences("LocationHistory", getContext().MODE_PRIVATE);
        int count = prefs.getInt("count", 0);

        locationList.clear();

        for (int i = count - 1; i >= 0; i--) {
            String numero = prefs.getString("numero_" + i, "");
            String latitude = prefs.getString("latitude_" + i, "");
            String longitude = prefs.getString("longitude_" + i, "");
            String timestamp = prefs.getString("timestamp_" + i, "");

            if (!numero.isEmpty() && !latitude.isEmpty() && !longitude.isEmpty()) {
                locationList.add(new LocationItem(null, "", numero, latitude, longitude, timestamp));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
        new Download().execute();
    }

    class DeleteTask extends AsyncTask<Void, Void, Boolean> {
        LocationItem item;
        int position;

        DeleteTask(LocationItem item, int position) {
            this.item = item;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONParser parser = new JSONParser();

                java.util.HashMap<String, String> params = new java.util.HashMap<>();

                if (item.getIdposition() != null && !item.getIdposition().isEmpty()) {
                    params.put("idposition", item.getIdposition());
                } else {
                    params.put("numero", item.getNumero());
                }

                JSONObject response = parser.makeHttpRequest(config.URL_DELETE, "POST", params);

                if (response != null) {
                    int success = response.getInt("success");
                    return success == 1;
                }
            } catch (Exception e) {
                Log.e("DeleteTask", "Error: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                adapter.removeItem(position);
                Toast.makeText(getContext(), "Position supprimée avec succès", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class EditTask extends AsyncTask<Void, Void, Boolean> {
        LocationItem oldItem;
        int position;
        String newPseudo, newNumero, newLatitude, newLongitude;

        EditTask(LocationItem oldItem, int position, String newPseudo, String newNumero, String newLatitude, String newLongitude) {
            this.oldItem = oldItem;
            this.position = position;
            this.newPseudo = newPseudo;
            this.newNumero = newNumero;
            this.newLatitude = newLatitude;
            this.newLongitude = newLongitude;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONParser parser = new JSONParser();

                java.util.HashMap<String, String> params = new java.util.HashMap<>();

                if (oldItem.getIdposition() != null && !oldItem.getIdposition().isEmpty()) {
                    params.put("idposition", oldItem.getIdposition());
                } else {
                    params.put("old_numero", oldItem.getNumero());
                }

                params.put("pseudo", newPseudo);
                params.put("numero", newNumero);
                params.put("latitude", newLatitude);
                params.put("longitude", newLongitude);

                JSONObject response = parser.makeHttpRequest(config.URL_EDIT, "POST", params);

                if (response != null) {
                    int success = response.getInt("success");
                    return success == 1;
                }
            } catch (Exception e) {
                Log.e("EditTask", "Error: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                LocationItem newItem = new LocationItem(oldItem.getIdposition(), newPseudo, newNumero, newLatitude, newLongitude, oldItem.getTimestamp());
                adapter.updateItem(position, newItem);
                Toast.makeText(getContext(), "Position modifiée avec succès", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class Download extends AsyncTask<Void, Void, Void> {
        AlertDialog alert;

        @Override
        protected void onPreExecute() {
            // Vous pouvez ajouter un ProgressDialog ici si nécessaire
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("HomeFragment", "Sleep interrupted: " + e.getMessage());
                return null;
            }

            try {
                JSONParser parser = new JSONParser();
                JSONObject response = parser.makeRequest(config.URL_GETALL);

                if (response == null) {
                    Log.e("HomeFragment", "Response is null - Serveur inaccessible");
                    return null;
                }

                Log.d("HomeFragment", "Response: " + response.toString());

                int success = response.getInt("success");
                if (success == 1) {
                    JSONArray tableau = response.getJSONArray("positions");
                    data.clear();

                    for (int i = 0; i < tableau.length(); i++) {
                        JSONObject ligne = tableau.getJSONObject(i);

                        int idposition = ligne.getInt("idposition");
                        String pseudo = ligne.getString("pseudo");

                        String numero;
                        try {
                            numero = String.valueOf(ligne.getInt("numero"));
                        } catch (JSONException e) {
                            numero = ligne.getString("numero");
                        }

                        String longitude = ligne.getString("longitude");
                        String latitude = ligne.getString("latitude");

                        try {
                            data.add(new Position(idposition, pseudo, Integer.parseInt(numero), longitude, latitude));
                        } catch (NumberFormatException e) {
                            Log.e("HomeFragment", "Invalid numero format: " + numero);
                            continue;
                        }

                        boolean exists = false;
                        for (LocationItem item : locationList) {
                            if (item.getNumero().equals(numero) &&
                                    item.getLatitude().equals(latitude) &&
                                    item.getLongitude().equals(longitude)) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            locationList.add(0, new LocationItem(
                                    String.valueOf(idposition),
                                    pseudo,
                                    numero,
                                    latitude,
                                    longitude,
                                    pseudo
                            ));
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("HomeFragment", "Error parsing JSON: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("HomeFragment", "Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            if (binding != null) {
                if (locationList.isEmpty()) {
                    Toast.makeText(getContext(), "Aucune position disponible", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), locationList.size() + " position(s) chargée(s)", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}