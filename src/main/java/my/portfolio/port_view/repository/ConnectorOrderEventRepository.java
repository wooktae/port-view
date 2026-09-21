package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorOrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectorOrderEventRepository extends JpaRepository<ConnectorOrderEvent, Long> {

    /**
     * Order Details timeline.
     *
     * Queries Events by the request IDs included in the chain.
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
     * Uses broker_order_no to include Events with a missing order_request_id link.
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