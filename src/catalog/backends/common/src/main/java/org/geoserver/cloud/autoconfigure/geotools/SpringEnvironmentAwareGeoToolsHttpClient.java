/*
 * GeoTools - The Open Source Java GIS Toolkit http://geotools.org
 *
 * (C) 2004-2011, Open Source Geospatial Foundation (OSGeo)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.geotools;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.geoserver.cloud.autoconfigure.geotools.GeoToolsHttpClientProxyConfigurationProperties.ProxyHostConfig;
import org.geotools.http.HTTPClient;
import org.geotools.http.HTTPConnectionPooling;
import org.geotools.http.HTTPProxy;
import org.geotools.http.HTTPResponse;
import org.geotools.util.factory.GeoTools;
import org.springframework.core.env.PropertyResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Copy of GeoTools' {@link org.geotools.http.commons.MultithreadedHttpClient} due to its lack of
 * extensibility, adding the ability to set up the Apache HTTPClient's proxy configuration from
 * Spring {@link PropertyResolver}'s properties.
 */
@Slf4j
class SpringEnvironmentAwareGeoToolsHttpClient extends org.geotools.http.AbstractHttpClient
        implements HTTPClient, HTTPConnectionPooling, HTTPProxy {

    private final PoolingHttpClientConnectionManager connectionManager;

    private HttpClient client;

    private RequestConfig connectionConfig;

    private BasicCredentialsProvider credsProvider = null;

    private GeoToolsHttpClientProxyConfigurationProperties proxyConfig;

    public SpringEnvironmentAwareGeoToolsHttpClient(
            @NonNull GeoToolsHttpClientProxyConfigurationProperties proxyConfig) {
        this.proxyConfig = proxyConfig;
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(6);
        connectionManager.setDefaultMaxPerRoute(6);
        connectionConfig =
                RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.DEFAULT)
                        .setExpectContinueEnabled(true)
                        .setSocketTimeout((int) ofSeconds(30).toMillis())
                        .setConnectTimeout((int) ofSeconds(30).toMillis())
                        .build();
        resetCredentials();
        client = builder().build();
    }

    @Override
    public void setUser(String user) {
        super.setUser(user);
        resetCredentials();
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
        resetCredentials();
    }

    private RequestConfig connectionConfig(URL url) {
        RequestConfig reqConf = this.connectionConfig;
        Optional<HttpHost> proxy = proxy(url);
        if (proxy.isPresent()) {
            HttpHost proxyHost = proxy.get();
            reqConf = RequestConfig.copy(reqConf).setProxy(proxyHost).build();
        }
        return reqConf;
    }

    private Optional<HttpHost> proxy(URL url) {
        final GeoToolsHttpClientProxyConfigurationProperties config = this.proxyConfig;
        final String host = url.getHost();
        return config.ofProtocol(url.getProtocol()).forHost(host).map(this::toHttpHost);
    }

    private HttpHost toHttpHost(ProxyHostConfig conf) {
        String host = conf.getHost();
        int port = conf.port();
        return new HttpHost(host, port);
    }

    private void resetCredentials() {
        // overrides defaulting to SystemDefaultCredentialsProvider
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        if (user != null && password != null) {
            setTargetCredentials(provider, user, password);
        }
        setProxyCredentials(provider, this.proxyConfig.getHttp());
        setProxyCredentials(provider, this.proxyConfig.getHttps());
        this.credsProvider = provider;
        client = builder().build();
    }

    private void setProxyCredentials(BasicCredentialsProvider provider, ProxyHostConfig proxy) {
        if (proxy.isSecured()) {
            AuthScope scope = new AuthScope(toHttpHost(proxy));
            String proxyUser = proxy.getUser();
            String proxyPassword = proxy.getPassword();
            Credentials credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
            provider.setCredentials(scope, credentials);
        }
    }

    private void setTargetCredentials(
            BasicCredentialsProvider provider, String userName, String pwd) {
        AuthScope authscope = AuthScope.ANY;
        Credentials credentials = new UsernamePasswordCredentials(userName, pwd);
        provider.setCredentials(authscope, credentials);
    }

    private HttpClientBuilder builder() {
        HttpClientBuilder builder =
                HttpClientBuilder.create()
                        .setUserAgent(
                                "GeoTools/%s (%s)"
                                        .formatted(
                                                GeoTools.getVersion(),
                                                this.getClass().getSimpleName()))
                        .useSystemProperties()
                        .setConnectionManager(connectionManager);
        if (credsProvider != null) {
            builder.setDefaultCredentialsProvider(credsProvider);
        }
        return builder;
    }

    @Override
    public HttpMethodResponse post(
            final URL url, final InputStream postContent, final String postContentType)
            throws IOException {
        return post(url, postContent, postContentType, null);
    }

    @Override
    public HttpMethodResponse post(
            URL url, InputStream postContent, String postContentType, Map<String, String> headers)
            throws IOException {
        if (headers == null) {
            headers = Map.of();
        } else {
            headers = Map.copyOf(headers); // avoid parameter modification
        }

        HttpPost postMethod = new HttpPost(url.toExternalForm());
        postMethod.setConfig(connectionConfig(url));
        HttpEntity requestEntity;
        if (credsProvider != null) {
            // we can't read the input stream twice as would be needed if the server asks us to
            // authenticate
            String input =
                    new BufferedReader(new InputStreamReader(postContent, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
            requestEntity = new StringEntity(input);
        } else {
            requestEntity = new InputStreamEntity(postContent);
        }
        if (tryGzip) {
            postMethod.setHeader("Accept-Encoding", "gzip");
        }
        if (postContentType != null) {
            postMethod.setHeader("Content-type", postContentType);
        }

        setHeadersOn(headers, postMethod);

        postMethod.setEntity(requestEntity);

        HttpMethodResponse response = executeMethod(postMethod);

        if (200 != response.getStatusCode()) {
            postMethod.releaseConnection();
            throw new IOException(
                    "Server returned HTTP error code %d for URL %s"
                            .formatted(response.getStatusCode(), url.toExternalForm()));
        }

        return response;
    }

    /**
     * @return the http status code of the execution
     */
    private HttpMethodResponse executeMethod(HttpRequestBase method) throws IOException {

        HttpClientContext localContext = HttpClientContext.create();
        HttpResponse resp;
        if (credsProvider != null) {
            localContext.setCredentialsProvider(credsProvider);
            // see https://stackoverflow.com/a/21592593
            AuthCache authCache = new BasicAuthCache();
            URI target = method.getURI();
            authCache.put(
                    new HttpHost(target.getHost(), target.getPort(), target.getScheme()),
                    new BasicScheme());
            localContext.setAuthCache(authCache);
            resp = client.execute(method, localContext);
        } else {
            resp = client.execute(method);
        }

        return new HttpMethodResponse(resp);
    }

    @Override
    public HTTPResponse get(final URL url) throws IOException {
        return this.get(url, null);
    }

    @Override
    public HTTPResponse get(URL url, Map<String, String> headers) throws IOException {
        if (isFile(url)) {
            return createFileResponse(url);
        }
        if (headers == null) {
            headers = Map.of();
        } else {
            headers = Map.copyOf(headers); // avoid parameter modification
        }
        Map<String, String> extraParams = getExtraParams();
        if (!extraParams.isEmpty()) {
            url = appendURL(url, extraParams);
        }

        HttpGet getMethod = new HttpGet(url.toExternalForm());
        getMethod.setConfig(connectionConfig(url));

        if (tryGzip) {
            getMethod.setHeader("Accept-Encoding", "gzip");
        }

        setHeadersOn(headers, getMethod);

        HttpMethodResponse response = executeMethod(getMethod);

        if (200 != response.getStatusCode()) {
            getMethod.releaseConnection();
            throw new IOException(
                    "Server returned HTTP error code %d for URL %s"
                            .formatted(response.getStatusCode(), url.toExternalForm()));
        }
        return response;
    }

    private void setHeadersOn(Map<String, String> headers, HttpRequestBase request) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            log.debug("Setting header {} = {}", header.getKey(), header.getValue());
            request.setHeader(header.getKey(), header.getValue());
        }
    }

    @Override
    public int getConnectTimeout() {
        return (int) ofMillis(connectionConfig.getConnectionRequestTimeout()).toSeconds();
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        connectionConfig =
                RequestConfig.copy(connectionConfig)
                        .setConnectionRequestTimeout((int) ofSeconds(connectTimeout).toMillis())
                        .build();
    }

    @Override
    public int getReadTimeout() {
        return (int) ofMillis(connectionConfig.getSocketTimeout()).toSeconds();
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        connectionConfig =
                RequestConfig.copy(connectionConfig)
                        .setSocketTimeout((int) ofSeconds(readTimeout).toMillis())
                        .build();
    }

    @Override
    public int getMaxConnections() {
        return connectionManager.getDefaultMaxPerRoute();
    }

    @Override
    public void setMaxConnections(final int maxConnections) {
        connectionManager.setDefaultMaxPerRoute(maxConnections);
        connectionManager.setMaxTotal(maxConnections);
    }

    static class HttpMethodResponse implements HTTPResponse {

        private org.apache.http.HttpResponse methodResponse;

        private InputStream responseBodyAsStream;

        public HttpMethodResponse(final org.apache.http.HttpResponse methodResponse) {
            this.methodResponse = methodResponse;
        }

        public int getStatusCode() {
            if (methodResponse != null) {
                StatusLine statusLine = methodResponse.getStatusLine();
                return statusLine.getStatusCode();
            } else {
                return -1;
            }
        }

        @Override
        public void dispose() {
            if (responseBodyAsStream != null) {
                try {
                    responseBodyAsStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            if (methodResponse != null) {
                methodResponse = null;
            }
        }

        @Override
        public String getContentType() {
            return getResponseHeader("Content-Type");
        }

        @Override
        public String getResponseHeader(final String headerName) {
            Header responseHeader = methodResponse.getFirstHeader(headerName);
            return responseHeader == null ? null : responseHeader.getValue();
        }

        @Override
        public InputStream getResponseStream() throws IOException {
            if (responseBodyAsStream == null) {
                responseBodyAsStream = methodResponse.getEntity().getContent();
                // commons httpclient does not handle gzip encoding automatically, we have to check
                // ourselves: https://issues.apache.org/jira/browse/HTTPCLIENT-816
                Header header = methodResponse.getFirstHeader("Content-Encoding");
                if (header != null && "gzip".equals(header.getValue())) {
                    responseBodyAsStream = new GZIPInputStream(responseBodyAsStream);
                }
            }
            return responseBodyAsStream;
        }

        @Override
        public String getResponseCharset() {
            final Header encoding = methodResponse.getEntity().getContentEncoding();
            return encoding == null ? null : encoding.getValue();
        }
    }

    @Override
    public void close() {
        this.connectionManager.shutdown();
    }
}
