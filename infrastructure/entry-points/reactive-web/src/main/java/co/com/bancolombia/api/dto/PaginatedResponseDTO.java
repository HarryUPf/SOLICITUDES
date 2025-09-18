package co.com.bancolombia.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponseDTO<T> {
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
    private Double totalMonto;
    private List<T> content;
}