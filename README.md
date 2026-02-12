# Community Skill Exchange Platform

The **Community Skill Exchange Platform** is a distributed microservices-based application designed to facilitate skill sharing and learning within a community. It leverages a modern technology stack with Spring Boot for the backend services and React (Vite) for the frontend.

## 🏗️ Architecture

The backend is built using a microservices architecture, with each service responsible for a specific domain. Services communicate via REST APIs and Kafka for asynchronous messaging. Service discovery is handled by Netflix Eureka, and external access is managed through Spring Cloud Gateway.

### Core Services

| Service | Port | Description | Technology Stack |
| :--- | :--- | :--- | :--- |
| **Eureka Server** | `8761` | Service Discovery Server. | Spring Cloud Netflix Eureka |
| **Gateway Service** | `8888` | API Gateway, routing, and authentication filter. | Spring Cloud Gateway |
| **Auth Service** | `8081` | User authentication (Register, Login, JWT generation). | PostgreSQL, Kafka, JWT |
| **User Profile** | `8087` | Manages user profiles and portfolio. | PostgreSQL, Kafka, Cloudinary |
| **Skill Service** | `8086` | Manages skill catalog and search. | PostgreSQL, OpenSearch, Kafka |
| **Booking Service** | `8082` | Handles session bookings and scheduling. | PostgreSQL, Kafka |
| **Chat Service** | `8083` | Real-time messaging between users. | MongoDB, Kafka, WebSockets |
| **Notification Service** | `8084` | Email and push notifications. | MongoDB, Gmail SMTP, OneSignal, Kafka |
| **Review Service** | `8085` | Reviews and ratings for skills/sessions. | PostgreSQL |

### Frontend

| Application | Port | Description | Technology Stack |
| :--- | :--- | :--- | :--- |
| **Skillshare Frontend** | `5173` | Main user interface. | React (Vite), TailwindCSS, Axios, StompJS |

## 🛠️ Technology Stack

- **Backend**: Java 17+, Spring Boot 3.x, Spring Cloud
- **Frontend**: React.js, Vite, TailwindCSS
- **Databases**: PostgreSQL (Relational), MongoDB (Chat/Notifications), OpenSearch (Search)
- **Messaging**: Apache Kafka
- **Infrastructure**: Docker (optional containerization)

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:
- **Java 17+** (JDK)
- **Node.js 18+** & npm
- **PostgreSQL** (Running on default port 5432)
- **MongoDB** (Running on default port 27017)
- **Apache Kafka** & Zookeeper (Running on default port 9092)
- **OpenSearch** (Running on configured port, e.g., 9200)

### Environment Configuration

Each service has its own `application.yaml` or `application.properties`. You may need to configure environment variables or update the files directly. Common variables include:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (PostgreSQL)
- `JWT_SECRET` (Shared secret for token signing)
- `EUREKA_URL` (Default: `http://localhost:8761/eureka`)
- `KAFKA_BOOTSTRAP_SERVERS` (Default: `localhost:9092`)
- `CLOUD_NAME`, `API_KEY`, `API_SECRET` (Cloudinary for User Profile)
- `EMAIL`, `EMAIL_PASSWORD` (Gmail SMTP for Notifications)
- `ONESIGNAL_APP_ID`, `ONESIGNAL_REST_API_KEY` (OneSignal for Push Notifications)

### Installation & Run Steps

#### 1. Infrastructure Services
Ensure PostgreSQL, MongoDB, Kafka, and OpenSearch are running.

#### 2. Start Backend Services
Start the services in the following order to ensure dependencies are met:

1.  **Eureka Server** (`eurekaServer`)
2.  **Gateway Service** (`gatewayservice`)
3.  **Auth Service** (`authService`)
4.  **User Profile Service** (`userprofile`)
5.  **Other Services** (`skillservice`, `bookingservice`, `chat-service`, `notification-service`, `review-service`)

For each service:
```bash
cd <service-directory>
mvn spring-boot:run
```

#### 3. Start Frontend
```bash
cd skillshare-frontend
npm install
npm run dev
```
The frontend will be available at `http://localhost:5173`.

## 📡 API Endpoints (Gateway)

All API requests should be routed through the Gateway Service on port `8888`.

- **Auth**: `/auth/**`
- **Users**: `/api/users/**`
- **Skills**: `/api/skills/**` (Search: `/api/search/**`)
- **Bookings**: `/api/bookings/**`
- **Chat**: `/api/chat/**` (WebSocket: `/ws`)
- **Notifications**: `/api/notifications/**`
- **Reviews**: `/api/reviews/**`

## 🤝 Contribution

1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.
