package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.NotificationGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationGateway {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

//    @Override
//    public Mono<Void> sendStatusUpdateNotification(Solicitud solicitud) {
//        try {
//            String message = objectMapper.writeValueAsString(solicitud);
//            log.info("Sending status update notification for Solicitud ID: {}", solicitud.getId());
//            return send(message)
//                    .then(); // Convert Mono<String> to Mono<Void>
//        } catch (JsonProcessingException e) {
//            return Mono.error(new RuntimeException("Error serializing Solicitud to JSON", e));
//        }
//    }

    @Override
    public Mono<Void> sendSQSStatusRechazado(Integer loanId) {
        try {
            String message = objectMapper.writeValueAsString(loanId);
            log.info("Sending sendSQSStatusChange for Solicitud: {}", loanId);
            return send(message, properties.queueUrlC())
                    .then(); // Convert Mono<String> to Mono<Void>
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing Solicitud id to JSON", e));
        }
    }

    @Override
    public Mono<Void> sendSQSStatusChange(Solicitud solicitud) {
        try {
            String message = objectMapper.writeValueAsString(solicitud);
            log.info("Sending status update notification for Solicitud: {}", solicitud);
            return send(message, properties.queueUrlA())
                    .then(); // Convert Mono<String> to Mono<Void>
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing Solicitud to JSON", e));
        }
    }
    @Override
    public Mono<Void> sendSQSCapacity(Map<String, Object> message) {
        try {
            String msg = objectMapper.writeValueAsString(message);
            log.info("Sending status update notification for sendSQSCapacity: {}", msg);
            return send(msg, properties.queueUrlB())
                    .then(); // Convert Mono<String> to Mono<Void>
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing Solicitud to JSON", e));
        }
    }
    private Mono<String> send(String message, String queueUrl) {
        return Mono.fromCallable(() -> buildRequest(message, queueUrl))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message, String queueUrl) {
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
    }
}
