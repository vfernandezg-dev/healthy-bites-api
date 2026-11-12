# ADR 0008 · Ficheros en un almacenamiento de objetos S3-compatible

- **Estado:** aceptada
- **Fecha:** 2026-11-12

## Contexto

Las subidas (fotos de publicaciones, portadas y ficheros de contenido, hasta 100 MB) se guardaban en una carpeta
del servidor y la API servía cada lectura. Eso tiene tres problemas en el hosting previsto, contenedores con varias
instancias:

- el disco de un contenedor es efímero: un reinicio o un despliegue pierde los ficheros;
- con varias instancias, un fichero subido a una no existe en las demás;
- cada vídeo o PDF ocupa un hilo y ancho de banda de la API mientras se descarga.

## Decisión

- `FileStorage` es un puerto de `media.app` con dos adaptadores en `media.infra`, elegidos con `STORAGE_TYPE`:
  - `local`: la carpeta de siempre, para desarrollo o una sola instancia con disco persistente;
  - `s3`: `S3FileStorage` con AWS SDK v2, que sirve para Cloudflare R2, AWS S3 o MinIO (`S3_ENDPOINT`, `S3_BUCKET`,
    `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_REGION`, `S3_PATH_STYLE`).
- Con `s3`, `GET /media/{id}` comprueba el acceso como antes (público, o URL firmada de la API) y responde **302 a
  una URL prefirmada** del almacenamiento, firmada con el tipo, la disposición y la caché que antes ponía la API:
  - ficheros públicos: la URL vale 1 h y el navegador reutiliza la redirección 50 min;
  - ficheros protegidos: la URL vale 5 min y la redirección no se guarda.
- Un fallo del almacenamiento responde 503 `storage-unavailable`. Con `s3`, la aplicación no arranca si falta
  alguna variable.
- Los tests usan Adobe S3Mock en Testcontainers (las imágenes de MinIO ya no se publican sin credenciales).

## Consecuencias

- Los ficheros sobreviven a reinicios y los comparten todas las instancias.
- Los vídeos (peticiones `Range`) y los PDF los sirve el almacenamiento, no la API.
- El contenido subido por usuarios se sirve desde otro origen, así que nada de lo que contenga puede actuar sobre el
  origen de la API. Las cabeceras `nosniff` y `sandbox` solo se aplican cuando sirve la API (modo `local`).
- Un fichero público cuya redirección caduca se vuelve a descargar con otra URL prefirmada: la caché del navegador
  dura lo que la redirección, no un año.
- La configuración del bucket (privado, sin listado público) es parte del despliegue.

## Alternativas descartadas

- **Seguir en disco con un volumen compartido.** No existe en el hosting previsto y ata el despliegue a un sistema
  de ficheros.
- **Que la API haga de proxy del almacenamiento.** Mantiene el coste de ancho de banda y complica las peticiones
  `Range` de los vídeos.
- **Bucket público para las imágenes.** Más sencillo, pero obliga a otro dominio y a gestionar dos rutas de
  lectura; se puede añadir más adelante sin cambiar la API.
