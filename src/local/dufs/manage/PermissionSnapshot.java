package local.dufs.manage;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** One immutable directory/account observation; absent results never permit writes. */
final class PermissionSnapshot {
    interface Probe { DufsDirectory.Actions check(DufsDirectory.Item i) throws Exception; }
    final DufsDirectory directory;
    final int incomplete;
    final Set<DufsDirectory.Item> started;
    private final Map<DufsDirectory.Item,DufsDirectory.Actions> rights;

    private PermissionSnapshot(DufsDirectory directory,Map<DufsDirectory.Item,DufsDirectory.Actions> rights,Set<DufsDirectory.Item> started) {
        this.directory=directory;
        this.rights=Collections.unmodifiableMap(new HashMap<>(rights));
        this.started=Collections.unmodifiableSet(new HashSet<>(started));
        incomplete=directory.items.size()-this.rights.size();
    }
    DufsDirectory.Actions actions(DufsDirectory.Item i) {
        return rights.getOrDefault(i,DufsDirectory.Actions.NONE);
    }
    static PermissionSnapshot capture(DufsDirectory directory,ExecutorService executor,int concurrency,long budgetMillis,Probe probe) throws InterruptedException {
        return captureSelected(directory,directory.items,executor,concurrency,budgetMillis,probe);
    }
    DufsDirectory.Actions observed(DufsDirectory.Item i) {return rights.get(i);}
    // Cancellation need not stop the worker immediately. Seal under the same
    // lock as claim so the next wave never inherits a still-claimable entry.
    static final class ProbeWave {
        private final Set<DufsDirectory.Item> started=new HashSet<>();
        private final Map<DufsDirectory.Item,DufsDirectory.Actions> results=new HashMap<>();
        private boolean sealed;
        synchronized boolean claim(DufsDirectory.Item item) {
            return !sealed && !Thread.currentThread().isInterrupted() && started.add(item);
        }
        synchronized void record(DufsDirectory.Item item,DufsDirectory.Actions value) {
            if(!sealed && value!=null) results.put(item,value);
        }
        synchronized PermissionSnapshot seal(DufsDirectory directory) {
            sealed=true;
            return new PermissionSnapshot(directory,results,started);
        }
    }
    static PermissionSnapshot captureSelected(DufsDirectory directory,List<DufsDirectory.Item> candidates,ExecutorService executor,int concurrency,long budgetMillis,Probe probe) throws InterruptedException {
        ProbeWave wave=new ProbeWave();
        List<DufsDirectory.Item> unknown=new ArrayList<>();
        for(DufsDirectory.Item i:candidates) {
            DufsDirectory.Actions known=i.explicit();
            if(known==null) unknown.add(i); else wave.record(i,known);
        }
        if(unknown.isEmpty()) return wave.seal(directory);
        List<Callable<Void>> workers=new ArrayList<>(); AtomicInteger cursor=new AtomicInteger();
        for(int n=0;n<Math.min(unknown.size(),concurrency);n++) workers.add(()-> {
            while(!Thread.currentThread().isInterrupted()) {
                int index=cursor.getAndIncrement(); if(index>=unknown.size()) break;
                DufsDirectory.Item item=unknown.get(index);
                if(!wave.claim(item)) break;
                try { wave.record(item,probe.check(item)); }
                catch(InterruptedException e) { Thread.currentThread().interrupt(); break; }
                catch(Exception ignored) { /* Unknown, not denied or allowed. */ }
            }
            return null;
        });
        PermissionSnapshot result;
        try { executor.invokeAll(workers,budgetMillis,TimeUnit.MILLISECONDS); }
        finally { result=wave.seal(directory); }
        return result;
    }
}
