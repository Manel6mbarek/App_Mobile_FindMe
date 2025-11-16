package ManelMbarekG1.findmyfriends;

public class Position {
    private int idposition;
    private String pseudo;
    private  int numero;
    private String longitude;
    private String latitude;

    public Position(){

    }
    public Position(int idposition, String pseudo, int numero, String longitude, String latitude) {
        this.idposition = idposition;
        this.pseudo = pseudo;
        this.numero = numero;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Position(String pseudo, int numero, String longitude, String latitude) {
        this.pseudo = pseudo;
        this.numero = numero;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getIdposition() {
        return idposition;
    }

    public void setIdposition(int idposition) {
        this.idposition = idposition;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "Position{" +
                "idposition=" + idposition +
                ", pseudo='" + pseudo + '\'' +
                ", numero='" + numero + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                '}';
    }
}
