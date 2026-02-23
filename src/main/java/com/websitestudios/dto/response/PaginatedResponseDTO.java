package com.websitestudios.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * Converts Spring's Page<T> into a clean JSON structure:
 * {
 * "content": [...],
 * "pageNumber": 0,
 * "pageSize": 20,
 * "totalElements": 150,
 * "totalPages": 8,
 * "first": true,
 * "last": false
 * }
 */
public class PaginatedResponseDTO<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    // ──────────────────────────── Constructors ────────────────────────────

    public PaginatedResponseDTO() {
    }

    public PaginatedResponseDTO(List<T> content, int pageNumber, int pageSize,
            long totalElements, int totalPages,
            boolean first, boolean last) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    // ──────────────────────────── Factory Method ────────────────────────────

    /**
     * Build PaginatedResponseDTO from a Spring Data Page object.
     *
     * Usage:
     * Page<ProjectRequestResponseDTO> page = ...;
     * PaginatedResponseDTO<ProjectRequestResponseDTO> response =
     * PaginatedResponseDTO.from(page);
     */
    public static <T> PaginatedResponseDTO<T> from(Page<T> page) {
        return new PaginatedResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }

    // ──────────────────────────── Getters & Setters ────────────────────────────

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}