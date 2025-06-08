package tech.ydb;

import io.gatling.app.Gatling;

public class GatlingRunner {
    public static String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        String simulationName = args[0];
        BOOTSTRAP_SERVERS = args[1];

        System.out.printf("Starting %s test for bootstraps servers %s%n", simulationName, BOOTSTRAP_SERVERS);

        Gatling.main(new String[] {
                "--simulation", simulationName,
                "--results-folder", "target/gatling"
        });

//        Gatling.main(new String[] {
//                "-ro",
//                "/Users/serebryanskiy/arcadia/kikimr/testing/configs/lbk/lbk-devslice-1/benchmark-scripts/kafka/redundancy-test/edge-tests/broker-edge-test-150mb/1920mbs-simulation",
//                "--results-folder",
//                "/Users/serebryanskiy/IdeaProjects/highload2025/target/results"
//        });
    }
}