package tech.ydb;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CreatePartitions {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "changeme:9092");
        AdminClient adminClient = AdminClient.create(props);

        for (int i = 0; i < 2000; i++) {
            String topicName = "topic-" + i;
            List<NewTopic> newTopics = Collections.singletonList(new NewTopic(topicName, 1000, (short) 3));
            adminClient.createTopics(newTopics).all().get();
            System.out.println("Created " + i + " topics");
        }
    }
}
