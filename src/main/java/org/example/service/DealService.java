package org.example.service;

import org.example.model.Deal;
import org.example.model.DealStatus;
import org.example.model.dto.PageDto;
import org.example.model.dto.deal.DealCreateDto;
import org.example.model.dto.deal.DealReadDto;
import org.springframework.data.domain.Pageable;

public interface DealService {

    DealReadDto getById(Integer id);
    Deal getEntityById(Integer id);
    PageDto<DealReadDto> getAll(Pageable pageable);
    PageDto<DealReadDto> getAllByUserId(Integer userId, Pageable pageable);
    PageDto<DealReadDto> getAllByStatus(DealStatus status, Pageable pageable);
    DealReadDto createDeal(DealCreateDto dto);
    void deleteById(Integer id);
}
