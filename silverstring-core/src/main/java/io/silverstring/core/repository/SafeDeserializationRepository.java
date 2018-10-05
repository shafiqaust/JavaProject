package io.silverstring.core.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SerializationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;

@Slf4j
public class SafeDeserializationRepository<S extends ExpiringSession> implements SessionRepository<S> {
    private final SessionRepository<S> delegate;
    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String BOUNDED_HASH_KEY_PREFIX = "spring:session:sessions:";

    public SafeDeserializationRepository(SessionRepository<S> delegate, RedisTemplate<Object, Object> redisTemplate) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public S createSession() {
        return delegate.createSession();
    }

    @Override
    public void save(S session) {
        delegate.save(session);
    }

    @Override
    public S getSession(String id) {
        try {
            return delegate.getSession(id);
        } catch(SerializationException ex) {
            log.info("Deleting non-deserializable session with key {}", id);
            redisTemplate.delete(BOUNDED_HASH_KEY_PREFIX + id);
            return null;
        }
    }

    @Override
    public void delete(String id) {
        delegate.delete(id);
    }
}
