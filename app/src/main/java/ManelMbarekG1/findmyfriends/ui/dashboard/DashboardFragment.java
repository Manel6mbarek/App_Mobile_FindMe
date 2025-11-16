package ManelMbarekG1.findmyfriends.ui.dashboard;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import ManelMbarekG1.findmyfriends.LocationHelper;
import ManelMbarekG1.findmyfriends.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private EditText editTextPhone;
    private Button buttonSend;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editTextPhone = binding.editTextPhone;
        buttonSend = binding.buttonSendSms;

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLocationRequest();
            }
        });



        return root;
    }

    private void sendLocationRequest() {
        String phoneNumber = editTextPhone.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer un numéro de téléphone", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "Envoyez-moi votre position";
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            Toast.makeText(getContext(), "SMS envoyé avec succès à " + phoneNumber, Toast.LENGTH_SHORT).show();
            editTextPhone.setText("");
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors de l'envoi du SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void sendMyLocation() {
        String phoneNumber = editTextPhone.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer un numéro de téléphone", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Obtention de votre position...", Toast.LENGTH_SHORT).show();

        LocationHelper locationHelper = new LocationHelper(getContext());
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                String message = "Ma position est latitude: " + latitude + " longitude: " + longitude;

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);

                    Toast.makeText(getContext(), "Position envoyée avec succès!", Toast.LENGTH_SHORT).show();
                    editTextPhone.setText("");
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Erreur lors de l'envoi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onLocationError(String error) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}