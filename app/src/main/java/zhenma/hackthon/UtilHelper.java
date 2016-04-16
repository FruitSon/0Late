package zhenma.hackthon;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by dnalwqer on 4/15/16.
 */
public class UtilHelper {
    public HttpClient httpclient;
    public UtilHelper(){
        httpclient=new DefaultHttpClient();
    }

    public String getXML(String url) throws ClientProtocolException, IOException {
        String xml = null;
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                xml = EntityUtils.toString(entity);
            }
        }
        return xml;
    }
}
