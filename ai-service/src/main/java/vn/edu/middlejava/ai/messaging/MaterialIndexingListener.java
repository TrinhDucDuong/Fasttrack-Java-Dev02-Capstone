package vn.edu.middlejava.ai.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.edu.middlejava.ai.config.RabbitConfiguration;
import vn.edu.middlejava.ai.ingestion.IMaterialIndexer;

@Component
public class MaterialIndexingListener {
    private final IMaterialIndexer materialIndexer;

    public MaterialIndexingListener(IMaterialIndexer materialIndexer) {
        this.materialIndexer = materialIndexer;
    }

    @RabbitListener(queues = RabbitConfiguration.MATERIAL_INDEXING_QUEUE)
    public void handle(MaterialIndexingRequest request) {
        materialIndexer.index(request.materialId());
    }
}
