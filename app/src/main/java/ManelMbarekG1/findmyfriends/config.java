package ManelMbarekG1.findmyfriends;


public class config {
    // L'adresse IP de votre serveur
    public static String Ip_Server="10.111.47.215";
    /**
     * Ipv4
     */
    // L'URL pour récupérer toutes les positions
    public static final String URL_GETALL = "http://" + Ip_Server + "/servicephp/get_all.php";

    // L'URL pour ajouter une position
    public static final String URL_ADD_POSITION = "http://" + Ip_Server + "/servicephp/add_position.php";
    // L'URL pour delete une position
    public static final String URL_DELETE = "http://" + Ip_Server + "/servicephp/delete_position.php";

    public static final String URL_EDIT = "http://" + Ip_Server + "/servicephp/edit_position.php";
}

