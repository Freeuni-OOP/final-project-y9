[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/skmUAHf8)
# Final-Project
OOP ფინალური პროექტი

# Y9 Gaming Site

OOP ფინალური პროექტი — თამაშების საიტი, რომელიც აგებულია **Spring Boot**-ზე (Java + Thymeleaf) და მოიცავს ავტორიზაციას, რამდენიმე მინი-თამაშს, ჩატს, ლიდერბორდს, მიღწევებს და ადმინ პანელს. Y9 Gaming Site-ზე მომხმარებელს შეუძლია დარეგისტრირდეს, ითამაშოს რამდენიმე სახის თამაში (ქვიზი, Wordle, Sudoku, ჯოკერი), მოაგროვოს ქულები და მიღწევები, დაუმეგობრდეს სხვა მომხმარებლებს, ესაუბროს რეალურ დროში ჩატში და ნახოს საკუთარი პოზიცია ლიდერბორდში. აპლიკაციას აქვს ცალკე ადმინისტრატორის პანელიც.


- **Java 17+** (Backend)
- **Spring Boot 4** — `spring-boot-starter-web(mvc)`, `data-jpa`, `security`, `thymeleaf`, `websocket`, `webservices`
- **Thymeleaf** — HTML შაბლონები (server-side rendering)
- **Spring Security** + საკუთარი **JWT** implementation (RSA key pair-ით ხელმოწერილი ტოკენები)
- **MySQL** — მონაცემთა ბაზა (`mysql-connector-j`)
- **Maven** (Maven Wrapper ჩართულია, ცალკე ინსტალაცია არ სჭირდება)
- **Lombok**
- **WebSocket** — ჩატისთვის


## პროექტის გასაშვებად დაგჭირდებათ:

