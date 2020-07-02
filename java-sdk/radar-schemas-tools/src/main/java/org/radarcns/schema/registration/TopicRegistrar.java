package org.radarcns.schema.registration;

import java.io.Closeable;
import java.util.Set;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;

import org.radarcns.schema.specification.SourceCatalogue;

public interface TopicRegistrar extends Closeable {

    int createTopics(@NotNull SourceCatalogue catalogue, int partitions, short replication,
            String topic, String match);

    boolean createTopics(Stream<String> topics, int partitions, short replication);

    void ensureInitialized();

    boolean refreshTopics() throws InterruptedException;

    Set<String> getTopics();
}
