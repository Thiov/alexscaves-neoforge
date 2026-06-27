package com.github.alexmodguy.alexscaves.mcshim;
import net.neoforged.neoforge.common.*;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Minimal re-implementation of the (removed) NeoForge 1.21.1 WorldWorkerManager,
 * kept at its original package path so Alex's Caves code compiles unchanged.
 *
 * <p>Workers are pumped once per server tick (post). A worker is dropped once it
 * reports no remaining work or its {@code doWork()} returns {@code false}.
 */
public final class WorldWorkerManager {
    private static final List<IWorker> WORKERS = new ArrayList<>();

    private WorldWorkerManager() {
    }

    static {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> tickWorkers());
    }

    public static void addWorker(IWorker worker) {
        WORKERS.add(worker);
    }

    private static void tickWorkers() {
        Iterator<IWorker> iterator = WORKERS.iterator();
        while (iterator.hasNext()) {
            IWorker worker = iterator.next();
            if (!worker.hasWork() || !worker.doWork()) {
                iterator.remove();
            }
        }
    }

    public interface IWorker {
        boolean hasWork();

        boolean doWork();
    }
}
