package com.github.alexmodguy.alexscaves.mcshim;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Re-implementation of the (removed) NeoForge 1.21.1 WorldWorkerManager, kept at its original package path
 * so Alex's Caves code compiles unchanged.
 *
 * <p>The previous shim pumped each worker exactly ONCE per server tick. Upstream instead drains workers in a
 * loop until a time budget (the remainder of the 50 ms tick, minimum 10 ms) is exhausted. That difference is
 * enormous in practice: {@code CaveBiomeMapWorldWorker.doWork()} advances its search spiral by a single
 * 64-block sample per call, so a cave-map lookup that upstream resolves in well under a second instead took
 * minutes to hours - reported as "the map just loads forever".
 *
 * <p>Also restores upstream's yield semantics: {@code doWork() == false} means "yield this tick, keep the
 * task", NOT "discard the worker". A worker is only dropped once {@code hasWork()} reports it is finished.
 */
public final class WorldWorkerManager {
    private static final List<IWorker> WORKERS = new ArrayList<>();
    private static long tickStart;
    private static int index;

    private WorldWorkerManager() {
    }

    static {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> tickStart = System.currentTimeMillis());
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> tickWorkers());
    }

    public static synchronized void addWorker(IWorker worker) {
        WORKERS.add(worker);
    }

    private static synchronized IWorker getNext() {
        return index < WORKERS.size() ? WORKERS.get(index++) : null;
    }

    private static synchronized void remove(IWorker worker) {
        if (WORKERS.remove(worker)) {
            index--;
        }
    }

    /** Faithful port of NeoForge 1.21.1 WorldWorkerManager.tick(boolean). */
    private static void tickWorkers() {
        index = 0;
        IWorker task = getNext();
        if (task == null) {
            return;
        }
        long budget = 50L - (System.currentTimeMillis() - tickStart);
        if (budget < 10L) {
            budget = 10L;
        }
        long deadline = System.currentTimeMillis() + budget;
        while (System.currentTimeMillis() < deadline && task != null) {
            boolean hasMore = task.doWork();
            if (!task.hasWork()) {
                remove(task);
                task = getNext();
            } else if (!hasMore) {
                task = getNext();
            }
        }
    }

    public interface IWorker {
        boolean hasWork();

        boolean doWork();
    }
}
