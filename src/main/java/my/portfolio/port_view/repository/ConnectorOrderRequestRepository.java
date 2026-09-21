package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorOrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectorOrderRequestRepository extends JpaRepository<ConnectorOrderRequest, Long> {

    /**
     * Recent Order Request list for an account.
     *
     * Used by the /orders list view.
     */
    @Query(
            value = """
                    SELECT *
                    FROM connector_order_request
                    WHERE account_no = :accountNo
                    ORDER BY requested_at DESC, id DESC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<ConnectorOrderRequest> findRecentOrdersByAccountNo(
            @Param("accountNo") String accountNo,
            @Param("limit") int limit
    );

    /**
     * Today's Order Request list for an account.
     * Currently available for summary calculations in Step3.
     */
    @Query(
            value = """
                    SELECT *
                    FROM connector_order_request
                    WHERE account_no = :accountNo
                      AND DATE(requested_at AT TIME ZONE 'Asia/Seoul') = CURRENT_DATE
                    ORDER BY requested_at DESC, id DESC
                    """,
            nativeQuery = true
    )
    List<ConnectorOrderRequest> findTodayOrdersByAccountNo(@Param("accountNo") String accountNo);
    
    /**
     * Order Details view: queries one specific Order.
     */
    @Query(
            value = """
                    SELECT *
                    FROM connector_order_request
                    WHERE id = :id
                      AND account_no = :accountNo
                    """,
            nativeQuery = true
    )
    ConnectorOrderRequest findOneByIdAndAccountNo(
            @Param("id") Long id,
            @Param("accountNo") String accountNo
    );

    /**
     * Order Details view: queries a parent/child chain.
     *
     * Policy:
     * - Follow parent links from the selected Order until the root is found.
     * - Query the root and all its children.
     * - This is sufficient because the current data structure is a single BUY -> MODIFY -> CANCEL chain.
     */
    @Query(
            value = """
                    WITH RECURSIVE ancestors AS (
                        SELECT *
                        FROM connector_order_request
                        WHERE id = :orderRequestId
                          AND account_no = :accountNo

                        UNION ALL

                        SELECT parent.*
                        FROM connector_order_request parent
                        JOIN ancestors child
                          ON child.parent_order_request_id = parent.id
                        WHERE parent.account_no = :accountNo
                    ),
                    root_order AS (
                        SELECT *
                        FROM ancestors
                        WHERE parent_order_request_id IS NULL
                        ORDER BY id ASC
                        LIMIT 1
                    ),
                    descendants AS (
                        SELECT *
                        FROM root_order

                        UNION ALL

                        SELECT child.*
                        FROM connector_order_request child
                        JOIN descendants parent
                          ON child.parent_order_request_id = parent.id
                        WHERE child.account_no = :accountNo
                    )
                    SELECT *
                    FROM descendants
                    ORDER BY requested_at ASC, id ASC
                    """,
            nativeQuery = true
    )
    List<ConnectorOrderRequest> findOrderChain(
            @Param("orderRequestId") Long orderRequestId,
            @Param("accountNo") String accountNo
    );

    /**
     * Orders that require a latest Fill check when entering View Trading History.
     *
     * Targets:
     * - Orders in ACCEPTED / SUBMITTED / PENDING / PARTIAL_FILLED state
     * - Orders with a broker_order_no
     * - Orders within the most recent N days
     */
    @Query(
        value = """
                SELECT *
                FROM connector_order_request
                WHERE account_no = :accountNo
                  AND request_status IN ('ACCEPTED', 'SUBMITTED', 'PENDING', 'PARTIAL_FILLED')
                  AND broker_order_no IS NOT NULL
                  AND broker_order_no <> ''
                  AND requested_at >= (CURRENT_TIMESTAMP - (:lookbackDays * INTERVAL '1 day'))
                ORDER BY requested_at DESC, id DESC
                LIMIT :limit
                """,
            nativeQuery = true
    )
    List<ConnectorOrderRequest> findRefreshTargetOrders(
            @Param("accountNo") String accountNo,
            @Param("lookbackDays") int lookbackDays,
            @Param("limit") int limit
    );

}
