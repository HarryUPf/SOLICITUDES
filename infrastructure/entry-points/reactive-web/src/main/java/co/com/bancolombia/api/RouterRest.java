package co.com.bancolombia.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitudes"), handler::createSolicitud)
                .andRoute(GET("/api/v1/solicitudes"), handler::findSolicitudes)
                .andRoute(POST("/api/v1/solicitudes/search"), handler::searchSolicitudes)
//                .andRoute(GET("/api/v1/solicitudes/{id}"), handler::getSolicitudById)
                ;
    }
}
