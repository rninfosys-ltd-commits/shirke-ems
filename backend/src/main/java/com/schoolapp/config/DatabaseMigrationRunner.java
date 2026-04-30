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
        try {
            System.out.println("Running Database Migrations...");
            
            // Alter casting_hall_report size column to VARCHAR
            jdbcTemplate.execute("ALTER TABLE casting_hall_report ALTER COLUMN size TYPE VARCHAR(255) USING size::VARCHAR;");
            System.out.println("Updated casting_hall_report size column to VARCHAR.");
            
        } catch (Exception e) {
            System.err.println("Migration failed or already applied for casting_hall_report: " + e.getMessage());
        }

        try {
            // Alter wire_cutting_report size column to VARCHAR
            jdbcTemplate.execute("ALTER TABLE wire_cutting_report ALTER COLUMN size TYPE VARCHAR(255) USING size::VARCHAR;");
            System.out.println("Updated wire_cutting_report size column to VARCHAR.");
        } catch (Exception e) {
            System.err.println("Migration failed or already applied for wire_cutting_report: " + e.getMessage());
        }
    }
}
