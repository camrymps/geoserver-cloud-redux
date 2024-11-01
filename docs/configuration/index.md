# Cloud Native GeoServer externalized configuration guide

## Contents
{:.no_toc}

* Will be replaced with the ToC, excluding the "Contents" header
{:toc}

## Introduction

TBD

## JNDI Datasources

A custom and very simple JNDI implementation is used through the `org.geoserver.cloud:spring-boot-simplejndi` maven module, allowing to configure JNDI data sources through Spring-Boot's configuration properties.

```yaml
jndi:
  datasources:
    # the data source names (i.e. ds1, ds2) will be bound as java:comp/env/jdbc/ds1 and java:comp/env/jdbc/ds2:
    ds1:
      enabled: true
      wait-for-it: true
      wait-timeout: 60
      url: jdbc:postgresql://host:5432/database
      username: sa
      password: sa
      connection-timeout: 250
      idle-timeout: 60000
```

On Kubernetes, you can for example mount it as a config map in all the services.

## GeoServer configuration properties

## HTTP proxy for cascaded OWS (WMS/WMTS/WFS) Stores

Cascaded OWS stores make use of a SPI (Service Provider Interface) extension point to configure the appropriate GeoTools `HTTPClientFactory`.

We provide a Spring Boot Auto-configuration that can be configured through regular spring-boot externalized properties and only affects GeoTools HTTP clients instead of the whole JVM.

The usual way to set an http proxy is through the `http.proxyHost`, `http.proxyPort`,
`http.proxyUser`, `http.proxyPassword` Java System Properties.

In the context of Cloud Native GeoServer containerized applications, this presents a number of drawbacks:

* Standard Java proxy parameters only work with System properties,
  not OS environment variables, and setting system properties is more
  cumbersome than env variables (you have to modify the container run command).
* `http.proxyUser/Password` are not standard properties, though commonly used, it's kind of
JDK implementation dependent.
* Setting `-Dhttp.proxy* System properties affects all HTTP clients in the container, meaning
requests to the config-service, discovery-service, etc., will also try to go through the proxy,
or you need to go through the extra burden of figuring out how to ignore them.
* If the proxy is secured, and since the http client used may not respect the
http.proxyUser/Password parameters, the apps won't start since they'll get
HTTP 407 "Proxy Authentication Required".

The following externalized configuration properties apply, with these suggested default values:

```yaml
# GeoTools HTTP Client proxy configuration, allows configuring cascaded WMS/WMTS/WFS stores
# that need to go through an HTTP proxy without affecting all the http clients at the JVM level
# These are default settings. The enabled property can be set to false to disable the custom
# HTTPClientFactory altogether.
# The following OS environment variables can be set for easier configuration:
# HTTP(S)_PROXYHOST, HTTP(S)_PROXYPORT, HTTP(S)_PROXYUSER, HTTP(S)_PROXYPASSWORD, HTTP(S)_NONPROXYHOSTS
geotools:
  httpclient:
    proxy:
      enabled: true
      http:
        host: ${http.proxyHost:}
        port: ${http.proxyPort:}
        user: ${http.proxyUser:}
        password: ${http.proxyPassword:}
        nonProxyHosts: ${http.nonProxyHosts:localhost.*}
        # comma separated list of Java regular expressions, e.g.: nonProxyHosts: localhost, example.*
      https:
        host: ${https.proxyHost:${geotools.httpclient.proxy.http.host}}
        port: ${https.proxyPort:${geotools.httpclient.proxy.http.port}}
        user: ${https.proxyUser:${geotools.httpclient.proxy.http.user}}
        password: ${https.proxyPassword:${geotools.httpclient.proxy.http.password}}
        nonProxyHosts: ${https.nonProxyHosts:${geotools.httpclient.proxy.http.nonProxyHosts}}
```

### Configure HTTP proxy with environment variables in compose.yml

As mentioned above, regular JVM proxy configuration works with Java System properties
but not with Operating System environment variables.

The above `geotools.httpclient.proxy` config properties though allow to do so
easily as in the following `compose.yml` snippet:

```yaml
version: "3.8"
...
services:
...
  wms:
    image: geoservercloud/geoserver-cloud-wms:<version>
    environment:
      HTTP_PROXYHOST: 192.168.86.26
      HTTP_PROXYPORT: 80
      HTTP_PROXYUSER: jack
      HTTP_PROXYPASSWORD: insecure
...
```

## Configure admin user through environment variables

For Cloud-Native deployments, an `AuthenticationProvider` exists that allows to set an administrator account (username and password) through environment variables `GEOSERVER_ADMIN_USERNAME`/`GEOSERVER_ADMIN_PASSWORD`,
or Java System Properties `geoserver.admin.username` and `geoserver.admin.password`.

Useful for devOps to set the admin password through a Kubernetes secret,
instead of having to tweak the security configuration XML files with an init container or similar.

This authentication provider will be the first one tested for an HTTP Basic authorization, only
if both the above mentioned username and password config properties are provided,
and regardless of the authentication chain configured in GeoServer.

If only one of the `geoserver.admin.username` and `geoserver.admin.password` config properties
is provided, the application will fail to start.

If enabled (i.e. both admin username and password provided), a failed attempt to log
in will cancel the authentication chain, and no other authentication providers will be tested.

If the default `admin` username is used, it effectively overrides the admin password set in the
xml configuration. If a separate administrator username is given, the regular
`admin` user is **disabled**.

## Change the default geoserver path

By default, GeoServer Cloud services are available under `geoserver/cloud/web` path. You can change it by using the `GEOSERVER_BASE_PATH` or Java System Properties `geoserver.base-path`.

## Use GeoServer ACL

To use GeoServer ACL with GeoServer Cloud, you need to do the following steps:
- Have a database available for storing the GeoServer ACL configuration
- Add a GeoServer ACL instance to your deployment
- Update the GeoServer spring configuration to enable ACL based security (you can use the `acl` spring profile)

## Use OAuth authentication

You can enable OAuth authentication by replacing the default `gateway` image by the geOrchestra gateway (for example `georchestra/gateway:23.1-RC1`).
