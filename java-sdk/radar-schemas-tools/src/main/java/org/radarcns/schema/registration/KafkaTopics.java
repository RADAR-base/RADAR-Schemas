package org.radarcns.schema.registration;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.radarcns.schema.specification.SourceCatalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Properties;

public class KafkaTopics implements Closeable {
    private final ZkUtils zkUtils;
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopics.class);

    public KafkaTopics(String zookeeper) {
        ZkClient zkClient = new ZkClient(zookeeper, 15_000, 10_000);

        zkClient.setZkSerializer(new ZkSerializer() {
            @Override
            public byte[] serialize(Object o) throws ZkMarshallingError {
                return ZKStringSerializer.serialize(o);
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return ZKStringSerializer.deserialize(bytes);
            }
        });

        zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeper), false);
    }

    public boolean createTopics(SourceCatalogue catalogue, int partitions, int replication) {
        return catalogue.getTopicNames()
                .allMatch(topic -> createTopic(topic, partitions, replication));
    }

    public boolean createTopic(String topic, int partitions, int replication) {
        Properties props = new Properties();
        try {
            if (!AdminUtils.topicExists(zkUtils, topic)) {
                logger.info("Creating topic {}", topic);
                AdminUtils.createTopic(zkUtils, topic, partitions, replication, props,
                        RackAwareMode.Enforced$.MODULE$);
            } else {
                logger.info("Topic {} already exists", topic);
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to create topic {}", topic, ex);
            return false;
        }
    }

    @Override
    public void close() {
        zkUtils.close();
    }
}
