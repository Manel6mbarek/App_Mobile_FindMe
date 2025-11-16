package ManelMbarekG1.findmyfriends;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class JSONParser {

    String charset = "UTF-8";
    HttpURLConnection conn;
    DataOutputStream wr;
    StringBuilder result;
    URL urlObj;
    JSONObject jObj = null;
    StringBuilder sbParams;
    String paramsString;

    public JSONObject makeRequest(String url) {
        result = new StringBuilder(); // Initialiser result

        try {
            urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setConnectTimeout(10000); // 10 secondes timeout
            conn.setReadTimeout(10000);

            Log.d("JSON Parser", "Connecting to: " + url);

        } catch (MalformedURLException e) {
            Log.e("JSON Parser", "URL mal formée: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e("JSON Parser", "Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        try {
            // Recevoir la réponse du serveur
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("JSON Parser", "result: " + result.toString());

        } catch (IOException e) {
            Log.e("JSON Parser", "Erreur lecture réponse: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        // Parser le JSON
        try {
            if (result.length() > 0) {
                jObj = new JSONObject(result.toString());
            } else {
                Log.e("JSON Parser", "Réponse vide du serveur");
                return null;
            }
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            Log.e("JSON Parser", "Raw response: " + result.toString());
            return null;
        }

        return jObj;
    }

    public JSONObject makeHttpRequest(String url, String method, HashMap<String, String> params) {
        result = new StringBuilder(); // Initialiser result
        sbParams = new StringBuilder();

        if (params != null) {
            int i = 0;
            for (String key : params.keySet()) {
                try {
                    if (i != 0) {
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=")
                            .append(URLEncoder.encode(params.get(key), charset));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }

        if (method.equals("POST")) {
            try {
                urlObj = new URL(url);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Charset", charset);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.connect();

                paramsString = sbParams.toString();
                wr = new DataOutputStream(conn.getOutputStream());
                if (params != null) {
                    wr.writeBytes(paramsString);
                    wr.flush();
                    wr.close();
                }

            } catch (IOException e) {
                Log.e("JSON Parser", "POST request error: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else if (method.equals("GET")) {
            if (sbParams.length() != 0) {
                url += "?" + sbParams.toString();
            }

            try {
                urlObj = new URL(url);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Charset", charset);
                conn.setConnectTimeout(15000);
                conn.connect();

            } catch (IOException e) {
                Log.e("JSON Parser", "GET request error: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("JSON Parser", "result: " + result.toString());

        } catch (IOException e) {
            Log.e("JSON Parser", "Error reading response: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            if (result.length() > 0) {
                jObj = new JSONObject(result.toString());
            } else {
                Log.e("JSON Parser", "Empty response");
                return null;
            }
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            return null;
        }

        return jObj;
    }

    public JSONObject makeHttpDeleteRequest(String url, JSONObject params) {
        result = new StringBuilder(); // Initialiser result

        try {
            urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept-Charset", charset);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = params.toString().getBytes(charset);
                os.write(input, 0, input.length);
            }

            conn.connect();
        } catch (IOException e) {
            Log.e("JSON Parser", "DELETE request error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("JSON Parser", "result: " + result.toString());
        } catch (IOException e) {
            Log.e("JSON Parser", "Error reading DELETE response: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            if (result.length() > 0) {
                jObj = new JSONObject(result.toString());
            } else {
                Log.e("JSON Parser", "Empty DELETE response");
                return null;
            }
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing DELETE data " + e.toString());
            return null;
        }

        return jObj;
    }
}