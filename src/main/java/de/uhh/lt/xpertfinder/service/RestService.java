package de.uhh.lt.xpertfinder.service;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class RestService {

    public String sendPostRequest(String url, String json) {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost get = new HttpPost(url);

        try {
            // create post request
            StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            get.setEntity(entity);
            get.setHeader("Accept", "application/json");
            get.setHeader("Content-type", "application/json");

            // execute post request
            CloseableHttpResponse response = client.execute(get);

            // extract results from response
            if (response.getStatusLine().getStatusCode() == 200) {
                return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String sendGetRequest(String url) {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);

        try {
            // execute get request
            CloseableHttpResponse response = client.execute(get);

            // extract results from response
            if (response.getStatusLine().getStatusCode() == 200) {
                return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
