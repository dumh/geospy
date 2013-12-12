package ru.mk.gs.http;

import ru.mk.gs.config.ConfigManager;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author mkasumov
 */
public class HttpServicePool {

    private static final int POOL_SIZE = 1;
    private static volatile BlockingQueue<HttpService> POOL;

    public static void init(ConfigManager configManager) {
        POOL = new ArrayBlockingQueue<HttpService>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            POOL.add(new HttpService(configManager));
        }
    }

    public static <T> T withService(ServiceAction<T> action) {
        try {
            final BlockingQueue<HttpService> pool = POOL;
            if (pool == null) {
                throw new RuntimeException("Http service pool not initialized");
            }
            final HttpService service = pool.take();
            try {
                return action.doAction(service);
            } finally {
                pool.put(service);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted", e);
        }
    }

    public static interface ServiceAction<T> {
        T doAction(HttpService service);
    }
}
