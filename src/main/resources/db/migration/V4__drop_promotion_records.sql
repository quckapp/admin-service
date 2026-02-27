-- V4: Remove centralized promotion_records table
-- Promotion gate enforcement is now handled per-service via standalone promotion-gate libraries.
-- Each service manages its own promotion records in its own database.

DROP TABLE IF EXISTS promotion_records;
