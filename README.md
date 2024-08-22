# Backend для мобильного приложения КАРДО

### Приложение разрабатывал:
Владимир Баханович ([vvbakhanovich](https://github.com/vvbakhanovich)).

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

---

Приложение представляет мобильную версию сайта международной премии КАРДО с дополнительным фукнционалом социальной сети. Пользователи могут регистрироваться в приложении, смотреть, искать мероприятия, а также оставлять заявки на участие в этих мероприятиях. Дополнительно была реализована возможность создавать посты и прикреплять к ним изображения и видео. Пользователи могут лайкать посты, оставлять комментарии и добавлять других пользователей в друзья.

В приложении реализована аутентифицкаия и авторизация при помощи _Spring Security 6_. После регистрации, пользователь после входа в приложение получает два _JWT_ токена (_access_ и _refresh_ токен). Для обращения по эндпоинтам используется Bearer аутентификация, в которой пользователь указывает свой _access_ токен. В целях безопасности срок жизни _access_ токена невелик (по умолчанию составляет 5 минут). По окончании срока жизни токена пользователь должен получить новый _access_ токен, обратившись по соответвуюшему эндпоинту и указав при этом свой _refresh_ токен (по умолчанию срок жизни _refresh_ токена составляет 1 день). После окончания срока жизни _refresh_ токена пользователю будет необходимо заново войти в приложение.

API документация доступна по [ссылке](https://51.250.33.187/swagger-ui.html).

---

### Инструкция по сборке и запуску приложения:

Приложение написано на Java 17 и Spring Boot 3.

Перед сборкой и запуском приложения необходимо указать путь для хранения загружаемых файлов при помощи свойства
__server.file-storage.directory__ в *application.properties*.

Запустить приложение можно двумя способами.

#### Первый способ:
- Предварительно создать БД POSTGRES не ниже версии 15 с именем *kardo_db*.
- Установить и запустить Docker. Необходимо для выполнения тестов с использованием test-containers.
- Собрать jar файл при помощи maven командой `mvn clean install`.
- Запустить приложение командой `java -jar kardo-moblie-app-back.jar`.
- Приложение будет доступно по адресу *https:/localhost*.
#### Второй способ:

- Установить и запустить Docker.
- Запустить контейнер командой `docker compose up`.
- Приложение будет доступно по адресу *https:/localhost*.




