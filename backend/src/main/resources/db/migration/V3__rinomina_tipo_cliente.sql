-- ════════════════════════════════════════════════════════════════════════════
-- Migrazione dati: i valori dell'enum TipoCliente sono stati rinominati nel round 2
-- (conformita ARERA). Le righe gia' presenti nel DB usano ancora i nomi vecchi e
-- vanno aggiornate, altrimenti JPA fallisce la conversione (HTTP 500 in lettura).
--
--   RESIDENTE            -> DOMESTICO_RESIDENTE
--   NON_RESIDENTE        -> DOMESTICO_NON_RESIDENTE
--   ATTIVITA_PRODUTTIVE  -> ALTRI_USI_BT
--   PMI_RESIDENZIALE     -> DOMESTICO_NON_RESIDENTE
--   DOMESTICO_USI_DIVERSI (invariato)
-- ════════════════════════════════════════════════════════════════════════════

UPDATE bollette_concorrenti SET tipologia_cliente = 'DOMESTICO_RESIDENTE'     WHERE tipologia_cliente = 'RESIDENTE';
UPDATE bollette_concorrenti SET tipologia_cliente = 'DOMESTICO_NON_RESIDENTE' WHERE tipologia_cliente = 'NON_RESIDENTE';
UPDATE bollette_concorrenti SET tipologia_cliente = 'ALTRI_USI_BT'            WHERE tipologia_cliente = 'ATTIVITA_PRODUTTIVE';
UPDATE bollette_concorrenti SET tipologia_cliente = 'DOMESTICO_NON_RESIDENTE' WHERE tipologia_cliente = 'PMI_RESIDENZIALE';

UPDATE simulazioni SET tipo_cliente = 'DOMESTICO_RESIDENTE'     WHERE tipo_cliente = 'RESIDENTE';
UPDATE simulazioni SET tipo_cliente = 'DOMESTICO_NON_RESIDENTE' WHERE tipo_cliente = 'NON_RESIDENTE';
UPDATE simulazioni SET tipo_cliente = 'ALTRI_USI_BT'            WHERE tipo_cliente = 'ATTIVITA_PRODUTTIVE';
UPDATE simulazioni SET tipo_cliente = 'DOMESTICO_NON_RESIDENTE' WHERE tipo_cliente = 'PMI_RESIDENZIALE';
