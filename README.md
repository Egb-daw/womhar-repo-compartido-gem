# WoMHAT

Aplicación web para la gestión visual y la documentación técnica de infraestructuras de CPD.

WoMHAT nace para resolver un problema muy habitual en entornos de sistemas: la documentación de racks, equipos y cambios de infraestructura suele acabar dispersa entre capturas, diagramas manuales, hojas de cálculo y descripciones largas. El objetivo del proyecto es centralizar esa información en una aplicación web clara, mantenible y pensada para el trabajo diario de administración y soporte.

---

## Índice

1. [Objetivo del proyecto](#1-objetivo-del-proyecto)
2. [Qué problema resuelve](#2-qué-problema-resuelve)
3. [Alcance funcional de WoMHAT](#3-alcance-funcional-de-womhat)
4. [Estado actual del repositorio](#4-estado-actual-del-repositorio)
5. [Stack tecnológico](#5-stack-tecnológico)
6. [Arquitectura y organización del backend](#6-arquitectura-y-organización-del-backend)
7. [Modelo de dominio](#7-modelo-de-dominio)
8. [Seguridad](#8-seguridad)
9. [Internacionalización, UI y experiencia de usuario](#9-internacionalización-ui-y-experiencia-de-usuario)
10. [Estructura del proyecto](#10-estructura-del-proyecto)
11. [Requisitos previos](#11-requisitos-previos)
12. [Configuración del entorno](#12-configuración-del-entorno)
13. [Puesta en marcha local](#13-puesta-en-marcha-local)
14. [Gestión del Frontend (Vite + SCSS)](#14-gestión-del-frontend-vite--scss)
15. [Base de datos y datos de ejemplo](#15-base-de-datos-y-datos-de-ejemplo)
16. [Flujo de ramas y trabajo en equipo](#16-flujo-de-ramas-y-trabajo-en-equipo)
17. [Buenas prácticas aplicadas](#17-buenas-prácticas-aplicadas)
18. [Roadmap](#18-roadmap)
19. [Autores](#19-autores)

---

## 1. Objetivo del proyecto

El objetivo de WoMHAT es ofrecer una herramienta web que permita:

- documentar la estructura física de un CPD;
- gestionar racks, equipos, elementos de red y almacenamiento;
- registrar información técnica útil para mantenimiento y soporte;
- mejorar la trazabilidad de cambios sobre la infraestructura;
- facilitar la comprensión visual del entorno a técnicos, responsables y usuarios con distintos niveles de acceso.

WoMHAT no pretende ser solo un inventario. La idea es combinar estructura visual, datos técnicos, seguridad, roles, histórico y mantenibilidad del código en un único proyecto.

---

## 2. Qué problema resuelve

En muchos entornos, la documentación del hardware se gestiona de esta manera:

- planos hechos de forma manual;
- descripciones largas difíciles de actualizar;
- inventarios desconectados de la realidad física del rack;
- poca trazabilidad de quién cambió qué;
- dificultad para consultar permisos, mantenimiento o características técnicas.

WoMHAT propone una solución orientada a centralizar esa información y presentarla de manera más clara, visual y reutilizable.

---

## 3. Alcance funcional de WoMHAT

### Gestión de infraestructura

- CPDs y salas.
- Racks con ubicación, capacidad, grupo y estado.
- Equipos con tipo, serie, IP principal, IP de gestión, VLAN, MAC y ocupación en U.
- Especializaciones técnicas para host, red y almacenamiento.

### Gestión de usuarios

- Usuarios con perfil.
- Roles.
- Control de acceso a racks.
- Bloqueo, activación y ciclo de contraseña.

### Operaciones de mantenimiento

- Órdenes de trabajo.
- Notas de mantenimiento.
- Histórico técnico de equipos.

### Soporte funcional de aplicación

- Login y seguridad.
- OAuth con GitHub.
- Recuperación y reseteo de contraseña por correo.
- Internacionalización.
- Vistas Thymeleaf para panel y administración.

---

## 4. Estado actual del repositorio

Este README está pensado para reflejar el proyecto real y su dirección final, pero separando claramente lo que ya existe de lo que forma parte de la evolución prevista.

### Ya presente en el proyecto actual

- Backend Spring Boot.
- Persistencia con Spring Data JPA y MariaDB.
- Seguridad con Spring Security.
- Login clásico + OAuth GitHub.
- Flujo de reseteo de contraseña por email.
- Internacionalización con ficheros `messages`.
- Esquema SQL y datos de ejemplo.
- Recursos estáticos y vistas Thymeleaf.
- Docker Compose para la base de datos.

### Evolución prevista / línea de crecimiento del proyecto

- API REST completa y securizada.
- Swagger / OpenAPI.
- Gestión de imágenes por endpoints REST.
- Mayor número de tests.
- Cierre definitivo de las vistas del dominio técnico completo.

---

## 5. Stack tecnológico

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Mail
- OAuth2 Client
- Lombok

### Base de datos

- MariaDB
- Inicialización mediante `schema.sql` y `data.sql`

### Frontend / presentación

- **Thymeleaf**: Motor de plantillas para el renderizado dinámico desde el servidor.
- **Vite**: Herramienta de construcción (*build tool*) para una gestión moderna de activos.
- **SASS (SCSS)**: Preprocesador CSS para estilos y mantenibles.
- **JavaScript**: Lógica de cliente moderna.
- **Bootstrap 5**: Base de diseño responsive y componentes.

Carpeta `frontend/` reservada para trabajar con `scss`, `js` y recursos estáticos de forma más organizada a medida que el proyecto evoluciona. Sus plantillas, a través de `npm run build`, se integran en el proyecto principal en un CSS y JS unificados para su uso en las vistas Thymeleaf.

### DevOps / entorno

- Docker Compose para la BD
- Maven Wrapper (`mvnw`) para compilación y ejecución
- Git + GitHub para control de versiones

---

## 6. Arquitectura y organización del backend

WoMHAT sigue una arquitectura por capas, buscando claridad y separación de responsabilidades:

### `controllers/`

Responsables de gestionar la entrada web, la navegación y el enlace con la capa de servicios.

### `services/`

Contienen la lógica de negocio y reglas del dominio.

### `repositories/`

Acceso a datos mediante Spring Data JPA.

### `dtos/`

Objetos de transferencia entre capas y formularios.

### `mappers/`

Conversión entre entidades y DTOs.

### `entities/`

Modelo de persistencia del proyecto.

### `config/`

Configuración: locale, seguridad, etc.

### `exceptions/`

Excepciones específicas del dominio.

### `handlers/`

Manejo de flujos especiales, como OAuth.

Esta organización favorece:

- código más mantenible;
- mayor facilidad para testear;
- crecimiento progresivo hacia API REST y documentación OpenAPI.

---

## 7. Modelo de dominio

El proyecto trabaja sobre un dominio técnico orientado a CPD e inventario.

### Infraestructura

- `DataCenter`
- `DataCenterRoom`
- `Rack`
- `Equipment`
- `HostSpecification`
- `NetworkElement`
- `StorageBackup`

### Seguridad y usuarios

- `User`
- `UserProfile`
- `Role`
- `UserRackAccess`
- `PasswordResetToken`

### Mantenimiento

- `EquipmentEventLog`
- `MaintenanceWorkOrder`
- `MaintenanceNote`

La base de datos está pensada para cubrir tanto la estructura física como la administración funcional del sistema.

---

## 8. Seguridad

WoMHAT incorpora una base de seguridad sólida:

- autenticación con Spring Security;
- almacenamiento de contraseñas como `password_hash`;
- roles de usuario;
- control de acceso por usuario y rack;
- bloqueo y activación de cuentas;
- recuperación de contraseña mediante token;
- integración OAuth con GitHub;
- configuración preparada para crecimiento hacia JWT/API REST.

> **Importante:** No se deben subir al repositorio credenciales reales, secretos OAuth, contraseñas SMTP ni rutas privadas de entorno.

---

## 9. Internacionalización, UI y experiencia de usuario

### Internacionalización

WoMHAT usa ficheros `messages.properties`, `messages_es.properties` y `messages_en.properties` para soportar varios idiomas y preparar la interfaz para un uso más amplio.

### Identidad visual

Decisiones ya tomadas para el proyecto:

- **Raleway** para encabezados.
- **Roboto** para cuerpo de texto.
- **Source Code Pro** para datos técnicos.
- Iconografía clara y profesional para racks, equipos, estados, acciones, etc.
- Paleta de colores profesional y sobria, con tonos neutros para el fondo y acentos de color para destacar información técnica y estados.
- Diseño responsive para asegurar usabilidad en distintos dispositivos.
- Estructura visual clara para diferenciar secciones, jerarquías de información y facilitar la navegación.
- Uso de tablas, tarjetas y paneles para organizar la información técnica de manera legible y accesible.
- Enfoque en la experiencia de usuario desde el diseño de la interfaz, buscando que sea intuitiva, clara y eficiente para los técnicos que la usarán a diario.

### Usabilidad

La interfaz está pensada para priorizar:

- lectura clara;
- estructura jerárquica;
- navegación lógica;
- presentación visual de la información técnica;
- una experiencia de usuario que facilite el trabajo diario de administración y soporte sobre la infraestructura documentada.

---

## 10. Estructura del proyecto

```plaintext
womhat/
├── .env
├── .gitattributes
├── .gitignore
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── org/iesalixar/daw2/womhat/womhat/
        │       ├── config/
        │       ├── controllers/
        │       ├── dtos/
        │       ├── entities/
        │       ├── enums/
        │       ├── exceptions/
        │       ├── handlers/
        │       ├── mappers/
        │       ├── repositories/
        │       └── services/
        └── resources/
            ├── application.properties
            ├── schema.sql
            ├── data.sql
            ├── messages*.properties
            ├── frontend/                     # zona reservada para evolución del frontend con Vite/SCSS
            ├── static/
            └── templates/
```

---

## 11. Requisitos previos

Antes de levantar el proyecto necesitas:

- Java 21
- Maven (o usar `./mvnw` / `mvnw.cmd`)
- Docker y Docker Compose
- MariaDB (se levanta con Docker Compose, pero puedes usar una instalación local si prefieres)
- Node.js y npm (para la compilación del frontend)
- IntelliJ IDEA o similar

Opcionalmente:

- cuenta de GitHub OAuth para login;
- cuenta SMTP de pruebas para el flujo de correo.

---

## 12. Configuración del entorno

El proyecto usa:

```properties
spring.config.import=optional:file:.env[.properties]
```

Por lo que la configuración puede centralizarse en un fichero `.env`.

---

## 13. Puesta en marcha local

### Levantar MariaDB con Docker Compose

```bash
docker compose up -d
```

### Ejecutar la aplicación Spring Boot

```bash
./mvnw spring-boot:run
```

O en Windows:

```bash
mvnw.cmd spring-boot:run
```

### Aplicación levantada

Una vez levantada, la aplicación estará disponible en `http://localhost:8080` (o el puerto configurado en `.env`).

---

## 14. Gestión del Frontend (Vite + SCSS)

WoMHAT utiliza un flujo de trabajo moderno para el frontend desacoplado del ciclo de vida estándar de Maven, aunque integrado en el resultado final.

### Estructura de la carpeta `frontend/`

- `src/`: contiene los archivos HTML de desarrollo, archivos `.scss` y archivos `.js`.
- `public/`: recursos estáticos que se copian directamente sin procesar (imágenes, favicons).
- `vite.config.js`: configuración personalizada para que el build deposite los archivos procesados directamente en las carpetas de Spring Boot.

### Comandos del Frontend

Si necesitas modificar estilos, scripts o HTML, debes trabajar dentro de la carpeta `frontend/`:

```bash
cd frontend
npm install
npm run dev
npm run build
```

> **Nota técnica:** El comando `npm run build` está configurado para no borrar los assets previos (`emptyOutDir: false`) y distribuir automáticamente los archivos `.css` y `.js` en `src/main/resources/static` y los `.html` procesados en `src/main/resources/templates`.

---

## 15. Base de datos y datos de ejemplo

El proyecto está preparado para inicializarse con:

- `schema.sql` -> estructura de tablas;
- `data.sql` -> datos para pruebas y demostraciones.

### Qué cubre el esquema actual

- CPDs
- salas
- racks
- equipos
- especialización técnica de hosts, red y almacenamiento
- usuarios, roles y perfiles
- permisos por rack
- tokens de reset
- órdenes de mantenimiento
- notas e histórico de eventos

---

## 16. Flujo de ramas y trabajo en equipo

En WoMHAT se sigue un flujo de integración claro:

- `dgc` -> rama de trabajo individual de David
- `egb` / `EGB` -> rama de trabajo individual de Eloy
- `develop` -> rama de integración del equipo
- `main` -> rama estable / entregable

### Flujo recomendado a seguir

```bash
# 1. Actualiza develop
git checkout develop
git pull origin develop

# 2. Cambia a tu rama de trabajo
git checkout dgc
# o
git checkout egb

# 3. Traer los cambios de develop a tu rama para evitar conflictos grandes
git merge develop

# 3.5 Asegúrate de compilar el frontend antes de subir si has tocado estilos/scripts
cd frontend
npm run build
cd ..

# 4. Sube tu rama
git add .
git commit -m "Descripción clara de los cambios"
git push origin dgc
# o
git push origin egb

# 5. Integra tu rama a develop cuando esté lista y validada
git checkout develop
git pull origin develop
git merge dgc
# o
git merge egb
git push origin develop

# 6. Integra develop a main solo cuando esté estable y listo para entrega
git checkout main
git pull origin main
git merge develop
git push origin main
```

### Criterios del flujo

- Integrar en `develop` solo cambios validados y que no rompan la funcionalidad.
- Hacer merges regulares desde `develop` a tu rama para evitar conflictos grandes.
- Mantener `main` como rama para despliegues o entregas, integrando solo desde `develop` cuando esté estable.
- Usar mensajes de commit claros y descriptivos para facilitar la revisión y el seguimiento de cambios.

---

## 17. Buenas prácticas aplicadas

El proyecto sigue el siguiente conjunto de buenas prácticas para asegurar su calidad, mantenibilidad y profesionalidad:

- estructura de paquetes clara y coherente;
- nombres de clases, métodos y variables claros y descriptivos;
- documentación técnica con JavaDoc y loggers;
- separación de responsabilidades entre controladores, servicios, repositorios, DTOs y mappers;
- uso de Spring Security para una base sólida de seguridad;
- uso de Thymeleaf para una presentación clara y mantenible;
- uso de ficheros de mensajes para internacionalización desde el inicio;
- esquema de base de datos bien diseñado para cubrir el dominio técnico y funcional del proyecto;
- datos de ejemplo para facilitar pruebas y demostraciones;
- gestión de configuración mediante variables de entorno y `.env`;
- uso de Docker Compose para facilitar el entorno de desarrollo;
- integración de Vite y flujos de trabajo de frontend modernos;
- estructura de proyecto organizada para facilitar la navegación y comprensión del código;
- inicialización de la base de datos con scripts SQL para asegurar un entorno reproducible;
- código preparado para evolución futura hacia API REST y documentación OpenAPI;
- enfoque en la experiencia de usuario desde el diseño de la interfaz y la usabilidad;
- gestión de seguridad integral desde el diseño de la autenticación, autorización y recuperación de contraseña;
- uso de Git con ramas claras para trabajo en equipo;
- documentación actualizada en el README para reflejar el estado real del proyecto y su dirección futura;
- enfoque profesional orientado a la mantenibilidad, claridad y usabilidad del proyecto como herramienta real para la gestión de infraestructura de CPD;
- y, por supuesto, el código se mantiene limpio, legible y bien estructurado para facilitar su comprensión y mantenimiento a largo plazo.

---

## 18. Roadmap

### Próximos objetivos

- cubrir el dominio técnico (CPDs, salas, racks, equipos, ...) completo con vistas Thymeleaf y capas (controladores, servicios, repositorios, DTOs, mappers);
- documentar el código con JavaDoc y con loggers;
- internacionalizar completamente la interfaz;
- reforzar tests unitarios;
- cubrir aspectos visuales, usabilidad y organización de la información para mejorar la experiencia de usuario (navegación, estructura de vistas, presentación de datos técnicos, etc.);
- mejorar README y documentación de uso.

### Objetivos finales

Convertir WoMHAT en una aplicación web completa y profesional para la gestión de infraestructura de CPD, con:

- panel web MVC usable;
- backend y frontend robustos;
- seguridad coherente;
- documentación técnica clara;
- y una base sólida para seguir evolucionando hacia una solución más completa en el futuro.

---

## 19. Autores

- **David González Córdoba**
- **Eloy González Bautista**

---

## Resumen rápido

WoMHAT es una aplicación web para documentar y gestionar infraestructura de CPD con foco en:

- racks y equipos,
- información técnica,
- seguridad y usuarios,
- mantenimiento,
- organización visual,
- y crecimiento progresivo hacia una solución más completa.

Este README representa la evolución del proyecto y debe ser actualizado para reflejar el estado real y la dirección futura del proyecto.
