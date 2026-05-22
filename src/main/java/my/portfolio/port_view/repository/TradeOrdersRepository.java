package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.TradeOrders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeOrdersRepository extends JpaRepository<TradeOrders, Long> {

    // 계좌별 주문/체결 내역 조회 - 최신 주문시간 기준 정렬
    List<TradeOrders> findByAccountNoOrderByOrderTimeDescIdDesc(String accountNo);

    // 계좌별 주문/체결 내역 조회 - 기존 호환용
    List<TradeOrders> findByAccountNo(String accountNo);

    // 특정 주문번호 조회
    List<TradeOrders> findByOrderNo(String orderNo);
}