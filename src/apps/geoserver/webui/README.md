# Web UI Microservice

The traditional GeoServer's [Wicket](https://wicket.apache.org/) based configuration user interface built as a microservice.

Just the UI, no REST api, or OWS services.

For the time being, this microservice is not ready to scale out, so keep it at one instance. More over, it may make sense to
just wake it up when you need to use it, and then shut it down.

When it's up, it'll be available at the `/web` gateway service entry point, `http://localhost:9090/web`
if you're running the default docker composition provided at the root project directory.

## Build

Follow the regular maven build as explained in the project's root `README.md`.

Optionally include or exclude components by enabling or disabling the following maven profiles.

### Maven Profiles

The following profiles are enabled by default:

* `wfs`
* `wms`
* `wcs`
* `wps`
* `gwc` (not yet functional)
* `web-resource`
* `importer`

And the following profiles can be enabled explicitly at build time:

* gwc (GWC integration is not ready, so don't do it for now)

Additional profiles may be added as new components are included in the build.

Disabling or enabling a profile is a build-time step, resulting in the profile's
module dependencies being or not included in the final application binary.

Nonetheless, Cloud Native Geoserver's approach to plugin inclusion and enablement
is to incorporate the full curated list of plugins and extensions at build time,
and being able to enable or disable them through configuration properties. See the
"Configuration" section bellow for more detail.

To enable a specific profile, run 

    mvn -P <profile1>,<profile2> clean install

to disable a profile, it has to be negated with the exclamation mark (and excaped in most shells): 

    mvn -P \!<profile1>,\!<profile2> clean install


## Configuration

Each profile comes with a spring-boot auto-configuration, that allows you to disable a component
through the `webui-service.yml` config file when using the default externalized configuration
(see the `config/webui-service.yml` file at the project's root folder).

Hence, for a certain web-ui module to be enabled and hence accessible through the user interface,
its dependencies have to be on the classpath and it has to be enabled through the service configuration
properties. The auto-configurations take care of checking both conditions, and they're all enabled
by default when the module is included in the application.

To disable or further configure a certain module, may it expose additional configuration options,
just edit the microservice's config file. 

For example, to disable the `wms` component, edit the following section,
set the `geoserver.web-ui.wms=false` property.

Here is the full list of `web-ui` config properties in YAML format:

```yaml
geoserver:
  web-ui:
    # These are all default values, here just for reference. You can omit them and add only the ones to disable or further configure
    security.enabled: true
    wfs.enabled: true
    wms.enabled: true
    wcs.enabled: true
    wps.enabled: true
    gwc.enabled: false # not ready yet
    extensions:
      importer.enabled: true
    demos:
      enabled: true
      wps-request-builder: true
      wcs-request-builder: true
      demo-requests: true
      srs-list: true
      reprojection-console: true
      layer-preview-page:
        enabled: true
        common-formats:
          open-layers: true
          gml: true
          kml: true
    tools:
      enabled: true
      resource-browser: true
      catalog-bulk-load: true
```

All these config options can also be provided at run-time through environment variables or Java System Properties, or overridden by a spring profile configuration file. 

The nested properties obey their parent's `enabled` settings, so for example, if `geoserver.web-ui.demos=false`, all its children are disable disregard their individual value.

## Differences with monolith GeoServer application's Web UI

- The `LogPage` (`/web/wicket/bookmarkable/org.geoserver.web.admin.LogPage`) menu entry is removed.

### Changes to GlobalSettings page:

A copy of `GlobalSettingsPage.html` is provided, setting `class="unused"` to HTML elements
that shall be hidden.

The contributed `geoserver-cloud.css` stylesheet defines `.unused{display: none;}`.

- The "**Logging settings**" section set is hidden.
- The "**Lock Provider**" selection is hidden, each Configuration and Catalog backend implementation is in charge of
setting up a lock provider appropriate to the backend that works effectively on a distributed system.
- The "**Web UI Mode**" selection is hidden. `WebUIApplicationAutoConfiguration` forces it to be
`WebUIMode.DO_NOT_REDIRECT` "Never redirect to persist page state (supports clustering but doesn't
prevent double submit problem)."
- The "**Proxy Base URL**" field is hidden. The gateway app is in charge of providing the appropriate
`X-Forward` headers. The spring-boot applications must set the `server.forward-headers-strategy=framework`
config property to let spring-boot's `ForwardedHeaderFilter` take care of reflecting the client-originated
protocol and address in the `HttpServletRequest`.
