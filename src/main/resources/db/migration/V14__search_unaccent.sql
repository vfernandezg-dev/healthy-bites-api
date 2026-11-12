-- Búsquedas sin distinguir tildes (por ejemplo, "jose" encuentra "José"): GET /nutritionist/clients?q=.
create extension if not exists unaccent;
