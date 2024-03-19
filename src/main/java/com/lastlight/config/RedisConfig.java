package com.lastlight.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    /**
     * To customarily build a redisTemplate object which is used for serializable key and value in redis storage
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        ObjectMapper mapper = new ObjectMapper();
        //这行代码设置了对象属性访问器的可见性（在序列化和反序列化过程中都可以访问），使得所有属性都可见（包括字段、getter和setter）
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //这行代码激活默认的类型信息，在反序列化过程中正确地恢复对象的类型信息（通过设置这些选项，Jackson将能够根据包含的类型信息，将JSON数据正确地转换回具体的Java对象类型。这对于处理多态对象非常重要，以确保在反序列化时对象的类型信息不丢失）
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        genericJackson2JsonRedisSerializer.serialize(mapper);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key、hash的key 采用 String序列化方式
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        // value、hash的value 采用 Jackson 序列化方式
        template.setValueSerializer(genericJackson2JsonRedisSerializer);
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        //使用前的连接等检查工作
        template.afterPropertiesSet();
        return template;
    }
}
