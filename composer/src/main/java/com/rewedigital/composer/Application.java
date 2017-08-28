package com.rewedigital.composer;

import java.util.Objects;

import com.rewedigital.composer.proxy.HostRoutingHandler;
import com.rewedigital.composer.routing.BackendRouting;
import com.rewedigital.composer.routing.StaticRoutes;
import com.spotify.apollo.Environment;
import com.spotify.apollo.core.Service;
import com.spotify.apollo.http.client.HttpClientModule;
import com.spotify.apollo.httpservice.HttpService;
import com.spotify.apollo.httpservice.LoadingException;
import com.spotify.apollo.route.Route;

public class Application {

    public static void main(final String[] args) throws LoadingException {
        final ComposerInitializer initializer =
            new ComposerInitializer(new HostRoutingHandler(new BackendRouting(StaticRoutes.routes())));
        final Service service =
            HttpService
                .usingAppInit(initializer::init, "composer")
                .withModule(HttpClientModule.create())
                .build();
        HttpService.boot(service, args);
    }

    private static class ComposerInitializer {

        private final HostRoutingHandler handler;

        private ComposerInitializer(final HostRoutingHandler handler) {
            this.handler = Objects.requireNonNull(handler);
        }

        private void init(final Environment environment) {
            environment.routingEngine()
                .registerAutoRoute(Route.async("GET", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("POST", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("PATCH", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("DELETE", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("TRACE", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("HEADE", "/<path:path>", rc -> handler.execute(rc)));
        }

    }
}