1. **JDK 17** ან უფრო ახალი ვერსია ([Adoptium Temurin](https://adoptium.net/) ან სხვა დისტრიბუტივი)
   -  `pom.xml`-ში `maven-compiler-plugin`-ს აქვს მითითებული `source/target 25`. თუ თქვენს მანქანაზე დაინსტალირებული არ არის JDK 25, საჭირო გახდება ან JDK 25-ის დაყენება, ან ამ მნიშვნელობების `pom.xml`-ში 17-ზე შეცვლა (`<source>17</source>`, `<target>17</target>`).
2. **MySQL სერვერი** (ლოკალურად დაყენებული ან დისტანციური), ვერსია 8.x რეკომენდირებულია
3. **Git** (რეპოზიტორიის კლონირებისთვის, სურვილისამებრ)
4. ინტერნეტ კავშირი — Maven-ს პირველი გაშვებისას სჭირდება დამოკიდებულებების ჩამოტვირთვა

> Maven-ის ცალკე ინსტალაცია არ არის საჭირო — პროექტში ჩართულია **Maven Wrapper** (`mvnw` / `mvnw.cmd`).

##  პროექტის სტრუქტურა

```
final-project-y9/
├── keys/                       # JWT RSA გასაღებები (private/public)
│   ├── jwt_private.key
│   └── jwt_public.key
└── y9_gaming_site/             # მთავარი Spring Boot მოდული
    ├── mvnw / mvnw.cmd         # Maven wrapper (Linux/macOS და Windows)
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/org/example/y9_gaming_site/
        │   │   ├── auth/           # რეგისტრაცია/ავტორიზაცია
        │   │   ├── security/       # JWT ფილტრი, SecurityConfig, TokenUtil
        │   │   ├── game/           # sudoku, wordle, joker
        │   │   ├── gameRecord/         # თამაშების ისტორია და ჩანაწერები
        │   │   ├── user/               # მომხმარებლის ლოგიკა და ენთითები
        │   │   ├── gamerules/          # თამაშის წესები და ვალიდაცია      
        │   │   ├── quiz/           # ქვიზების მოდული
        │   │   ├── Challenge/      # გამოწვევები
        │   │   ├── achievement/    # მიღწევები
        │   │   ├── homePage/           # მთავარი გვერდის ლოგიკა
        │   │   ├── leaderboard/    # ლიდერბორდი
        │   │   ├── notification/       # შეტყობინებების სისტემა
        │   │   ├── streak/         # სერიები (streaks)
        │   │   ├── friendship/     # მეგობრობა
        │   │   ├── chat/           # WebSocket ჩატი
        │   │   ├── profile/        # პროფილი
        │   │   ├── admin/          # ადმინ პანელი
        │   │   └── Y9GamingSiteApplication.java   # Main კლასი
        │   └── resources/
        │       ├── application.properties         # ბაზის/სერვერის კონფიგურაცია
        │       ├── schema.sql                      # ბაზის სქემა (ავტომატურად იტვირთება)
        │       ├── wordlists/           # სიტყვების ბაზა (მაგ. Wordle-სთვის)
        │       ├── static/              # სტატიკური რესურსები (css, img, js, visualExternals)
        │       └── templates/           # Thymeleaf HTML გვერდები
        │           ├── admin/           # ადმინისტრირების გვერდები
        │           ├── fragments/       # HTML ფრაგმენტები (header, footer და ა.შ.)
        │           ├── joker/           # ჯოკერის თამაშის ინტერფეისი
        │           ├── Chat.html
        │           ├── addQuiz.html
        │           ├── homepage.html
        │           ├── index.html
        │           ├── leaderboard.html
        │           ├── profile.html
        │           ├── quizzes.html
        │           ├── sudoku.html
        │           └── wordle.html
        └── test/java/org/example/y9_gaming_site/
            ├── FileStorageServiceTest.java  # ფაილების შენახვის ტესტები
            └── UserServiceTest.java         # მომხმარებლის სერვისის ტესტები
```

## 🚀 გაშვების ნაბიჯები

### 1. პროექტის მოპოვება

ჩამოტვირთეთ/გაანარქივეთ პროექტი ან დააკლონეთ რეპოზიტორია, ისე რომ **`keys/` ფოლდერი დარჩეს `y9_gaming_site/`-ის გვერდით** (ერთ დონეზე ზემოთ), რადგან JWT-ის კოდი ამ გასაღებებს ეძებს ან `../keys/`-ში, ან `y9_gaming_site/keys/`-ში (იხილეთ `TokenUtil.java`).

```
final-project-y9/
├── keys/               ← აქ უნდა იყოს
└── y9_gaming_site/     ← აქედან გაუშვებთ პროექტს
```

თუ გასაღებები არსად მოიძებნება, აპლიკაცია ავტომატურად შექმნის დროებით შემთხვევით RSA წყვილს (კონსოლში დაინახავთ warning-ს), მაგრამ ეს ტოკენებს რესტარტების შემდეგ არასწორად გახდის ვალიდურს — რეკომენდებულია ორიგინალი `keys/` ფოლდერის შენარჩუნება.

### 2. MySQL ბაზის მომზადება

შექმენით ცარიელი ბაზა:

```sql
CREATE DATABASE `y9-game-db` CHARACTER SET utf8mb4;
```

ბაზის ცხრილები ავტომატურად შეიქმნება `schema.sql`-იდან პროექტის პირველივე გაშვებისას (`spring.sql.init.mode=always`).

### 3. `application.properties`-ის კონფიგურაცია

გახსენით `y9_gaming_site/src/main/resources/application.properties` და მიუთითეთ საკუთარი ბაზის მონაცემები:

```properties
spring.application.name=y9_gaming_site

spring.datasource.url=jdbc:mysql://localhost:3306/y9-game-db
spring.datasource.username=<თქვენი_username>
spring.datasource.password=<თქვენი_password>

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

server.port=8081
```

### 4. პროექტის აგება და გაშვება

გადადით `y9_gaming_site` საქაღალდეში:

```bash
cd final-project-y9/y9_gaming_site
```

**Linux / macOS:**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

**Windows:**

```cmd
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

ალტერნატივა — თუ სისტემაზე გაქვთ Maven დაინსტალირებული:

```bash
mvn clean install
mvn spring-boot:run
```

ან, `.jar` ფაილის აწყობის შემდეგ:

```bash
./mvnw clean package
java -jar target/y9_gaming_site-0.0.1-SNAPSHOT.jar
```
თუ `.jar`-ის საშუალებით უშვებთ, დარწმუნდით, რომ სამუშაო დირექტორია (`java -jar`-ის გამოძახების ადგილი) ისეთია, საიდანაც `keys/` ფოლდერამდე მისადგომია `../keys/` ან `keys/` ბილიკით.

### 5. აპლიკაციის გახსნა

წარმატებული გაშვების შემდეგ სერვერი ავტომატურად ჩაირთვება მისამართზე:

```
http://localhost:8081
```

გახსენით ეს ბმული ბრაუზერში — გამოჩნდება მთავარი გვერდი (`index.html`), საიდანაც შესაძლებელია რეგისტრაცია/ავტორიზაცია და თამაშების არჩევა.

## ტესტების გაშვება

```bash
./mvnw test
```

ტესტები მოიცავს unit და integration ტესტებს ავტორიზაციის, ქვიზების, Wordle-ის, Sudoku-ს, ჯოკერის, მიღწევებისა და ადმინის ლოგიკისთვის (`src/test/java`).

##  ძირითადი ფუნქციონალი

- **რეგისტრაცია/ავტორიზაცია** — JWT-ზე დაფუძნებული (RSA ხელმოწერით), სტუმრის (guest) რეჟიმის მხარდაჭერით
- **თამაშები**: Quiz, Wordle, Sudoku, ჯოკერი (კარტების თამაში)
- **ლიდერბორდი** — მომხმარებელთა რეიტინგი ქულების მიხედვით
- **მიღწევები (Achievements)** და **სერიები (Streaks)**
- **მეგობრობის სისტემა** (მოთხოვნების გაგზავნა/მიღება)
- **რეალურ დროში ჩატი** — WebSocket-ის საშუალებით
- **პროფილის გვერდი** — ავატარის ატვირთვის შესაძლებლობით
- **ადმინ პანელი** — მომხმარებლების და კონტენტის მართვისთვის (`ROLE_ADMIN`)

წვდომის წესები დეტალურად აღწერილია `SecurityConfig.java`-ში — მაგალითად, `/admin/**` ხელმისაწვდომია მხოლოდ `ADMIN` როლისთვის, ხოლო თამაშების უმეტესობა მოითხოვს ავტორიზებულ მომხმარებელს.

