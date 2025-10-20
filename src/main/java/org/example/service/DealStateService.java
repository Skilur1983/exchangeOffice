package org.example.service;

import org.example.model.dto.deal.CancelDealDto;
import org.example.model.dto.deal.DealReadDto;
import org.example.model.dto.deal.ResumeDealDto;

import java.util.List;

public interface DealStateService {

    DealReadDto resumeDeal(Integer dealId, ResumeDealDto dto);
    DealReadDto cancelDeal(Integer dealId, CancelDealDto dto);
    DealReadDto completeDeal(Integer dealId);

    List<DealReadDto> getAllPausedDeals();
    List<DealReadDto> getAllPausedDealsByUser(Integer userId);
}
