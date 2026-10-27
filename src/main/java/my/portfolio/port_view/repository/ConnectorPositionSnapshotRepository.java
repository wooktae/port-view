package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorPositionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectorPositionSnapshotRepository extends JpaRepository<ConnectorPositionSnapshot, Long> {

    /**
     * Queries all current Positions for an account using the latest Balance date.
     *
     * Important:
     * - Do not use MAX(as_of_date) from connector_position_snapshot as the latest Position baseline.
     * - When there are no current Positions, connector_position_snapshot may have no row for the latest date.
     * - Therefore, only Position rows with the same as_of_date as the latest connector_balance_snapshot are current.
     */
    @Query(
            value = """
                    WITH latest_balance AS (
                        SELECT
                            account_id,
                            account_no,
                            as_of_date,
                            as_of_ts
                        FROM connector_balance_snapshot
                        WHERE account_no = :accountNo
                        ORDER BY as_of_date DESC, as_of_ts DESC, id DESC
                        LIMIT 1
                    )
                    SELECT cps.*
                    FROM latest_balance lb
                    JOIN connector_position_snapshot cps
                      ON cps.account_id = lb.account_id
                     AND cps.as_of_date = lb.as_of_date
                    WHERE cps.account_no = :accountNo
                      AND COALESCE(cps.quantity, 0) > 0
                    ORDER BY cps.eval_amount DESC NULLS LAST, cps.ticker_code ASC
                    """,
            nativeQuery = true
    )
    List<ConnectorPositionSnapshot> findLatestPositionsByAccountNo(@Param("accountNo") String accountNo);

    /**
     * Queries one current Position for an account and Ticker using the latest Balance date.
     *
     * Used by the /positions/{tickerCode} Details view.
     * The Position is not currently held when the Ticker is absent on the latest Balance date.
     */
    @Query(
            value = """
                    WITH latest_balance AS (
                        SELECT
                            account_id,
                            account_no,
                            as_of_date,
                            as_of_ts
                        FROM connector_balance_snapshot
                        WHERE account_no = :accountNo
                        ORDER BY as_of_date DESC, as_of_ts DESC, id DESC
                        LIMIT 1
                    )
                    SELECT cps.*
                    FROM latest_balance lb
                    JOIN connector_position_snapshot cps
                      ON cps.account_id = lb.account_id
                     AND cps.as_of_date = lb.as_of_date
                    WHERE cps.account_no = :accountNo
                      AND cps.ticker_code = :tickerCode
                      AND COALESCE(cps.quantity, 0) > 0
                    ORDER BY cps.as_of_ts DESC NULLS LAST, cps.id DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<ConnectorPositionSnapshot> findLatestPositionByAccountNoAndTickerCode(
            @Param("accountNo") String accountNo,
            @Param("tickerCode") String tickerCode
    );

    /**
     * Queries the complete Position Snapshot history for an account.
     * Historical rows may be displayed directly because this is a history view.
     */
    @Query(
            value = """
                    SELECT *
                    FROM connector_position_snapshot
                    WHERE account_no = :accountNo
                    ORDER BY as_of_date DESC, eval_amount DESC NULLS LAST, ticker_code ASC
                    """,
            nativeQuery = true
    )
    List<ConnectorPositionSnapshot> findPositionHistoryByAccountNo(@Param("accountNo") String accountNo);
}