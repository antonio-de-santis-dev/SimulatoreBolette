-- V4 — Collega le offerte al gestore (ParametriGestore)  [prompt 1]
--
-- Aggiunge la colonna FK parametri_gestore_id a offerte (nullable durante la
-- transizione) e collega le offerte esistenti al profilo predefinito.
-- SQL volutamente "piatto" (niente blocchi PL/pgSQL) per restare compatibile
-- sia con PostgreSQL (produzione) sia con H2 in modalita' PostgreSQL (test).

ALTER TABLE offerte ADD COLUMN parametri_gestore_id BIGINT;

ALTER TABLE offerte
    ADD CONSTRAINT fk_offerte_parametri_gestore
    FOREIGN KEY (parametri_gestore_id) REFERENCES parametri_gestore (id);

-- Collega le offerte ancora orfane al profilo predefinito (se presente).
-- La colonna resta NULLABLE: nessun NOT NULL, per tollerare offerte orfane.
UPDATE offerte
SET parametri_gestore_id = (
    SELECT id FROM parametri_gestore WHERE predefinito = TRUE LIMIT 1
)
WHERE parametri_gestore_id IS NULL;
