package com.lgali.request.response;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.lgali.common.aspect.LogResponseTime;
import com.lgali.common.message.RequestEvent;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResponseMessageListener {

    @RabbitListener(queues = "${spring.rabbitmq.event.queue.response}", containerFactory = "listenerFactory")
    @LogResponseTime(treatmentName = "RABBIT MQ : Consume response message ")
    public void processResponse(final RequestEvent responseMessage) {
        log.info("Message consumed {} ", responseMessage);
    }
}
