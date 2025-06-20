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
    }
}