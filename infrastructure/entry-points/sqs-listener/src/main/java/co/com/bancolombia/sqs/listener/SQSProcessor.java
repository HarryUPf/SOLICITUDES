package co.com.bancolombia.sqs.listener;

import co.com.bancolombia.sqs.listener.dto.UpdateEstadoMessageDTO;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.IOException;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final SolicitudUseCase solicitudUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> apply(Message message) {
        return Mono.fromCallable(() -> {
                    log.info(">>> Received SQS message: {}", message.messageId());
                    return objectMapper.readValue(message.body(), UpdateEstadoMessageDTO.class);
                })
                .flatMap(dto -> {
                    log.info("--- Processing update for solicitud ID: {}, new estado: {}", dto.getId(), dto.getEstado());
                    return solicitudUseCase.updateEstadoSolicitud(dto.getId(), dto.getEstado(), true);
                })
                .then() // Convert Mono<Object> from use case to Mono<Void>
                .doOnSuccess(v -> log.info("<<< SQS message processed successfully: {}", message.messageId()))
                .onErrorResume(IOException.class, e -> {
                    log.error("!!! Failed to parse SQS message body. MessageId: {}", message.messageId(), e);
                    return Mono.empty(); // Acknowledge message to prevent reprocessing of malformed JSON
                })
                .onErrorResume(e -> {
                    log.error("!!! Error processing SQS message. MessageId: {}", message.messageId(), e);
                    return Mono.error(e); // Propagate error to signal failure to SQS
                });
    }
}
