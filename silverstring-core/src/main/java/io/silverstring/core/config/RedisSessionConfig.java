package io.silverstring.core.config;

import io.silverstring.core.repository.SafeDeserializationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.web.http.SessionRepositoryFilter;

@Configuration
public class RedisSessionConfig  extends RedisHttpSessionConfiguration {
    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    @Bean
    @Override
    public <S extends ExpiringSession> SessionRepositoryFilter<? extends ExpiringSession> springSessionRepositoryFilter(SessionRepository<S> sessionRepository) {
        return super.springSessionRepositoryFilter(new SafeDeserializationRepository<>(sessionRepository, redisTemplate));
    }
}
