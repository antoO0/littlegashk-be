package io.littlegashk.webapp;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class DynamoDbSchemaInitializer implements ApplicationListener<ContextRefreshedEvent> {

    public static final String TABLE_LITTLEGAS = "littlegas";
    @Autowired
    AmazonDynamoDB db;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        log.info("Running dynamo init....");
        ListTablesResult listTablesResult = db.listTables();
        if (!listTablesResult.getTableNames().contains(TABLE_LITTLEGAS)) {
            log.info("Table not found, creating....");
            GlobalSecondaryIndex globalSecondaryIndex = new GlobalSecondaryIndex().withIndexName("meta-index")
                                                                                  .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                                       .withAttributeName("sid"),
                                                                                                 new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                                       .withAttributeName("group"))
                                                                                  .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(
                                                                                          5L).withWriteCapacityUnits(3L))
                                                                                  .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(TABLE_LITTLEGAS)
                                                                            .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                                 .withAttributeName("pid"),
                                                                                           new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                                 .withAttributeName("sid"))
                                                                            .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(
                                                                                    15L).withWriteCapacityUnits(10L))
                                                                            .withGlobalSecondaryIndexes(globalSecondaryIndex)
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("pid").withAttributeType(ScalarAttributeType.S),
                                              new AttributeDefinition().withAttributeName("sid").withAttributeType(ScalarAttributeType.S),
                                              new AttributeDefinition().withAttributeName("group").withAttributeType(ScalarAttributeType.S));
            db.createTable(createTableRequest);
            log.info("Table creation done");
        }
    }

}