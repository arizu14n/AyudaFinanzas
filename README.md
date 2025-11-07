# AyudaFinanzas App

Esta Aplicación es un proyecto de prueba para la materia Programación 3, en el marco de la Tecnicatura en Desarrollo de Software cursada en el Instituto Superior Villa del Rosario.

---

## Funcionalidades Principales

- **Autenticación de Usuarios:**
  - Registro e inicio de sesión utilizando una API propia desarrollada en Node.js y desplegada en Render.
  - Gestión de sesión mediante JSON Web Tokens (JWT).

- **Gestión de Cheques Propios (CRUD completo):**
  - **Registrar (POST):** Permite cargar los datos de un cheque y subir una imagen del mismo a Cloudinary. **Integra la IA de Google Gemini** para extraer automáticamente los datos del cheque a partir de la imagen.
  - **Consultar (GET):** Muestra una lista de todos los cheques registrados por el usuario, con un botón flotante (+) para un acceso rápido al registro.
  - **Actualizar (PUT):** Permite modificar el estado de un cheque (En cartera, Depositado, Rechazado, etc.).
  - **Eliminar (DELETE):** Permite borrar un cheque de la cartera del usuario.

- **APIs Externas Integradas:**
  - **Google Gemini API:** Se utiliza para el análisis de imágenes de cheques, extrayendo de forma automática el número, importe, fecha de pago y librador.
  - **APIs del Banco Central (BCRA):**
    - Estadísticas Cambiarias: Consulta de cotizaciones de divisas por rango de fechas.
    - Central de Deudores: Consulta de la situación crediticia de un CUIT/CUIL.
    - Cheques Denunciados: Verificación de si un cheque ha sido denunciado.
  - **API del Banco Nación (BNA):**
    - Consulta de cotizaciones históricas del dólar y el euro (formato billete) mediante web scraping.
  - **API de Dólar Blue:**
    - Consulta de las cotizaciones del dólar blue y dólares financieros (CCL y MEP).

## Tecnologías Utilizadas

- **Lenguaje:** 100% Kotlin.
- **Arquitectura:** Cliente-Servidor.
- **Inteligencia Artificial:** Google Gemini API para el reconocimiento de texto en imágenes.
- **Networking:** Retrofit para el consumo de APIs REST.
- **Web Scraping:** Jsoup para la extracción de datos de la web del BNA.
- **Carga de Imágenes:** Glide.
- **Diseño:** Material Design 3, iconos vectoriales y layouts responsivos.

---

## Demostración

Haz clic en la imagen para ver un video de la aplicación en funcionamiento:

[![Demostración de AyudaFinanzas](./assets/ayudafinanzas.png)](https://youtu.be/CTOU1Ys8JMs)

MEJORAS:

https://youtube.com/shorts/jSKULX6Tx1I?si=webUloSaHY3K3twP

Si prefieres una presentación: --> https://gamma.app/docs/Ayuda-Finanzas-i6ylzy5vsbs7ftz



Muchas gracias por su atención...!!!
