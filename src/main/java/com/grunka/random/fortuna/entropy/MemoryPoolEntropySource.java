package com.grunka.random.fortuna.entropy;

import com.grunka.random.fortuna.Util;
import com.grunka.random.fortuna.accumulator.EntropySource;
import com.grunka.random.fortuna.accumulator.EventAdder;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MemoryPoolEntropySource implements EntropySource {

    @Override
    public void event(EventAdder adder) {
        long sum = 0;
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            if (memoryPoolMXBean.isValid()) {
                MemoryUsage usage = memoryPoolMXBean.getUsage();
                if (usage != null) {
                    sum += usage.getUsed();
                }
                MemoryUsage collectionUsage = memoryPoolMXBean.getCollectionUsage();
                if (collectionUsage != null) {
                    sum += collectionUsage.getUsed();
                }
            }
        }
        if (sum > 0) {
            adder.add(Util.twoLeastSignificantBytes(sum + System.currentTimeMillis()));
        }
    }

    @Override
    public Future<?> schedule(Runnable runnable, ScheduledExecutorService scheduler) {
        return scheduler.scheduleWithFixedDelay(runnable, 0, 5, TimeUnit.SECONDS);
    }
}
