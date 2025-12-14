package org.example.service;

import org.example.model.Deal;
import org.example.model.dto.PageDto;
import org.example.model.dto.deal.CancelDealDto;
import org.example.model.dto.deal.DealCreateDto;
import org.example.model.dto.deal.DealReadDto;
import org.example.model.dto.deal.ResumeDealDto;

import java.util.Map;

public interface DealService {

    DealReadDto getById(Integer id);
    Deal getEntityById(Integer id);
    PageDto<DealReadDto> getAll(Map<String, String> params);
    DealReadDto createDeal(DealCreateDto dto);
    DealReadDto resumeDeal(Integer dealId, ResumeDealDto dto);
    DealReadDto cancelDeal(Integer dealId, CancelDealDto dto);
    void deleteById(Integer id);
}
