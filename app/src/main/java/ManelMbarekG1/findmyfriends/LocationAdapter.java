package ManelMbarekG1.findmyfriends;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<LocationItem> items;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(LocationItem item);
        void onDeleteClick(LocationItem item, int position);
        void onEditClick(LocationItem item, int position, String newPseudo, String newNumero, String newLatitude, String newLongitude);
    }

    public LocationAdapter(List<LocationItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationItem item = items.get(position);

        // Afficher le pseudo et le numéro
        String displayText = item.getPseudo().isEmpty() ? item.getNumero() : item.getPseudo() + " (" + item.getNumero() + ")";
        holder.textNumero.setText(displayText);

        holder.textCoordinates.setText("Lat: " + item.getLatitude() + "\nLon: " + item.getLongitude());
        holder.textTimestamp.setText(item.getTimestamp());

        // Click sur le card pour voir la carte
        holder.cardView.setOnClickListener(v -> listener.onItemClick(item));

        // Bouton Call
        holder.btnCall.setOnClickListener(v -> {
            checkAndRequestCallPermission(item.getNumero());
        });

        // Bouton Edit
        holder.btnEdit.setOnClickListener(v -> {
            showEditDialog(item, position);
        });

        // Bouton Delete
        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmation(item, position);
        });
    }

    // Fonction pour appeler
    private void checkAndRequestCallPermission(String numero) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            // Demander la permission
            Toast.makeText(context, "Veuillez autoriser l'appel téléphonique dans les paramètres", Toast.LENGTH_LONG).show();

            // Si c'est une Activity, demander la permission
            if (context instanceof androidx.appcompat.app.AppCompatActivity) {
                ActivityCompat.requestPermissions(
                        (androidx.appcompat.app.AppCompatActivity) context,
                        new String[]{Manifest.permission.CALL_PHONE},
                        100
                );
            }
        } else {
            // Permission accordée, appeler
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + numero));
            context.startActivity(callIntent);
        }
    }

    // Dialog de confirmation de suppression
    private void showDeleteConfirmation(LocationItem item, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous vraiment supprimer cette position ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    listener.onDeleteClick(item, position);
                })
                .setNegativeButton("Non", null)
                .show();
    }

    // Dialog pour éditer
    private void showEditDialog(LocationItem item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_location, null);

        EditText editPseudo = dialogView.findViewById(R.id.editPseudo);
        EditText editNumero = dialogView.findViewById(R.id.editNumero);
        EditText editLatitude = dialogView.findViewById(R.id.editLatitude);
        EditText editLongitude = dialogView.findViewById(R.id.editLongitude);

        // Remplir avec les valeurs actuelles
        editPseudo.setText(item.getPseudo());
        editNumero.setText(item.getNumero());
        editLatitude.setText(item.getLatitude());
        editLongitude.setText(item.getLongitude());

        builder.setView(dialogView)
                .setTitle("Modifier la position")
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String newPseudo = editPseudo.getText().toString().trim();
                    String newNumero = editNumero.getText().toString().trim();
                    String newLatitude = editLatitude.getText().toString().trim();
                    String newLongitude = editLongitude.getText().toString().trim();

                    if (newNumero.isEmpty() || newLatitude.isEmpty() || newLongitude.isEmpty()) {
                        Toast.makeText(context, "Numéro, latitude et longitude sont requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // CORRECTION: Ajouter newPseudo comme premier paramètre String
                    listener.onEditClick(item, position, newPseudo, newNumero, newLatitude, newLongitude);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Fonction pour supprimer un item de la liste
    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    // Fonction pour mettre à jour un item
    public void updateItem(int position, LocationItem newItem) {
        items.set(position, newItem);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textNumero;
        TextView textCoordinates;
        TextView textTimestamp;
        ImageView btnCall;
        ImageView btnEdit;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textNumero = itemView.findViewById(R.id.textNumero);
            textCoordinates = itemView.findViewById(R.id.textCoordinates);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            btnCall = itemView.findViewById(R.id.id_phone);
            btnEdit = itemView.findViewById(R.id.id_edit);
            btnDelete = itemView.findViewById(R.id.id_delete);
        }
    }
}