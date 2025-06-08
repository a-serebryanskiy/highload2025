# Репозиторий с нагрузочными тестами для доклада на Highload 2025

Ссылка на доклад: https://highload.ru/spb/2025/abstracts/14333

Какие тесты в этом репозитории есть:
1. Постоянная нагрузка в 1Mb с помощью Gatling - [Constant1MbSimulation.java](src/main/java/tech/ydb/Constant1MbSimulation.java)
2. Увеличение нагрузки каждую секунду на 1 mb вплоть до 240 mb/s - [IncreaseTo240MbSimulation.java](src/main/java/tech/ydb/IncreaseTo240MbSimulation.java)

Что еще есть в этом репозитории:
1. Отчеты о тестах нагрузки kafka и ydb, проведенных перед докладом в июне 2025-го года, лежат в папке [june_2025_results](june_2025_results).
2. Скрипт для постепенного создания 200 000 партиций на кластере лежит в файле [CreatePartitions.java](src/main/java/tech/ydb/CreatePartitions.java)

# Версии

Версия Kafka, на которой прогонялись тесты: (с KRaft - 4.0.0, с zookeeper - 3.8.1)

Версия YDB, на которой прогонялись тесты: 25.1

# Как запустить

Ниже описано, как я запускал эти тесты. По идее вы можете повторить все тоже самое на своем железе

## Подготовка

И для YDB и для Kafka я создал кластер на 3-х серверах, расположенных в одном датацентре.

### Настройка Kafka

На каждом сервере поднимал и инстанс контроллера и инстанс брокера. Брокер запускал командой

```bash
export KAFKA_HEAP_OPTS="-Xmx240G -Xms32G"; /path/to/kafka-server-start.sh /path/to/broker-kraft.properties
```

Где kafka-server-start.sh - это утилита из пакета Kafka, а broker-kraft.properties - это настройки ждя каждой ноды, приведенные [здесь](test-configurations/kafka-broker.properties).
Обратите внимание, что для запуска вам нужно поменять все `changeme` на реальные значения, актуальные для вас.

### Настройка YDB

На каждом сервере поднимал 1 storage ноду и 2 динамических ноды. Поднимал с помощью утилиты [ydbd_slice](https://github.com/ydb-platform/ydb/tree/main/ydb/tools/ydbd_slice), передавая ей конфиг
[ydb-config.yaml](test-configurations/ydb-config.yaml).
Обратите внимание, что для запуска вам нужно поменять все `changeme` на реальные значения, актуальные для вас.

### Создание топиков

В Kafka:

```shell
BOOTSTRAP_SERVER=changeme
TOPIC=test-topic
NUM_PARTITIONS=changeme

kafka-topics --create \
  --bootstrap-server ${BOOTSTRAP_SERVER} \
  --topic ${TOPIC} \
  --config min.insync.replicas=2 \
  --replication-factor 3 \
  --partitions ${NUM_PARTITIONS}
```

В YDB:

```shell
CONNECTION_PARAMS="-e grpc://changeme:2135 -d /changeme"
TEST_TOPIC_NAME="test-topic"
PARTITIONS_CNT=100

ya ydb ${CONNECTION_PARAMS} workload topic init \
  --topic ${TEST_TOPIC_NAME} \
  -p ${PARTITIONS_CNT} \
  -c 0

ya ydb ${CONNECTION_PARAMS} topic alter \
    --partition-write-speed-kbps 50000 \
    ${TEST_TOPIC_NAME}
```

Выше используется утилита [ya](https://github.com/ydb-platform/ydb/blob/main/ya). Как вариант можно сразу использовать [CLI YDB](https://ydb.tech/docs/ru/reference/ydb-cli/).

### Запуск тестов

Для тестов я использовал Gatling. Приложение из этого репозитория упаковывал с помощью `./gradlew shadowJar` 
и раскладывал по серверам генераторам нагрузки (отдельным от серверов с кластерами Kafka/YDB, но в том же ДЦ).

Тесты запускал командой ниже

```shell
java \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  -Xmx64G \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=30 \
  -XX:+ParallelRefProcEnabled \
  -jar highload2025-1.0-all.jar tech.ydb.IncreaseTo240MbSimulation changeme:9092
```

Для тестов с увеличвающейся нагрузкой запускал параллельно с 8 хостов с помощью внутрияндексового тула `pssh` 
(есть одноименные аналоги в open source, но я их не пробовал)

# Железо, на котором проводились тесты

Тесты и YDB и Kafka проводились на 3 серверах с конфигурацией:
- 56 ядер
- 256 Гб оперативной памяти 
- 3 NVME диска со скоростью чтения/записи 3,5/3,1 ГБ/с
- 10 Гбит/с сеть
