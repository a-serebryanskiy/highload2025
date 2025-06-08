package tech.ydb;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.galaxio.gatling.kafka.javaapi.protocol.KafkaProtocolBuilder;

import java.util.Map;
import java.util.SplittableRandom;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static org.galaxio.gatling.kafka.javaapi.KafkaDsl.kafka;
import static tech.ydb.GatlingRunner.BOOTSTRAP_SERVERS;

public class Constant1MbSimulation extends Simulation {

    public static final String TOPIC_NAME = "test-topic";
    public static final int MESSAGE_SIZE_BYTES = 100 * 1024; // 100 kb
    public static final int BYTES_PER_SEC = 1 * 1024 * 1024; // 1 mb
    public static final int SIMULATION_DURATION_SEC = 10;

    private final KafkaProtocolBuilder kafkaProtocol = kafka()
            .topic(TOPIC_NAME)
            .properties(
                    Map.of(
                            ProducerConfig.ACKS_CONFIG, "all",
                            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS,
                            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer",
                            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer")
            );

    private final SplittableRandom random = new SplittableRandom();
    private final ScenarioBuilder kafkaProducer = scenario("Kafka Producer")
            .exec(kafka("Simple Message")
                    .send("", generateRandomPayload(random))
            );

    private static byte[] generateRandomPayload(SplittableRandom random) {
        byte[] payload = new byte[MESSAGE_SIZE_BYTES];
        for (int j = 0; j < MESSAGE_SIZE_BYTES; ++j)
            payload[j] = (byte) (random.nextInt(26) + 65);
        return payload;
    }

    {
        setUp(
                kafkaProducer.injectOpen(constantUsersPerSec((double) BYTES_PER_SEC / MESSAGE_SIZE_BYTES).during(SIMULATION_DURATION_SEC))
        ).protocols(kafkaProtocol);
    }

}
