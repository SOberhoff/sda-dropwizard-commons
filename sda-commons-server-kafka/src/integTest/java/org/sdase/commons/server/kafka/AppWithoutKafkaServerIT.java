package org.sdase.commons.server.kafka;

import org.sdase.commons.server.kafka.builder.MessageHandlerRegistration;
import org.sdase.commons.server.kafka.builder.ProducerRegistration;
import org.sdase.commons.server.kafka.consumer.IgnoreAndProceedErrorHandler;
import org.sdase.commons.server.kafka.dropwizard.AppConfiguration;
import org.sdase.commons.server.kafka.dropwizard.KafkaApplication;
import org.sdase.commons.server.kafka.exception.ConfigurationException;
import org.sdase.commons.server.testing.EnvironmentRule;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

public class AppWithoutKafkaServerIT {


   public static final DropwizardAppRule<AppConfiguration> DROPWIZARD_APP_RULE = new DropwizardAppRule<>(
         KafkaApplication.class, ResourceHelpers.resourceFilePath("test-config-con-prod.yml"));

   public static final EnvironmentRule rule = new EnvironmentRule().setEnv("BROKER_CONNECTION_STRING", "[  \"localhost:12345\" ]");

   @ClassRule
   public static final TestRule CHAIN = RuleChain.outerRule(rule).around(DROPWIZARD_APP_RULE);


   private List<String> results = Collections.synchronizedList(new ArrayList<>());

   private KafkaBundle<AppConfiguration> bundle;

   @Before
   public void before() {
      KafkaApplication app = DROPWIZARD_APP_RULE.getApplication();
      bundle = app.getKafkaBundle();
      results.clear();
   }

   @Test(expected = TimeoutException.class)
   public void checkMessageListenerCreationThrowsException() throws ConfigurationException {
      String topicName = "checkMessageListenerCreationSuccessful";
      bundle
            .registerMessageHandler(MessageHandlerRegistration
                  .<String, String>builder()
                  .withListenerConfig("lc1")
                  .forTopic(topicName)
                  .checkTopicConfiguration()
                  .withDefaultConsumer()
                  .withValueDeserializer(new StringDeserializer())
                  .withHandler(record -> results.add(record.value()))
                  .withErrorHandler(new IgnoreAndProceedErrorHandler<>())
                  .build());

   }

   @Test(expected = TimeoutException.class)
   public void checkProducerWithCreationThrowsException() throws ConfigurationException {
      String topicName = "checkProducerWithCreationThrowsException";
      bundle.registerProducer(ProducerRegistration.builder().forTopic(topicName).createTopicIfMissing().withDefaultProducer().build());
   }

   @Test(expected = TimeoutException.class)
   public void checkProducerWithCheckThrowsException() throws ConfigurationException {
      String topicName = "checkProducerWithCreationThrowsException";
      bundle.registerProducer(ProducerRegistration.builder().forTopic(topicName).checkTopicConfiguration().withDefaultProducer().build());
   }

}
