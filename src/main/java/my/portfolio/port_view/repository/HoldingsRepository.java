package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.Holdings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HoldingsRepository extends JpaRepository<Holdings, Long> {

    // 계좌별 전체 보유종목 이력
    List<Holdings> findByAccountNoOrderByAsOfDateDescStockCodeAsc(String accountNo);

    // 계좌별 최신 기준일 조회용
    Optional<Holdings> findTopByAccountNoOrderByAsOfDateDescIdDesc(String accountNo);

    // 계좌 + 기준일 기준 보유종목
    List<Holdings> findByAccountNoAndAsOfDateOrderByStockCodeAsc(String accountNo, LocalDate asOfDate);
}