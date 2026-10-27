package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectorBalanceSnapshotRepository extends JpaRepository<ConnectorBalanceSnapshot, Long> {

    /**
     * Queries the latest Balance Snapshot for an account.
     */
    Optional<ConnectorBalanceSnapshot> findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(String accountNo);

    /**
     * Queries the earliest Balance Snapshot for an account.
     *
     * Baseline for initial investment calculations.
     * Examples:
     * - Rejoining paper trading
     * - Changing the account number
     * - Starting a new Paper account
     *
     * Instead of hard-coding KRW 10,000,000,
     * use the first connector_balance_snapshot for the account as the baseline amount.
     */
    Optional<ConnectorBalanceSnapshot> findTopByAccountNoOrderByAsOfDateAscAsOfTsAsc(String accountNo);
}