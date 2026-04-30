package com.schoolapp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running Database Migrations (MySQL)...");

        // 1. Fix old columns causing "Field doesn't have a default value" errors
        try {
            jdbcTemplate.execute("ALTER TABLE casting_hall_report ALTER COLUMN flow SET DEFAULT 0");
            System.out.println("Set default for legacy 'flow' column.");
        } catch (Exception e) {
            System.err.println("Note: 'flow' column fix skipped: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE casting_hall_report ALTER COLUMN casting_temp SET DEFAULT 0");
            System.out.println("Set default for legacy 'casting_temp' column.");
        } catch (Exception e) {
            System.err.println("Note: 'casting_temp' column fix skipped: " + e.getMessage());
        }

        // 2. Change size columns to VARCHAR
        try {
            jdbcTemplate.execute("ALTER TABLE casting_hall_report MODIFY COLUMN size VARCHAR(255)");
            System.out.println("Updated casting_hall_report size column to VARCHAR.");
        } catch (Exception e) {
            System.err.println("Migration failed or already applied for casting_hall_report size: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE wire_cutting_report MODIFY COLUMN size VARCHAR(255)");
            System.out.println("Updated wire_cutting_report size column to VARCHAR.");
        } catch (Exception e) {
            System.err.println("Migration failed or already applied for wire_cutting_report size: " + e.getMessage());
        }
    }
}
