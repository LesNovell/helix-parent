/*
 *  Copyright (c) 2016 Les Novell
 *  ------------------------------------------------------
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 */

/*
 * @author Les Novell
 *
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 */

package io.helixservice.feature.health;

import com.google.common.collect.Multimap;
import io.helixservice.feature.configuration.locator.ResourceLocator;
import io.helixservice.feature.restservice.controller.HttpMethod;
import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.controller.Response;
import io.helixservice.feature.restservice.controller.annotation.Controller;
import io.helixservice.feature.restservice.controller.annotation.Endpoint;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * REST controller for Health and Heartbeat endpoints
 */
@Controller
public class HealthController {
    private static Logger LOG = LoggerFactory.getLogger(HealthController.class);

    private static final String UP_TIME_FORMAT = "%02d:%02d:%02d";
    private static final String UP_SINCE = "upSince";
    private static final String UP_TIME = "upTime";
    private static final String APP_VERSION = "appVersion";
    private static final String GIT_REMOTE_URL = "gitRemoteOriginUrl";
    private static final String GIT_COMMIT_ID = "gitCommitId";
    private static final String VERSION_FILE = "version.json";
    private static final String NAMED_APP_VERSION = "namedAppVersion";
    private static final String UNKNOWN = "unknown";

    private final OfflineProcessor<Multimap<String,String>> offlineProcessor;
    private final Clock clock;
    private final OffsetDateTime startedAt;
    private String commitId = UNKNOWN;
    private String remoteUrl = UNKNOWN;
    private String appVersion = UNKNOWN;

    /**
     * Create Health Controller REST endpoint
     *
     * @param offlineProcessor Component for determining if a server should be marked as offline
     * @param resourceLocator Resource locator for reporting version.json information injected by the build
     * @param clock Clock instance indicating the start time of the server; used in calculating uptime
     */
    public HealthController(OfflineProcessor<Multimap<String,String>> offlineProcessor, ResourceLocator resourceLocator, Clock clock) {
        checkNotNull(resourceLocator, "resourceLocator cannot be null");
        this.offlineProcessor = checkNotNull(offlineProcessor);
        this.clock = checkNotNull(clock);
        this.startedAt = OffsetDateTime.now(clock);

        try {
            Optional<JsonObject> json = resourceLocator.getJsonObject(VERSION_FILE);
            if (json.isPresent()) {
                parseJsonVersionFile(json.get());
            } else {
                LOG.warn("Unable to determine GIT versioning information");
            }
        } catch (Throwable t) {
            LOG.error("Unable to configure HealthController", t);
        }
    }

    @Endpoint(value = "/health/heartbeat", methods = HttpMethod.HEAD)
    public Response<String> heartbeatStatus(Request<String> request) {
        return Response.fromHttpStatusCode(httpStatusCode());
    }

    @Endpoint(value = "/health/heartbeat", methods = HttpMethod.GET)
    public Response<String> heartbeat(Request<String> request) {
        offlineProcessor.processInstruction(request.getParams());
        return Response.fromHttpStatusCode(httpStatusCode(), heartBeatData().toString());
    }

    private int httpStatusCode() {
        return Status.INSTANCE.isOnline() ? Status.SUCCESS : Status.FORCED_DOWN;
    }

    @Endpoint(value = "/health/healthcheck", methods = {HttpMethod.GET, HttpMethod.HEAD})
    public Response<String> healthCheck(Request<String> request) {
        return Response.fromHttpStatusCode(httpStatusCode());
    }

    private JsonObject heartBeatData() {
        return new JsonObject()
                .put(GIT_COMMIT_ID, commitId)
                .put(GIT_REMOTE_URL, remoteUrl)
                .put(APP_VERSION, appVersion)
                .put(UP_SINCE, startedAt.toString())
                .put(UP_TIME, currentUpTime());
    }

    private String currentUpTime() {
        long millis = Duration.between(startedAt, OffsetDateTime.now(clock)).toMillis();

        return String.format(UP_TIME_FORMAT, TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private void parseJsonVersionFile(JsonObject json) {
        this.commitId = json.getString(GIT_COMMIT_ID);
        this.remoteUrl = json.getString(GIT_REMOTE_URL);
        this.appVersion = json.getString(NAMED_APP_VERSION);
    }
}
