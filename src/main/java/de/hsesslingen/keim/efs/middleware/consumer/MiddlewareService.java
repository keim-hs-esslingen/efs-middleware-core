/*
 * MIT License
 * 
 * Copyright (c) 2020 Hochschule Esslingen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package de.hsesslingen.keim.efs.middleware.consumer;

import static de.hsesslingen.keim.efs.middleware.consumer.ServiceDirectoryProxy.buildGetAllRequest;
import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.ICoordinates;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import static de.hsesslingen.keim.efs.mobility.service.MobilityService.API.BOOKING_API;
import static de.hsesslingen.keim.efs.mobility.service.MobilityService.API.OPTIONS_API;
import static de.hsesslingen.keim.efs.mobility.service.MobilityService.API.PLACES_API;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import java.time.ZonedDateTime;
import static java.util.Collections.disjoint;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author keim
 */
@Service
@Lazy
@EnableScheduling
public class MiddlewareService {

    private static Logger logger = getLogger(MiddlewareService.class);

    private static final Function<String, String> defaultTokenGetter = (serviceId) -> null;

    @Value("${middleware.service-directory-url}")
    public String baseUrl;

    private CompletableFuture<List<ProviderProxy>> providersFuture = new CompletableFuture<>();

    private synchronized CompletableFuture<List<ProviderProxy>> getProvidersFuture() {
        return providersFuture;
    }

    private synchronized void setProvidersFuture(CompletableFuture<List<ProviderProxy>> providersFuture) {
        this.providersFuture = providersFuture;
    }

    private List<MobilityService> fetchAvailableProviders() {
        var response = buildGetAllRequest(baseUrl)
                .toInternal()
                .go();

        if (response == null) {
            logger.warn("Services request returned \"null\" as response. This must be some kind of error.");
            return List.of();
        }

        var list = response.getBody();

        if (list == null) {
            logger.warn("The retunred services list from ServiceDirectory is \"null\".");
            return List.of();
        }

        return list;
    }

    /**
     * Makes some sanity checks on retrieved mobility service objects to prevent
     * exceptions in later processing of them. Semantical changes to the objects
     * are reduced to minimum.
     *
     * @param service
     */
    private void sanitizeMobilityService(MobilityService service) {
        if (service.getApis() == null) {
            service.setApis(Set.of());
        }
        if (service.getModes() == null) {
            service.setModes(Set.of());
        }
        if (service.getMobilityTypes() == null) {
            service.setMobilityTypes(Set.of());
        }
    }

    @Scheduled(
            initialDelayString = "${middleware.refresh-services-cache-initial-delay:0}",
            fixedRateString = "${middleware.refresh-services-cache-rate:86400000}"
    )
    public void refreshAvailableProviders() {
        logger.info("Refreshing available services from service-directory.");

        var all = fetchAvailableProviders();
        
        var services = all.stream()
                // Sanitize invalid services to prevent null pointers and other stuff.
                .peek(this::sanitizeMobilityService)
                .map(ProviderProxy::new)
                .collect(toList());

        if (providersFuture.isDone()) {
            setProvidersFuture(new CompletableFuture<>());
        }

        providersFuture.complete(services);
        logger.debug("Done refreshing available services.");
    }

    public List<ProviderProxy> getProviders() {
        try {
            return getProvidersFuture().get();
        } catch (InterruptedException | ExecutionException ex) {
            logger.warn("Thread got interrupted while waiting for services to be retrieved.");
            return getProviders();
        }
    }

    public Stream<ProviderProxy> getProviders(Set<String> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return getProviders().stream();
        }

