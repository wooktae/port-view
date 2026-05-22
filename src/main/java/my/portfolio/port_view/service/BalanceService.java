package my.portfolio.port_view.service;

import my.portfolio.port_view.entity.BalanceSummary;
import my.portfolio.port_view.entity.ConnectorPositionSnapshot;
import my.portfolio.port_view.repository.BalanceSummaryRepository;
import my.portfolio.port_view.repository.ConnectorPositionSnapshotRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import my.portfolio.port_view.entity.ConnectorBalanceSnapshot;
import my.portfolio.port_view.repository.ConnectorBalanceSnapshotRepository;
import java.math.RoundingMode;

@Service
public class BalanceService {

    private final BalanceSummaryRepository balanceSummaryRepository;
    private final ConnectorPositionSnapshotRepository connectorPositionSnapshotRepository;
    private final ConnectorBalanceSnapshotRepository connectorBalanceSnapshotRepository;

    public BalanceService(
        BalanceSummaryRepository balanceSummaryRepository,
        ConnectorPositionSnapshotRepository connectorPositionSnapshotRepository,
        ConnectorBalanceSnapshotRepository connectorBalanceSnapshotRepository
    ) {
        this.balanceSummaryRepository = balanceSummaryRepository;
        this.connectorPositionSnapshotRepository = connectorPositionSnapshotRepository;
        this.connectorBalanceSnapshotRepository = connectorBalanceSnapshotRepository;
    }

    public List<BalanceSummary> listByAccount(String accountNo) {
        return balanceSummaryRepository
                .findTopByAccountNoOrderByAsOfDateDescIdDesc(accountNo)
                .map(List::of)
                .orElseGet(List::of);
    }

    public BigDecimal getLatestStockEvalAmount(String accountNo) {
        return connectorPositionSnapshotRepository.findLatestPositionsByAccountNo(accountNo)
                .stream()
                .map(ConnectorPositionSnapshot::getEvalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getLatestHoldingCount(String accountNo) {
        return connectorPositionSnapshotRepository.findLatestPositionsByAccountNo(accountNo)
                .size();
    }

    public BigDecimal getInitialCapitalAmount(String accountNo) {
        return connectorBalanceSnapshotRepository
                .findTopByAccountNoOrderByAsOfDateAscAsOfTsAsc(accountNo)
                .map(ConnectorBalanceSnapshot::getTotalEvalAmount)
                .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getCumulativeProfitAmount(String accountNo) {
        BigDecimal latestTotalEvalAmount = balanceSummaryRepository
                .findTopByAccountNoOrderByAsOfDateDescIdDesc(accountNo)
                .map(BalanceSummary::getTotalEvalAmount)
                .orElse(BigDecimal.ZERO);

        BigDecimal initialCapitalAmount = getInitialCapitalAmount(accountNo);

        if (initialCapitalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return latestTotalEvalAmount.subtract(initialCapitalAmount);
    }

    public BigDecimal getCumulativeProfitRate(String accountNo) {
        BigDecimal initialCapitalAmount = getInitialCapitalAmount(accountNo);

        if (initialCapitalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal cumulativeProfitAmount = getCumulativeProfitAmount(accountNo);

        return cumulativeProfitAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(initialCapitalAmount, 4, RoundingMode.HALF_UP);
    }

}