package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectorBalanceSnapshotRepository extends JpaRepository<ConnectorBalanceSnapshot, Long> {

    /**
     * 계좌별 최신 잔고 스냅샷 1건 조회.
     */
    Optional<ConnectorBalanceSnapshot> findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(String accountNo);

    /**
     * 계좌별 최초 잔고 스냅샷 1건 조회.
     *
     * 초기 투자금 계산 기준.
     * 예:
     * - 모의투자 재참가
     * - 계좌번호 변경
     * - 새 paper 계좌 시작
     *
     * 코드에 10,000,000원을 박지 않고,
     * 해당 계좌의 첫 connector_balance_snapshot을 기준금액으로 사용.
     */
    Optional<ConnectorBalanceSnapshot> findTopByAccountNoOrderByAsOfDateAscAsOfTsAsc(String accountNo);
}