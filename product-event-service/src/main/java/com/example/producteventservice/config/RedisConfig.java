package com.example.producteventservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** Redis configuration for pub/sub messaging. */
@Configuration
public class RedisConfig {

  /** Redis template for publishing messages. */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    return template;
  }

  /** Redis message listener container for subscribing to channels. */
  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory connectionFactory,
      MessageListenerAdapter messageListenerAdapter,
      ChannelTopic productEventsTopic) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(messageListenerAdapter, productEventsTopic);
    return container;
  }

  /** Message listener adapter for handling Redis messages. */
  @Bean
  public MessageListenerAdapter messageListenerAdapter(
      RedisSubscriberService redisSubscriberService) {
    return new MessageListenerAdapter(redisSubscriberService, "onMessage");
  }

  /** Channel topic for product events. */
  @Bean
  public ChannelTopic productEventsTopic() {
    return new ChannelTopic("product-events-channel");
  }
}
