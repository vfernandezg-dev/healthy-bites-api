-- Búsquedas "contiene" de los listados (lower(columna) like '%texto%', ver TextSearch) con índices de trigramas: sin
-- ellos, cada búsqueda recorre la tabla entera. Los índices solo se usan con el mismo lower(...) de la consulta.
create extension if not exists pg_trgm;

create index ix_community_groups_name_trgm on community_groups using gin (lower(name) gin_trgm_ops);
create index ix_content_items_title_trgm on content_items using gin (lower(title) gin_trgm_ops);
create index ix_content_items_summary_trgm on content_items using gin (lower(summary) gin_trgm_ops);
create index ix_users_email_trgm on users using gin (lower(email) gin_trgm_ops);
