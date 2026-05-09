package org.lb4.loadbalancer.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SessionManager {

    private final Map<Long, Session> sessions = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public Session createSession() {
        return new Session(nextId.getAndIncrement());
    }

    public void register(Session session) {
        sessions.put(session.getSessionId(), session);
    }

    public void remove(Session session) {
        sessions.remove(session.getSessionId());
    }

    public Collection<Session> getAllSessions() {
        return sessions.values();
    }

    public int activeSessionCount() {
        return sessions.size();
    }

}
