-- Update size columns from INTEGER to VARCHAR
ALTER TABLE casting_hall_report ALTER COLUMN size TYPE VARCHAR(255) USING size::VARCHAR;
ALTER TABLE wire_cutting_report ALTER COLUMN size TYPE VARCHAR(255) USING size::VARCHAR;

-- Add new columns to wire_cutting_report (Hibernate should do this automatically with ddl-auto=update, but here they are just in case)
ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS qty100 INTEGER;
ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS quantity_total100 DOUBLE PRECISION;
ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS breakage100 INTEGER;
ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS net_qty100 DOUBLE PRECISION;

ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS qty150 INTEGER;
ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS quantity_total150 DOUBLE PRECISION;
ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS breakage150 INTEGER;
ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS net_qty150 DOUBLE PRECISION;

ALTER TABLE wire_cutting_report ADD COLUMN IF NOT EXISTS total_item DOUBLE PRECISION;
