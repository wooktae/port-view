package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorFill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectorFillRepository extends JpaRepository<ConnectorFill, Long> {

    /**
     * Queries Fill History by the request IDs included in an Order Chain.
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
     * Supplements Fills that have no order_request_id.
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