package com.lgali.request.deposit;

import java.io.IOException;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lgali.common.aspect.LogResponseTime;
import com.lgali.common.dao.entity.DepositRequest;
import com.lgali.common.dao.repository.DepositRequestRepository;
import com.lgali.common.dto.DepositRequestDTO;
import com.lgali.common.exception.GlobalException;
import com.lgali.common.mapper.DepositRequestMapper;
import com.lgali.common.message.RequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositRequestService {

    private final RabbitTemplate           lgaliRabbitTemplate;
    private final DepositRequestRepository depositRequestRepository;
    private final DepositRequestMapper     depositRequestMapper;

    @Value("${spring.rabbitmq.event.exchange.request}")
    private String exchange;

    @Value("${spring.rabbitmq.event.queue.request}")
    private String queue;

    @Value("${spring.rabbitmq.event.routing.key.request}")
    private String routingKey;

    @LogResponseTime(treatmentName = "PROCESS REQUEST")
    public DepositRequestDTO processRequest(final DepositRequestDTO depositRequestDTO)
      throws GlobalException, IOException {
        try {
            DepositRequest depositRequest = depositRequestMapper.mapFromDTO(depositRequestDTO);
            depositRequest.setContentImage(depositRequestDTO.getContentImage());
            DepositRequest savedDepositRequest = depositRequestRepository.save(depositRequest);
            final RequestEvent requestEvent = RequestEvent.builder()
                                                          .requestID(savedDepositRequest.getId())
                                                          .latitude(savedDepositRequest.getLatitude())
                                                          .longitude(savedDepositRequest.getLongitude())
                                                          .build();
            messageSend(requestEvent);
            final DepositRequestDTO savedDepositRequestDTO = depositRequestMapper.mapFromEntity(savedDepositRequest);
            log.info("Saved deposit request DTO {} ", savedDepositRequestDTO);
            return savedDepositRequestDTO;
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }

    @LogResponseTime(treatmentName = "GET ALL REQUESTS BY USER ID")
    public List<DepositRequestDTO> getAllRequests(final String userID) throws GlobalException {
        try {
            final List<DepositRequest> depositRequestList = depositRequestRepository.findByUserID(userID);
            return depositRequestMapper.mapFromListEntity(depositRequestList);
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }

    public void updateStatusToFailed(final Long depositRequestId) {
        depositRequestRepository.updateStatusToFailedById(depositRequestId);
    }

    public void deleteByID(final Long depositRequestID) throws GlobalException {
        try {
            depositRequestRepository.deleteById(depositRequestID);
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }

    private void messageSend(RequestEvent requestEvent) {
        lgaliRabbitTemplate.convertAndSend(exchange, routingKey, requestEvent);
    }

}
