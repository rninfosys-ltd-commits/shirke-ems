package com.schoolapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AutoclaveSchemaSync {

    private static final Logger log = LoggerFactory.getLogger(AutoclaveSchemaSync.class);

    private final JdbcTemplate jdbcTemplate;

    public AutoclaveSchemaSync(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sync() {
        try {
            dropColumnIfExists("autoclave_cycle", "cycle_start_time");
            dropColumnIfExists("autoclave_cycle", "hold_start_time");
            dropColumnIfExists("autoclave_cycle", "hold_end_time");
            dropColumnIfExists("autoclave_cycle", "cycle_end_time");
            addTransferredColumnIfMissing();
        } catch (Exception e) {
            log.warn("Autoclave schema sync skipped: {}", e.getMessage());
        }
    }

    private void dropColumnIfExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = ?
                          AND column_name = ?
                        """,
                Integer.class,
                table,
                column);
        if (count != null && count > 0) {
            jdbcTemplate.execute("ALTER TABLE `" + table + "` DROP COLUMN `" + column + "`");
            log.warn("Dropped legacy autoclave column {}", column);
        }
    }

    private void addTransferredColumnIfMissing() {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'autoclave_cycle'
                          AND column_name = 'transferred_to_autoclave_no'
                        """,
                Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.execute(
                    "ALTER TABLE `autoclave_cycle` ADD COLUMN `transferred_to_autoclave_no` INT NULL");
            log.warn("Added autoclave transferred_to_autoclave_no column");
        }
    }
}
