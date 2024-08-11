# Backernd для мобильного приложения КАРДО

### Приложение разрабатывали:
Владимир Баханович ([vvbakhanovich](https://github.com/vvbakhanovich)), Руслан Якупов ([YakupovRR](https://github.com/YakupovRR)).

![Static Badge](https://img.shields.io/badge/Java-%23FF7800?style=plastic)
![Static Badge](https://img.shields.io/badge/Spring%20Boot-%236DB33F?style=plastic&logo=springboot&logoColor=black)
![Static Badge](https://img.shields.io/badge/Spring%20Security-%236DB33F?style=plastic&logo=springsecurity&logoColor=black)
![Static Badge](https://img.shields.io/badge/Spring%20Data%20JPA-%236DB33F?style=plastic&logo=spring&logoColor=black)
![Static Badge](https://img.shields.io/badge/PostgreSQL-%234169E1?style=plastic&logo=postgresql&logoColor=white)
![Static Badge](https://img.shields.io/badge/Liquibase-%232962FF?style=plastic&logo=liquibase&logoColor=white)
![Static Badge](https://img.shields.io/badge/Docker-%232496ED?style=plastic&logo=docker&logoColor=white)
![Static Badge](https://img.shields.io/badge/Test%20Containers-%232496ED?style=plastic&logo=docker&logoColor=white)
![Static Badge](https://img.shields.io/badge/Apache%20Maven-%23C71A36?style=plastic&logo=apachemaven)
![Static Badge](https://img.shields.io/badge/Git-%23F05032?style=plastic&logo=git&logoColor=white)
![Static Badge](https://img.shields.io/badge/Swagger-%2385EA2D?style=plastic&logo=swagger&logoColor=white)
![Static Badge](https://img.shields.io/badge/JUnit-%2325A162?style=plastic&logo=junit5&logoColor=white)

API документация доступна по [ссылке](https://51.250.33.187/swagger-ui.html).

### Инструкция по сборке и запуску приложения:

Приложение написано на Java 17 и Spring Boot 3. Запустить приложение можно двумя способами.

#### Первый способ:
- Предварительно создать БД POSTGRES не ниже версии 15 с именем *kardo_db*. 
- Установить и запустить Docker. Необходимо для выполнения тестов с использованием test-containers.
- Собрать jar файл при помощи maven командой `mvn clean install`.
- Запустить приложение командой `java -jar kardo-mobile-app-back`.
- Приложение будет доступно по адресу *https:/localhost*.
#### Второй способ:

- Установить и запустить Docker.
- Запустить контейнер командой `docker compose up`.
- Приложение будет доступно по адресу *https:/localhost*.




