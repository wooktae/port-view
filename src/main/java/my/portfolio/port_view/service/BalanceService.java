package my.portfolio.port_view.service;

import my.portfolio.port_view.entity.BalanceSummary;
import my.portfolio.port_view.entity.ConnectorBalanceSnapshot;
import my.portfolio.port_view.entity.ConnectorPositionSnapshot;
import my.portfolio.port_view.repository.ConnectorBalanceSnapshotRepository;
import my.portfolio.port_view.repository.ConnectorPositionSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class BalanceService {

    private final ConnectorPositionSnapshotRepository connectorPositionSnapshotRepository;
    private final ConnectorBalanceSnapshotRepository connectorBalanceSnapshotRepository;

    public BalanceService(
            ConnectorPositionSnapshotRepository connectorPositionSnapshotRepository,
            ConnectorBalanceSnapshotRepository connectorBalanceSnapshotRepository
    ) {
        this.connectorPositionSnapshotRepository = connectorPositionSnapshotRepository;
        this.connectorBalanceSnapshotRepository = connectorBalanceSnapshotRepository;
    }

    @Transactional(readOnly = true)
    public List<BalanceSummary> listByAccount(String accountNo) {
        return connectorBalanceSnapshotRepository
                .findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(accountNo)
                .map(this::toBalanceSummary)
                .map(List::of)
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public BigDecimal getLatestStockEvalAmount(String accountNo) {
        return connectorPositionSnapshotRepository.findLatestPositionsByAccountNo(accountNo)
                .stream()
                .map(ConnectorPositionSnapshot::getEvalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public int getLatestHoldingCount(String accountNo) {
        return connectorPositionSnapshotRepository.findLatestPositionsByAccountNo(accountNo)
                .size();
    }

    @Transactional(readOnly = true)
    public BigDecimal getInitialCapitalAmount(String accountNo) {
        return connectorBalanceSnapshotRepository
                .findTopByAccountNoOrderByAsOfDateAscAsOfTsAsc(accountNo)
                .map(ConnectorBalanceSnapshot::getTotalEvalAmount)
                .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCumulativeProfitAmount(String accountNo) {
        BigDecimal latestTotalEvalAmount = connectorBalanceSnapshotRepository
                .findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(accountNo)
                .map(ConnectorBalanceSnapshot::getTotalEvalAmount)
                .orElse(BigDecimal.ZERO);

        BigDecimal initialCapitalAmount = getInitialCapitalAmount(accountNo);

        if (initialCapitalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return latestTotalEvalAmount.subtract(initialCapitalAmount);
    }

    @Transactional(readOnly = true)
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

    private BalanceSummary toBalanceSummary(ConnectorBalanceSnapshot snapshot) {
        BalanceSummary summary = new BalanceSummary();

        summary.setId(snapshot.getId());
        summary.setAccountNo(snapshot.getAccountNo());
        summary.setCashBalance(nvl(snapshot.getCashBalance()));
        summary.setNextdayExecAmt(nvl(snapshot.getNextdayExecAmt()));
        summary.setPrevClosingAmt(nvl(snapshot.getPrevClosingAmt()));
        summary.setTotalEvalAmount(nvl(snapshot.getTotalEvalAmount()));
        summary.setEvalProfit(nvl(snapshot.getEvalProfit()));
        summary.setBuyAmountToday(nvl(snapshot.getBuyAmountToday()));
        summary.setSellAmountToday(nvl(snapshot.getSellAmountToday()));
        summary.setFeeTotalToday(nvl(snapshot.getFeeTotalToday()));
        summary.setAsOfDate(snapshot.getAsOfDate());

        return summary;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}