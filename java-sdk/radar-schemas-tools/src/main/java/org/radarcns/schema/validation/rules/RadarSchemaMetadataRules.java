package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;
import org.radarcns.schema.validation.ValidationSupport;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.nio.file.Path;

import static org.radarcns.schema.validation.rules.Validator.check;
import static org.radarcns.schema.validation.rules.Validator.raise;
import static org.radarcns.schema.validation.rules.Validator.valid;

/** Rules for schemas with metadata in RADAR-Schemas. */
public class RadarSchemaMetadataRules implements SchemaMetadataRules {
    private final SchemaRules schemaRules;
    private final Path root;
    private final ExcludeConfig config;

    /**
     * Rules for schemas with metadata in RADAR-Schemas. {@link RadarSchemaRules} is used as schema
     * rules implementation.
     * @param root directory of RADAR-Schemas
     * @param config configuration for excluding schemas from validation.
     */
    public RadarSchemaMetadataRules(Path root, ExcludeConfig config) {
        this(root, config, new RadarSchemaRules(config));
    }

    /**
     * Rules for schemas with metadata in RADAR-Schemas.
     * @param root directory of RADAR-Schemas
     * @param config configuration for excluding schemas from validation.
     * @param schemaRules schema rules implementation.
     */
    public RadarSchemaMetadataRules(Path root, ExcludeConfig config, SchemaRules schemaRules) {
        this.schemaRules = schemaRules;
        this.config = config;
        this.root = root;
    }

    @Override
    public SchemaRules getSchemaRules() {
        return schemaRules;
    }

    @Override
    public Validator<SchemaMetadata> validateSchemaLocation() {
        return validateNamespaceSchemaLocation()
                .and(validateNameSchemaLocation());
    }

    private Validator<SchemaMetadata> validateNamespaceSchemaLocation() {
        return metadata -> {
            try {
                String expected = ValidationSupport.getNamespace(
                        root, metadata.getPath(), metadata.getScope());
                String namespace = metadata.getSchema().getNamespace();

                return check(expected.equalsIgnoreCase(namespace), message(
                        "Namespace cannot be null and must fully lowercase dot"
                        + " separated without numeric. In this case the expected value is \""
                        + expected + "\".").apply(metadata));
            } catch (IllegalArgumentException ex) {
                return raise("Path " + metadata.getPath()
                        + " is not part of root " + root, ex);
            }
        };
    }

    private Validator<SchemaMetadata> validateNameSchemaLocation() {
        return metadata -> {
            String expected = ValidationSupport.getRecordName(metadata.getPath());

            return expected.equalsIgnoreCase(metadata.getSchema().getName()) ? valid() : raise(
                    message("Record name should match file name. Expected record name is \""
                            + expected + "\".").apply(metadata));
        };
    }

    @Override
    public Validator<SchemaMetadata> schema(Validator<Schema> validator) {
        return metadata -> config.skipFile(metadata.getPath()) ? valid()
                : validator.apply(metadata.getSchema());
    }
}
