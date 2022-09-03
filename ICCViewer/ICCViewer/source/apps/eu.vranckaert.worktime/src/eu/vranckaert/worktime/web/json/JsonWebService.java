/*
 * Copyright 2012 Dirk Vranckaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.vranckaert.worktime.web.json;

import eu.vranckaert.worktime.web.json.exception.CommunicationException;
import eu.vranckaert.worktime.web.json.exception.WebException;
import eu.vranckaert.worktime.web.json.model.AuthorizationHeader;
import eu.vranckaert.worktime.web.json.model.JsonEntity;
import eu.vranckaert.worktime.web.json.model.JsonResult;

import java.util.Map;

/**
 * Date: 16/10/12
 * Time: 19:00
 *
 * @author Dirk Vranckaert
 */
public interface JsonWebService {
    /**
     * Invoke a JSON webrequest for a certain method, with a certain JsonEntity as parameter for the method. If the
     * jsonEntity is null a method will be called without any parameters. Executed as http POST!
     *
     * @param baseUrl The base url of the web-method to invoke.
     * @param methodName The name of the webservice method to be executed.
     * @param authorizationHeader The {@link AuthorizationHeader} containing the value of for 'authorization' header. if
     * null the header will not be set.
     * @param jsonEntity The method parameter.
     * @param parameters A list of the parameters to be passed to the method. The order of the parameters is
     *                   important!
     * @return The {@link JsonResult} from the webservice.
     * @throws WebException Thrown if the status code of the response is something else than 200.
     * @throws CommunicationException If any low-level communication error appears this exception is thrown.
     */
    JsonResult webInvokePost(String baseUrl, String methodName, AuthorizationHeader authorizationHeader, Map<String, String> ampParams, JsonEntity jsonEntity, String... parameters) throws WebException, CommunicationException;

    /**
     * Invoke a JSON webrequest for a certain method. If the parameters are null a method call will
     * be initiated without any parameters. Executed as http GET!
     *
     * @param baseUrl The base url of the web-method to invoke.
     * @param methodName The name of the webservice method to be executed.
     * @param authorizationHeader The {@link AuthorizationHeader} containing the value of for 'authorization' header. if
     * null the header will not be set.
     * @param parameters A list of the parameters to be passed to the method. The order of the parameters is
     *                   important!
     * @return The {@link JsonResult} from the webservice.
     * @throws WebException Thrown if the status code of the response is something else than 200.
     * @throws CommunicationException If any low-level communication error appears this exception is thrown.
     */
    JsonResult webInvokeGet(String baseUrl, String methodName, AuthorizationHeader authorizationHeader, Map<String, String> ampParams, String... parameters) throws WebException, CommunicationException;

    /**
     * Invoke a JSON webrequest for a certain method. If the parameters are null a method call will
     * be initiated without any parameters. Executed as http PUT!
     *
     * @param baseUrl The base url of the web-method to invoke.
     * @param methodName The name of the webservice method to be executed.
     * @param authorizationHeader The {@link AuthorizationHeader} containing the value of for 'authorization' header. if
     * null the header will not be set.
     * @param parameters A list of the parameters to be passed to the method. The order of the parameters is
     *                   important!
     * @return The {@link JsonResult} from the webservice.
     * @throws WebException Thrown if the status code of the response is something else than 200.
     * @throws CommunicationException If any low-level communication error appears this exception is thrown.
     */
    JsonResult webInvokePut(String baseUrl, String methodName, AuthorizationHeader authorizationHeader, Map<String, String> ampParams, String... parameters) throws WebException, CommunicationException;

    /**
     * Invoke a JSON webrequest for a certain method. If the parameters are null a method call will
     * be initiated without any parameters. Executed as http PUT!
     *
     * @param baseUrl The base url of the web-method to invoke.
     * @param methodName The name of the webservice method to be executed.
     * @param authorizationHeader The {@link AuthorizationHeader} containing the value of for 'authorization' header. if
     * null the header will not be set.
     * @param parameters A list of the parameters to be passed to the method. The order of the parameters is
     *                   important!
     * @return The http status code. This code must be handled request per request as probably each status code is used
     * to define a certain result (if it's ok, not the correct rights, item not found,...). Use the
     * {@link JsonWebServiceImpl.HttpStatusCode} for this purpose!
     * @throws CommunicationException If any low-level communication error
     * appears this exception is thrown.
     */
    int webInvokeDelete(String baseUrl, String methodName, AuthorizationHeader authorizationHeader, Map<String, String> ampParams, String... parameters) throws CommunicationException;

    //    String doGet(String methodName, Map<String, String> params) throws IOException;

    /**
     * Clear all the cookies
     */
    void clearCookies();

    /**
     * Only works on an {@link org.apache.http.client.methods.HttpPost}. This aborts the call.
     */
    void abort();

    /**
     * Checks if the endpoint is available or not.
     * @param endpoint The URL of the endpoint that needs to be reached.
     * @return {@link Boolean#TRUE} if the endpoint can be resolved and reached. Otherwise {@link Boolean#FALSE}.
     */
    boolean isEndpointAvailable(String endpoint);
}
