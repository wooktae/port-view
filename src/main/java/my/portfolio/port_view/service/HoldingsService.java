package my.portfolio.port_view.service;

import my.portfolio.port_view.entity.Holdings;
import my.portfolio.port_view.repository.HoldingsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HoldingsService {

    private final HoldingsRepository holdingsRepository;

    public HoldingsService(HoldingsRepository holdingsRepository) {
        this.holdingsRepository = holdingsRepository;
    }

    public List<Holdings> listByAccount(String accountNo) {
        LocalDate latestAsOfDate = holdingsRepository
                .findTopByAccountNoOrderByAsOfDateDescIdDesc(accountNo)
                .map(Holdings::getAsOfDate)
                .orElse(null);

        if (latestAsOfDate == null) {
            return List.of();
        }

        return holdingsRepository.findByAccountNoAndAsOfDateOrderByStockCodeAsc(
                accountNo,
                latestAsOfDate
        );
    }
}