package com.rewedigital.examples.msintegration.productinformation.scheduling;

import com.rewedigital.examples.msintegration.productinformation.product.ProductEvent;
import com.rewedigital.examples.msintegration.productinformation.product.ProductEventPublisher;
import com.rewedigital.examples.msintegration.productinformation.product.ProductEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ProductEventProcessingTask implements ApplicationListener<ProductEvent.Message> {

    private static final Logger LOG = LoggerFactory.getLogger(ProductEventProcessingTask.class);

    final private ProductEventPublisher eventPublisher;
    final private ProductEventRepository productEventRepository;

    @Inject
    public ProductEventProcessingTask(final ProductEventPublisher eventPublisher,
        final ProductEventRepository productEventRepository) {
        this.eventPublisher = eventPublisher;
        this.productEventRepository = productEventRepository;
    }

    @Override
    public void onApplicationEvent(final ProductEvent.Message event) {
        try {
            final ProductEvent productEvent = productEventRepository.getOne(event.id());
            publishEventAndDeleteFromDB(productEvent);
        } catch (final Exception ex) {
            LOG.error("error publishing event with id [%s] due to %s", event.id(), ex.getMessage(), ex);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void processNextMessageBatch() {
        LOG.debug("performing next message batch");

        publishEventAndDeleteFromDB(productEventRepository.findFirstByOrderByTimeAsc());
    }

    private void publishEventAndDeleteFromDB(final ProductEvent productEvent) {
        if (productEvent == null) {
            return;
        }

        eventPublisher.publish(productEvent)
            .addCallback(sendResult -> productEventRepository.delete(productEvent),
                ex -> logException(ex));
    }

    private void logException(final Throwable ex) {
        LOG.error("error publishing event", ex);
    }
}
