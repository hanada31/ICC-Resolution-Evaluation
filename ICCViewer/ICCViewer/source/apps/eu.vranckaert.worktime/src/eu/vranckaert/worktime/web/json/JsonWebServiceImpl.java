/*
 * Copyright 2013 Dirk Vranckaert
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.web.json;

import android.util.Log;
import eu.vranckaert.worktime.guice.Application;
import eu.vranckaert.worktime.web.json.exception.*;
import eu.vranckaert.worktime.web.json.model.AuthorizationHeader;
import eu.vranckaert.worktime.web.json.model.JsonEntity;
import eu.vranckaert.worktime.web.json.model.JsonResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.*;
import java.util.Map;

/**
 * Date: 16/10/12
 * Time: 19:00
 *
 * @author Dirk Vranckaert
 */
public class JsonWebServiceImpl implements JsonWebService {
    private static final String LOG_TAG = JsonWebServiceImpl.class.getSimpleName();

    private HttpPost httpPost = null;
    private Application applicationScope;

    public JsonWebServiceImpl(Application applicationScope) {
        this.applicationScope = applicationScope;
    }

    @Override
    public JsonResult webInvokePost(String baseUrl, String methodName, AuthorizationHeader authorizationHeader, Map<String, String> ampParams, JsonEntity jsonEntity, String... parameters) throws WebException, CommunicationException {
        String endpoint = baseUrl + methodName;

        if (parameters != null && parameters.length > 0) {
            for (Object param : parameters) {
                endpoint += "/" + param;
            }
        }

        endpoint = buildEndpointWithAmpParams(endpoint, ampParams);

        httpPost = new HttpPost(endpoint);
        httpPost.setHeader(new BasicHeader(HTTP.CONTENT_ENCODING, "utf-8"));
        httpPost.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        if (authorizationHeader != null) {
            httpPost.setHeader("Authorization", authorizationHeader.getContent());
        }

        if (jsonEntity != null) {
            String data = jsonEntity.toJSON();

            try {
                StringEntity entity = new StringEntity(data, "utf-8");
                entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(entity);
            } catch (UnsupportedEncodingException e) {}
        }

        HttpClient client = authorizationHeader == null ? getClient() : getNewClient();
        try {
            HttpResponse response = client.execute(httpPost);
            return handleHttpResponse(response);
        } catch (UnknownHostException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public JsonResult webInvokeGet(String baseUrl, String methodName, AuthorizationHeader authorizationHeader, Map<String, String> ampParams, String... parameters) throws WebException, CommunicationException {
        String endpoint = baseUrl + methodName;

        if (parameters != null && parameters.length > 0) {
            for (String param : parameters) {
                try {
                    param = URLEncoder.encode(param, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    Log.d(LOG_TAG, "The encoding is not supported!");
                }
                endpoint += "/" + param;
            }
        }

        endpoint = buildEndpointWithAmpParams(endpoint, ampParams);

        HttpGet httpGet = new HttpGet(endpoint);
        if (authorizationHeader != null) {
            httpGet.setHeader("Authorization", authorizationHeader.getContent());
        }

        HttpClient client = authorizationHeader == null ? getClient() : getNewClient();
        try {
            HttpResponse response = client.execute(httpGet);
            return handleHttpResponse(response);
        } catch (ClientProtocolException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public JsonResult webInvokePut(String baseUrl, String methodName, AuthorizationHeader authorizationHeader, Map<String, String> ampParams, String... parameters) throws WebException, CommunicationException {
        String endpoint = baseUrl + methodName;

        if (parameters != null && parameters.length > 0) {
            for (Object param : parameters) {
                endpoint += "/" + param;
            }
        }

        endpoint = buildEndpointWithAmpParams(endpoint, ampParams);

        HttpPut httpPut = new HttpPut(endpoint);
        if (authorizationHeader != null) {
            httpPut.setHeader("Authorization", authorizationHeader.getContent());
        }

        HttpClient client = authorizationHeader == null ? getClient() : getNewClient();
        try {
            HttpResponse response = client.execute(httpPut);
            return handleHttpResponse(response);
        } catch (ClientProtocolException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public int webInvokeDelete(String baseUrl, String methodName, AuthorizationHeader authorizationHeader, Map<String, String> ampParams, String... parameters) throws CommunicationException {
        String endpoint = baseUrl + methodName;

        if (parameters != null && parameters.length > 0) {
            for (Object param : parameters) {
                endpoint += "/" + param;
            }
        }

        endpoint = buildEndpointWithAmpParams(endpoint, ampParams);

        HttpPut httpPut = new HttpPut(endpoint);
        if (authorizationHeader != null) {
            httpPut.setHeader("Authorization", authorizationHeader.getContent());
        }

        HttpClient client = authorizationHeader == null ? getClient() : getNewClient();
        try {
            HttpResponse response = client.execute(httpPut);
            if (response != null) {
                return response.getStatusLine().getStatusCode();
            } else {
                return HttpStatusCode.UNHANDLED_EXCEPTION;
            }
        } catch (ClientProtocolException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    private String buildEndpointWithAmpParams(String endpoint, Map<String, String> ampParams) {
        if (ampParams == null || ampParams.size() == 0)
            return endpoint;

        endpoint += "?";

        for (Map.Entry<String, String> entry : ampParams.entrySet()) {
            if (!endpoint.endsWith("?")) {
                endpoint += "&";
            }

            String key = "";
            String value = "";
            try {
                key = URLEncoder.encode(entry.getKey(), "utf-8");
                value = URLEncoder.encode(entry.getValue(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                Log.d(LOG_TAG, "The encoding is not supported!");
            }
            String nvp = key + "=" + value;
            endpoint += nvp;
        }

        return endpoint;
    }

    private HttpClient getClient() {
        return applicationScope.getHttpClient();
    }

    private HttpClient getNewClient() {
        return new DefaultHttpClient();
    }

    private JsonResult handleHttpResponse(HttpResponse response) throws IOException, WebException {
        if (response != null) {
            int responseCode = response.getStatusLine().getStatusCode();
            String message = response.getStatusLine().getReasonPhrase();

            if (responseCode == HttpStatusCode.OK) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {

//                    InputStream is = entity.getContent();
//                    String result = convertStreamToString(is);
//
//                    // Closing the input stream will trigger connection release
//                    is.close();

                    String result = EntityUtils.toString(entity, HTTP.UTF_8);

                    return new JsonResult(result);
                }
            } else {
                WebException e;
                switch (responseCode) {
                    case HttpStatusCode.UNAUTHORIZED:
                        e = new AuthorizationRequiredException(responseCode, message);
                        Log.e(LOG_TAG, "Could not start request because authorization is missing or wrong", e);
                        break;
                    case HttpStatusCode.NOT_FOUND:
                        e = new NotFoundException(responseCode, message);
                        Log.e(LOG_TAG, "Could not start request because it's not found", e);
                        break;
                    case HttpStatusCode.METHOD_NOT_ALLOWED:
                        e = new MethodNotAllowedException(responseCode, message);
                        Log.e(LOG_TAG, "The method (POST or GET) is not allowed", e);
                        break;
                    case HttpStatusCode.BAD_REQUEST:
                        e = new WebClientException(responseCode, message);
                        Log.e(LOG_TAG, "Could not process the request, it might be in a bad format", e);
                        break;
                    case HttpStatusCode.FORBIDDEN:
                        e = new WebClientException(responseCode, message);
                        Log.e(LOG_TAG, "Access is forbidden", e);
                        break;
                    case HttpStatusCode.PROXY_AUTHENTICATION_REQUIRED:
                        e = new WebClientException(responseCode, message);
                        Log.e(LOG_TAG, "Request cannot be processed because proxy authentication is required", e);
                        break;
                    case HttpStatusCode.REQUEST_TIMEOUT:
                        e = new WebClientException(responseCode, message);
                        Log.e(LOG_TAG, "Request cannot be processed because it timed out", e);
                        break;
                    case HttpStatusCode.SERVER_ERROR:
                    case HttpStatusCode.NOT_IMPLEMENTED:
                    case HttpStatusCode.BAD_GATEWAY:
                    case HttpStatusCode.SERVICE_UNAVAILABLE:
                    case HttpStatusCode.GATEWAY_TIMEOUT:
                        e = new WebServerException(responseCode, message);
                        Log.e(LOG_TAG, "A server error with code " + responseCode + " occurred (" + message + ")", e);
                        break;
                    default:
                        e = new WebException(HttpStatusCode.UNHANDLED_EXCEPTION, message);
                }
                throw e;
            }
        }

        return null;
    }

    @Override
    public void clearCookies() {
        ((DefaultHttpClient) getClient()).getCookieStore().clear();
    }

    @Override
    public void abort() {
        try {
            if (getClient() != null && httpPost != null) {
                Log.i(LOG_TAG, "Abort!");
                httpPost.abort();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception while abborting action", e);
        }
    }

    @Override
    public boolean isEndpointAvailable(String endpoint) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
            if (urlc.getResponseCode() == HttpStatusCode.OK) {
                return true;
            }
        } catch (MalformedURLException e1) {
        } catch (IOException e) {}
        return false;
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    protected class HttpStatusCode {
        private static final int UNHANDLED_EXCEPTION = -1;

        private static final int OK = 200;

        private static final int BAD_REQUEST = 400;
        private static final int UNAUTHORIZED = 401;
        private static final int FORBIDDEN = 402;
        private static final int NOT_FOUND = 404;
        private static final int METHOD_NOT_ALLOWED = 405;
        private static final int PROXY_AUTHENTICATION_REQUIRED = 407;
        private static final int REQUEST_TIMEOUT = 408;

        private static final int SERVER_ERROR = 500;
        private static final int NOT_IMPLEMENTED = 501;
        private static final int BAD_GATEWAY = 502;
        private static final int SERVICE_UNAVAILABLE = 503;
        private static final int GATEWAY_TIMEOUT = 504;
    }
}