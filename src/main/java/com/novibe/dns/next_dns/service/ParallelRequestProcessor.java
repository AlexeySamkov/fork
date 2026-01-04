package com.novibe.dns.next_dns.service;

import com.novibe.common.util.FunctionWrapper;
import com.novibe.common.util.Log;
import com.novibe.dns.next_dns.http.response.NextDnsResponse;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.Objects.isNull;

public abstract class ParallelRequestProcessor {

    @SneakyThrows
    protected <D, R extends NextDnsResponse<?>> void callApi(List<D> requestList, Function<D, R> request) {
        int CHUNK_SIZE = 60;
        int totalAmount = requestList.size();

        for (int i = 0; i < totalAmount; i += CHUNK_SIZE) {
            int chunkEnd = Math.min(i + CHUNK_SIZE, totalAmount);
            List<D> chunk = requestList.subList(i, chunkEnd);

            List<NextDnsResponse.NextDnsApiError> errors = sendParallelRequests(chunk, request);
            if (errors.isEmpty()) {
                Log.common("\nChunk %s have been processed. Total progress: %s/%s".formatted(i / CHUNK_SIZE + 1, chunkEnd, totalAmount));
            } else {
                Log.fail("Failed request (%s of %s): %s".formatted(errors.size(), totalAmount, errors));
            }
            if (chunkEnd == totalAmount) {
                Log.common("All chunks have been processed");
                return;
            }
            for (int j = 60; j > 0; j--) {
                Log.progress("Waiting for api rate limit to reset: " + j);
                Thread.sleep(Duration.of(1, TimeUnit.SECONDS.toChronoUnit()));
            }
        }
    }

    private <D, R extends NextDnsResponse<?>> List<NextDnsResponse.NextDnsApiError> sendParallelRequests(Collection<D> list, Function<D, R> request) {
        AtomicInteger counter = new AtomicInteger();
        @Cleanup ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        return list.stream().map(dto -> executor.submit(() -> request.apply(dto)))
                .map(FunctionWrapper.wrap(Future::get))
                .peek(response -> {
                    if (isNull(response) || isNull(response.getErrors()))
                        Log.progress(counter.incrementAndGet() + "/" + list.size());
                })
                .filter(Objects::nonNull)
                .map(NextDnsResponse::getErrors)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }

}
