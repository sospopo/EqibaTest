import Entity.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by chunmiao on 17-4-9.
 */
public class testSession {
    private String session = null;
    public static void main(String[] args) {
        testSession testSession = new testSession();
        testSession.getRes();

    }

    private void getRes(){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).build();

        HttpPost httpPost = new HttpPost("http://localhost:8080/user/loginIn");
        httpPost.setConfig(config);
        User user = new User();
        user.setUserName("miao");
        user.setPassword("844934");
        Gson gson = new Gson();
        String s = gson.toJson(user);

        StringEntity entity = new StringEntity(s,"utf-8");
        entity.setContentEncoding("utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        HttpResponse response;
        CookieStore cookieStore;

        try {
            response = httpClient.execute(httpPost);
            Header[] headers = response.getAllHeaders();
            printInfo(headers);
            String data = EntityUtils.toString(response.getEntity());
            System.out.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse result  = httpClient.execute(httpPost);
            Header[] headers = result.getAllHeaders();
            printInfo(headers);
            String data = EntityUtils.toString(result.getEntity());
            System.out.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printInfo(Header[] headers){
        int i = 0;
        while (i < headers.length){
            System.out.println("name : " + headers[i].getName() +
                    "   value: " + headers[i].getValue());
            i ++;
        }

    }

}


