package com.schoolapp.config;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class WireCuttingSchemaSync {

    private static final Logger log = LoggerFactory.getLogger(WireCuttingSchemaSync.class);

    private static final Set<String> MAPPED_COLUMNS = Set.of(
            "id",
            "batch_no",
            "cutting_date",
            "mould_no",
            "size",
            "cutting_length",
            "ball_test_mm",
            "qty100",
            "quantity_total100",
            "breakage",
            "breakage100",
            "net_qty100",
            "qty150",
            "quantity_total150",
            "breakage150",
            "net_qty150",
            "total_item",
            "remark",
            "time",
            "cycle_time",
            "shift",
            "plant_name",
            "cutting_hours",
            "cutting_temp_c",
            "cutting_time",
            "rising_id",
            "approval_stage",
            "approved_by_l1",
            "approved_by_l2",
            "approved_by_l3",
            "rejection_reason",
            "user_id",
            "branch_id",
            "org_id",
            "created_date",
            "updated_by",
            "updated_date",
            "is_active");

    private final JdbcTemplate jdbcTemplate;

    public WireCuttingSchemaSync(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void relaxLegacyColumns() {
        try {
            List<String> sql = jdbcTemplate.queryForList(
                    """
                    SELECT CONCAT(
                        'ALTER TABLE `wire_cutting_report` MODIFY COLUMN `',
                        COLUMN_NAME,
                        '` ',
                        COLUMN_TYPE,
                        ' NULL DEFAULT NULL'
                    ) AS alter_sql
                    FROM information_schema.columns
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'wire_cutting_report'
                      AND IS_NULLABLE = 'NO'
                    """,
                    String.class);

            int altered = 0;
            for (String alterSql : sql) {
                String columnName = extractColumnName(alterSql);
                if (columnName == null || MAPPED_COLUMNS.contains(columnName.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                jdbcTemplate.execute(alterSql);
                altered++;
                log.warn("Relaxed legacy wire_cutting_report column {}", columnName);
            }

            if (altered > 0) {
                log.info("Wire cutting schema sync relaxed {} legacy column(s)", altered);
            }
        } catch (Exception e) {
            log.warn("Wire cutting schema sync skipped: {}", e.getMessage());
        }
    }

    private String extractColumnName(String alterSql) {
        if (alterSql == null) {
            return null;
        }
        String marker = "MODIFY COLUMN `";
        int start = alterSql.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = alterSql.indexOf('`', start);
        if (end < 0) {
            return null;
        }
        return alterSql.substring(start, end);
    }
}
