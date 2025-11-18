package com.jeinsdean.aiven.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 래퍼
 * Spring Data JPA의 Page를 클라이언트 친화적 형태로 변환
 *
 * 모바일 앱에서 무한 스크롤 구현 시 필요한 메타데이터 제공
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final PageMetadata metadata;

    private PageResponse(List<T> content, PageMetadata metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    /**
     * Spring Data Page를 PageResponse로 변환
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                PageMetadata.of(page)
        );
    }

    /**
     * 페이징 메타데이터
     */
    @Getter
    public static class PageMetadata {
        private final int page;           // 현재 페이지 (0부터 시작)
        private final int size;           // 페이지 크기
        private final long totalElements; // 전체 요소 수
        private final int totalPages;     // 전체 페이지 수
        private final boolean first;      // 첫 페이지 여부
        private final boolean last;       // 마지막 페이지 여부
        private final boolean hasNext;    // 다음 페이지 존재 여부
        private final boolean hasPrevious;// 이전 페이지 존재 여부

        private PageMetadata(Page<?> page) {
            this.page = page.getNumber();
            this.size = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.first = page.isFirst();
            this.last = page.isLast();
            this.hasNext = page.hasNext();
            this.hasPrevious = page.hasPrevious();
        }

        public static PageMetadata of(Page<?> page) {
            return new PageMetadata(page);
        }
    }
}