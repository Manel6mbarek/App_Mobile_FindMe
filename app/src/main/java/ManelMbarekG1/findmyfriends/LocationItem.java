package ManelMbarekG1.findmyfriends;

public class LocationItem {
    private String idposition;  // Nouveau champ
    private String numero;
    private String latitude;
    private String longitude;
    private String timestamp;
    private String pseudo;  // Nouveau champ

    // Constructeur avec ID et pseudo
    public LocationItem(String idposition, String pseudo, String numero, String latitude, String longitude, String timestamp) {
        this.idposition = idposition;
        this.pseudo = pseudo;
        this.numero = numero;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    // Constructeur sans ID (pour compatibilité)
    public LocationItem(String numero, String latitude, String longitude, String timestamp) {
        this.idposition = null;
        this.pseudo = "";
        this.numero = numero;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    // Getters et Setters
    public String getIdposition() {
        return idposition;
    }

    public void setIdposition(String idposition) {
        this.idposition = idposition;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}