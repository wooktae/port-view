package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorFill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectorFillRepository extends JpaRepository<ConnectorFill, Long> {

    /**
     * 주문 chain에 포함된 request id 기준 체결내역 조회.
     */
    @Query(
            value = """
                    SELECT *
                    FROM connector_fill
                    WHERE order_request_id IN (:orderRequestIds)
                    ORDER BY fill_ts ASC, id ASC
                    """,
            nativeQuery = true
    )
    List<ConnectorFill> findFillsByOrderRequestIds(
            @Param("orderRequestIds") List<Long> orderRequestIds
    );

    /**
     * fallback:
     * order_request_id가 없는 fill 보완용.
     */
    @Query(
            value = """
                    SELECT *
                    FROM connector_fill
                    WHERE broker_order_no IN (:brokerOrderNos)
                    ORDER BY fill_ts ASC, id ASC
                    """,
            nativeQuery = true
    )
    List<ConnectorFill> findFillsByBrokerOrderNos(
            @Param("brokerOrderNos") List<String> brokerOrderNos
    );
}