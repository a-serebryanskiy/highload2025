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
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "vla4-3539.lbk-devslice-1.logbroker.yandex.net:9092");
        AdminClient adminClient = AdminClient.create(props);

        for (int i = 53; i < 200; i++) {
            String topicName = "topic-" + i;
            List<NewTopic> newTopics = Collections.singletonList(new NewTopic(topicName, 1000, (short) 3));
//            adminClient.deleteTopics(List.of(topicName)).all().get();
            adminClient.createTopics(newTopics).all().get();
            System.out.println("Created " + i + " topics");
        }
    }
}