        return getProviders().stream().filter(p -> serviceIds.contains(p.getServiceId()));
    }

    public ProviderProxy getProvider(String serviceId) {
        return getProviders().stream()
                .filter(p -> p.getServiceId().equals(serviceId))
                .findAny()
                .orElse(null);
    }

    public Stream<ProviderProxy> getProviders(
            Set<Mode> anyOfTheseModesSupported,
            Set<MobilityType> anyOfTheseMobilityTypesSupported,
            Set<API> allOfTheseApisSupported
    ) {
        var stream = getProviders().stream();

        if (allOfTheseApisSupported != null && !allOfTheseApisSupported.isEmpty()) {
            stream = stream.filter(s -> s.getService().getApis().containsAll(allOfTheseApisSupported));
        }

        if (anyOfTheseModesSupported != null && !anyOfTheseModesSupported.isEmpty()) {
            stream = stream.filter(s -> !disjoint(anyOfTheseModesSupported, s.getService().getModes()));
        }

        if (anyOfTheseMobilityTypesSupported != null && !anyOfTheseMobilityTypesSupported.isEmpty()) {
            stream = stream.filter(s -> !disjoint(anyOfTheseMobilityTypesSupported, s.getService().getMobilityTypes()));
        }

        return stream;
    }

    public Stream<Place> searchPlaces(
            String query,
            ICoordinates areaCenter,
            Integer radiusMeter,
            Integer limitToPerProvider,
            Function<String, String> serviceTokenGetter
    ) {
        var tokenGetter = serviceTokenGetter == null ? defaultTokenGetter : serviceTokenGetter;

        var requests = getProviders().stream()
                .filter(p -> p.supportsApi(PLACES_API))
                .map(p -> p.createSearchPlacesRequest(query, areaCenter, radiusMeter, limitToPerProvider, tokenGetter.apply(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(places -> places != null)
                .flatMap(places -> places.stream());
    }

    public Stream<Place> searchPlaces(
            String query,
            ICoordinates areaCenter,
            Integer radiusMeter,
            Integer limitToPerProvider,
            Map<String, String> serviceIdTokenMap
    ) {
        Map<String, String> tokenMap = serviceIdTokenMap == null ? Map.of() : serviceIdTokenMap;

        var requests = getProviders(tokenMap.keySet())
                .filter(p -> p.supportsApi(PLACES_API))
                .map(p -> p.createSearchPlacesRequest(query, areaCenter, radiusMeter, limitToPerProvider, tokenMap.get(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(places -> places != null)
                .flatMap(places -> places.stream());
    }

    public Stream<Options> getOptions(
            ICoordinates from,
            ICoordinates to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitToPerProvider,
            Function<String, String> serviceTokenGetter
    ) {
        var tokenGetter = serviceTokenGetter == null ? defaultTokenGetter : serviceTokenGetter;

        var requests = getProviders(modesAllowed, mobilityTypesAllowed, Set.of(OPTIONS_API))
                .map(p -> p.createGetOptionsRequest(from, to, startTime, endTime, radiusMeter, sharingAllowed, modesAllowed, mobilityTypesAllowed, limitToPerProvider, tokenGetter.apply(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(options -> options != null)
                .flatMap(options -> options.stream());
    }

    public Stream<Options> getOptions(
            ICoordinates from,
            ICoordinates to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitToPerProvider,
            Map<String, String> serviceIdTokenMap
    ) {
        Map<String, String> tokenMap = serviceIdTokenMap == null ? Map.of() : serviceIdTokenMap;

        var ids = tokenMap.keySet();

        var requests = getProviders(modesAllowed, mobilityTypesAllowed, Set.of(OPTIONS_API))
                .filter(p -> ids.contains(p.getServiceId()))
                .map(p -> p.createGetOptionsRequest(from, to, startTime, endTime, radiusMeter, sharingAllowed, modesAllowed, mobilityTypesAllowed, limitToPerProvider, tokenMap.get(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(options -> options != null)
                .flatMap(options -> options.stream());
    }

    public Stream<Booking> getBookings(
            Set<String> serviceIds,
            Function<String, String> serviceTokenGetter
    ) {
        var tokenGetter = serviceTokenGetter == null ? defaultTokenGetter : serviceTokenGetter;

        var requests = getProviders(serviceIds)
                .filter(p -> p.supportsApi(BOOKING_API))
                .map(p -> p.createGetBookingsRequest(tokenGetter.apply(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(options -> options != null)
                .flatMap(options -> options.stream());
    }

    public Stream<Booking> getBookings(Map<String, String> serviceIdTokenMap) {
        Map<String, String> tokenMap = serviceIdTokenMap == null ? Map.of() : serviceIdTokenMap;
        return getBookings(tokenMap.keySet(), tokenMap::get);
    }

}
