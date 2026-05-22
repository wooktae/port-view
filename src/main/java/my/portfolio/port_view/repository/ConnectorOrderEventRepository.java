package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorOrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectorOrderEventRepository extends JpaRepository<ConnectorOrderEvent, Long> {

    /**
     * 주문 상세 timeline.
     *
     * chain에 포함된 request id 기준으로 event를 조회.
     */
    @Query(
            value = """
                    SELECT *
                    FROM connector_order_event
                    WHERE account_no = :accountNo
                      AND order_request_id IN (:orderRequestIds)
                    ORDER BY event_ts ASC, id ASC
                    """,
            nativeQuery = true
    )
    List<ConnectorOrderEvent> findEventsByOrderRequestIds(
            @Param("accountNo") String accountNo,
            @Param("orderRequestIds") List<Long> orderRequestIds
    );

    /**
     * fallback:
     * order_request_id 연결이 누락된 이벤트까지 보고 싶을 때 broker_order_no 기준 보완.
     */
    @Query(
            value = """
                    SELECT *
                    FROM connector_order_event
                    WHERE account_no = :accountNo
                      AND broker_order_no IN (:brokerOrderNos)
                    ORDER BY event_ts ASC, id ASC
                    """,
            nativeQuery = true
    )
    List<ConnectorOrderEvent> findEventsByBrokerOrderNos(
            @Param("accountNo") String accountNo,
            @Param("brokerOrderNos") List<String> brokerOrderNos
    );
}