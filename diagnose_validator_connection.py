from execution_db import get_conn

with get_conn() as conn:
    with conn.cursor() as cur:
        cur.execute("""
            SELECT
                current_database(),
                current_user,
                current_schema(),
                current_setting('search_path')
        """)
        row = cur.fetchone()

        print("=== PYTHON CONNECTION ===")
        print(f"database       : {row[0]}")
        print(f"user           : {row[1]}")
        print(f"current_schema : {row[2]}")
        print(f"search_path    : {row[3]}")

        print()
        print("=== SAME-NAME TABLES ===")
        cur.execute("""
            SELECT
                table_schema,
                table_name
            FROM information_schema.tables
            WHERE table_name IN (
                'strategy_execution_plan',
                'strategy_execution_order',
                'connector_order_request',
                'connector_fill',
                'strategy_position_state'
            )
            ORDER BY table_name, table_schema
        """)

        for schema_name, table_name in cur.fetchall():
            print(f"{schema_name}.{table_name}")

        print()
        print("=== RESOLVED ORDER TABLE COUNTS ===")
        cur.execute("""
            SELECT
                table_schema
            FROM information_schema.columns
            WHERE table_name = 'strategy_execution_order'
              AND table_schema NOT IN ('pg_catalog', 'information_schema')
            GROUP BY table_schema
            ORDER BY
                CASE
                    WHEN table_schema = current_schema() THEN 0
                    WHEN table_schema = 'public' THEN 1
                    ELSE 2
                END,
                table_schema
        """)

        schemas = [r[0] for r in cur.fetchall()]

        for schema_name in schemas:
            cur.execute(
                f'''
                SELECT count(*)
                FROM "{schema_name}"."strategy_execution_order"
                WHERE execution_mode = %s
                  AND action_type IN ('BUY', 'SELL')
                  AND signal_date = %s
                ''',
                ("PAPER_STRATEGY", "2026-07-20"),
            )
            count = cur.fetchone()[0]
            print(f"{schema_name}.strategy_execution_order : {count}")