package fr.formationacademy.scpiinvestpluspartner.service;

import fr.formationacademy.scpiinvestpluspartner.dto.ScpiRequestDto;
import fr.formationacademy.scpiinvestpluspartner.entity.Investment;
import fr.formationacademy.scpiinvestpluspartner.mapper.InvestmentMapper;
import fr.formationacademy.scpiinvestpluspartner.repository.InvestmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static fr.formationacademy.scpiinvestpluspartner.enums.InvestmentState.PROCESSING;

@Service
@Slf4j
public class ProcessInvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentMapper investmentMapper;

    public ProcessInvestmentService(InvestmentRepository investmentRepository, InvestmentMapper investmentMapper) {
        this.investmentRepository = investmentRepository;
        this.investmentMapper = investmentMapper;
    }


    public void processInvestment(ScpiRequestDto dto) {

        dto.setInvestmentState(PROCESSING);
        saveInvestment(dto);
        log.info("l'investissement numero {} est en cours de traitement", dto.getInvestmentId());

    }

    public ScpiRequestDto saveInvestment(ScpiRequestDto data) {

        Investment investment = investmentMapper.toEntity(data);
        data.setInvestmentState(PROCESSING);
        Investment savedInvestment = investmentRepository.save(investment);
        return investmentMapper.toDto(savedInvestment);
    }


}