package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorPositionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectorPositionSnapshotRepository extends JpaRepository<ConnectorPositionSnapshot, Long> {

    /**
     * 계좌별 최신 잔고 기준의 현재 보유 종목 전체 조회.
     *
     * 중요:
     * - connector_position_snapshot의 MAX(as_of_date)를 최신 보유 기준으로 쓰면 안 됨.
     * - 현재 보유종목이 0건이면 connector_position_snapshot에는 최신 날짜 row가 없을 수 있음.
     * - 따라서 최신 connector_balance_snapshot의 as_of_date와 같은 날짜의 position row만 현재 보유로 인정.
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
     * 계좌 + 종목코드 기준 최신 잔고 기준의 현재 보유 종목 1건 조회.
     *
     * /positions/{tickerCode} 상세 화면에서 사용.
     * 최신 balance 기준일에 해당 종목이 없으면 현재 미보유로 간주.
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
     * 계좌별 전체 보유 스냅샷 이력 조회.
     * 이력 화면용이므로 과거 row를 그대로 보여줘도 됨.
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