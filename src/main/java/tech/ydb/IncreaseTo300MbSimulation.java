package tech.ydb;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.galaxio.gatling.kafka.javaapi.protocol.KafkaProtocolBuilder;
import org.galaxio.gatling.kafka.javaapi.request.expressions.Builders;

import java.util.Map;
import java.util.SplittableRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static org.galaxio.gatling.kafka.javaapi.KafkaDsl.kafka;
import static tech.ydb.GatlingRunner.BOOTSTRAP_SERVERS;

/**
 * This simulation increases load on specified kafka topic by 1mb every second till it reaches the load of 240mb/s.
 */
public class IncreaseTo300MbSimulation extends Simulation {

    public static final String TOPIC_NAME = "test-topic";
    public static final int MESSAGE_SIZE_BYTES = 100 * 1024; // 100Kb
    public static final int TARGET_MBS_PER_SECOND = 300;

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
    private final byte[] messagePayload = generateRandomPayload(random);
    private final ScenarioBuilder kafkaProducer = scenario("Kafka Producer")
            .exec(kafka("Simple Message")
                    .send((byte[]) null, new Builders.ByteArrayExpressionBuilder((session) -> messagePayload))
            );

    private static byte[] generateRandomPayload(SplittableRandom random) {
        byte[] payload = new byte[MESSAGE_SIZE_BYTES];
        for (int j = 0; j < MESSAGE_SIZE_BYTES; ++j)
            payload[j] = (byte) (random.nextInt(26) + 65);
        return payload;
    }

    {
        setUp(
                kafkaProducer.injectOpen(incrementUsersPerSec(10).times(TARGET_MBS_PER_SECOND).eachLevelLasting(1))
        ).protocols(kafkaProtocol);
    }

}
