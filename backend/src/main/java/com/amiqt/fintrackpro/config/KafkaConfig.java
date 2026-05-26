package com.amiqt.fintrackpro.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String PAYROLL_COMMANDS_TOPIC = "payroll-commands";

    @Bean
    public NewTopic payrollCommandsTopic() {
        return TopicBuilder.name(PAYROLL_COMMANDS_TOPIC)
                .partitions(3) // 3 partitions for horizontal scaling of worker consumers
                .replicas(1)
                .build();
    }
}
