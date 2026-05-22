package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.ConnectorOrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectorOrderRequestRepository extends JpaRepository<ConnectorOrderRequest, Long> {

    /**
     * 계좌별 최근 주문 요청 목록.
     *
     * /orders 리스트 화면에서 사용.
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
     * 계좌별 오늘 주문 요청 목록.
     * 지금 Step3에서는 요약 계산용으로 사용 가능.
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
     * 주문 상세 화면: 특정 주문 1건 조회.
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
     * 주문 상세 화면: parent/child chain 조회.
     *
     * 정책:
     * - 선택 주문에서 parent를 계속 따라 root를 찾음.
     * - root와 그 하위 children을 모두 조회.
     * - 현재 데이터 구조는 BUY -> MODIFY -> CANCEL 단일 chain이므로 이 방식이면 충분.
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
}