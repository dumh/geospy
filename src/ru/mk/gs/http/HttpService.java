package ru.mk.gs.http;

import android.location.Location;
import android.util.Base64;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import ru.mk.gs.config.ConfigManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mkasumov
 */
public class HttpService {

    private final HttpClient httpClient;
    private final ConfigManager cfg;

    public HttpService(ConfigManager configManager) {
        cfg = configManager;
        httpClient = createClient();
    }

    private HttpClient createClient() {
        try {
            final URL url = cfg.getConfig().getUrl();
            if (!url.getProtocol().equalsIgnoreCase("https")) {
                return new DefaultHttpClient();
            }

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { new CustomX509TrustManager() },
                    new SecureRandom());

            HttpClient client = new DefaultHttpClient();
            SSLSocketFactory ssf = new CustomSSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            final int port = url.getPort();
            sr.register(new Scheme("https", ssf, (port > 0 ? port : 443)));
            return new DefaultHttpClient(ccm, client.getParams());
        } catch (Exception e) {
            Log.d("GS", "HTTP error", e);
            throw new RuntimeException("Could not create http client", e);
        }
    }

    private void setAuthorization(AbstractHttpMessage httpMessage) {
        httpMessage.setHeader("Authorization",
                "Basic " + Base64.encodeToString(cfg.getConfig().getCredentials().getBytes(), Base64.NO_WRAP));
    }

    public boolean sendLocation(String subject, Location location) {
        try {
            HttpPost httpPost = new HttpPost(cfg.getConfig().getUrl() +"/loc.php");
            setAuthorization(httpPost);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("subj", subject));
            nameValuePairs.add(new BasicNameValuePair("prv", location.getProvider()));
            nameValuePairs.add(new BasicNameValuePair("lon", String.valueOf(location.getLongitude())));
            nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(location.getLatitude())));
            nameValuePairs.add(new BasicNameValuePair("acc", String.valueOf(location.getAccuracy())));
            nameValuePairs.add(new BasicNameValuePair("spd", String.valueOf(location.getSpeed())));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            final HttpResponse response = httpClient.execute(httpPost);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return true;
            } else {
                Log.d("GS", "HTTP Response: " + statusCode);
            }

        } catch (Exception e) {
            Log.d("GS", "HTTP error", e);
        }
        return false;
    }

    public Location receiveLocation(String subject) {
        try {
            final HttpGet httpGet = new HttpGet(cfg.getConfig().getUrl() + "/track.php?subj="+subject);
            setAuthorization(httpGet);
            final HttpResponse response = httpClient.execute(httpGet);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
            response.getEntity().writeTo(baos);
            baos.flush();

            return parseLocation(baos.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error receiving location", e);
        }
    }

    private Location parseLocation(String content) {
        try {
            final JSONObject json = new JSONObject(content);
            final Location l = new Location(json.getString("provider"));
            l.setTime(json.getInt("time") * 1000L);
            l.setLatitude(json.getDouble("latitude"));
            l.setLongitude(json.getDouble("longitude"));
            l.setAccuracy((float) json.getDouble("accuracy"));
            l.setSpeed((float) json.getDouble("speed"));
            return l;
        } catch (JSONException e) {
            throw new RuntimeException("Error parsing location", e);
        }
    }
}
