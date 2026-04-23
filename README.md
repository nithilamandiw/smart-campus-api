# 🏫 Smart Campus API

A RESTful API for managing smart campus resources such as rooms and sensors, built using **JAX-RS (Jersey)** and deployed on **Apache Tomcat 9**.

---

## 🚀 Tech Stack

* Java (JDK 8+)
* JAX-RS (Jersey 2.x)
* Apache Tomcat 9
* Maven (WAR packaging)
* JSON (Jackson)

---

## ⚙️ Project Setup

### 1. Prerequisites

- JDK 8+
- Maven 3.6+
- Apache Tomcat 9.x

### 2. Clone the Repository

```bash
git clone https://github.com/nithilamandiw/smart-campus-api.git
cd smart-campus-api
```

---

### 3. Build the Project

```bash
mvn clean package
```

This generates a `.war` file inside the `target/` directory.

---

### 4. Deploy to Tomcat 9

1. Copy `target/smart-campus-api.war` to `<TOMCAT_HOME>/webapps/`
2. Start Tomcat:

```bash
<TOMCAT_HOME>/bin/startup.sh
```
(or `startup.bat` on Windows)

3. Confirm the app is up:

```bash
curl http://localhost:8080/smart-campus-api/api/v1
```

---

## 🌐 Base URL

```
http://localhost:8080/smart-campus-api/api/v1
```

---

## 📡 API Endpoints

### 🏢 Rooms

| Method | Endpoint | Description |
| ------ | -------- | ----------- |
| GET | /rooms | Get all rooms |
| POST | /rooms | Create room |
| GET | /rooms/{id} | Get room by ID |
| DELETE | /rooms/{id} | Delete room (fails if room still has sensors) |
| GET | /rooms/{roomId}/sensors | Get sensors in a room |
| POST | /rooms/{roomId}/sensors | Create sensor directly under a room |

---

### 🌡️ Sensors

| Method | Endpoint | Description |
| ------ | -------- | ----------- |
| GET | /sensors | Get all sensors |
| GET | /sensors?type={type} | Filter sensors by type |
| POST | /sensors | Create sensor |
| GET | /sensors/{id}/readings | Get readings for a sensor |
| POST | /sensors/{id}/readings | Add reading for a sensor |

---

## ⚠️ Error Handling

The API uses custom exception handling for clear HTTP responses:

| Status Code | Description                        |
| ----------- | ---------------------------------- |
| 400         | Bad Request (invalid input / JSON) |
| 404         | Resource not found                 |
| 422         | Linked resource not found          |
| 403         | Sensor is under maintenance        |
| 409         | Room cannot be deleted (not empty) |
| 500         | Internal server error              |

---

## 🧪 Working curl Commands

Set once:

```bash
BASE_URL="http://localhost:8080/smart-campus-api/api/v1"
```

1. Create room

```bash
curl -X POST "$BASE_URL/rooms" \
  -H "Content-Type: application/json" \
  -d '{"id":"room-101","name":"Engineering Lab","capacity":40}'
```

2. Get rooms

```bash
curl "$BASE_URL/rooms"
```

3. Create sensor

```bash
curl -X POST "$BASE_URL/sensors" \
  -H "Content-Type: application/json" \
  -d '{"id":"sensor-101","type":"TEMPERATURE","status":"ACTIVE","roomId":"room-101"}'
```

4. Filter sensors by type

```bash
curl "$BASE_URL/sensors?type=TEMPERATURE"
```

5. Add reading

```bash
curl -X POST "$BASE_URL/sensors/sensor-101/readings" \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

6. Delete room (bonus)

```bash
curl -X POST "$BASE_URL/rooms" \
  -H "Content-Type: application/json" \
  -d '{"id":"room-empty","name":"Temp Room","capacity":10}'

curl -X DELETE "$BASE_URL/rooms/room-empty"
```

---

---

Part 1 - Service Architecture & Setup

Q1: JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a new instance of a Resource class per HTTP request. This means instance variables cannot hold shared data between requests. To persist data across requests, shared state must be stored in static fields, as implemented in RoomStore, SensorStore, and SensorReadingStore.
In this implementation, HashMap is used for simplicity, as this is a single-threaded demonstration environment. In a production system with concurrent requests, ConcurrentHashMap would be preferred to prevent race conditions and data corruption.

Q2: Why HATEOAS is Important in RESTful Design

HATEOAS allows an API to provide its clients with information about how to navigate the API. By linking to APIs in the responses they receive, clients can follow actual links to navigate an entire API instead of being forced to use hard-coded URLs or static documentation. This reduces the coupling between the client and server. If a URL changes, a client that follows the hypermedia link continues to work without needing to change the code at the client. This also makes exploring and integrating the API easier.

Part 2 - Room Management

Q3: Returning IDs vs Full Room Objects in a List

By returning only IDs, the size of the response is reduced. The downside is, however, that the client will need to make N additional requests to extract any useful data. This is known as the N+1 problem, and it wastes bandwidth and increases latency. When a client receives everything it requires right away in one response, it is more efficient to return full room objects. For large datasets, it is often advisable to return summary objects or information (e.g. id, name, capacity) and avoid deeply-nested structures.

Q4: Is DELETE Idempotent in This Implementation?

Yes, DELETE is idempotent at the state level. The initial DELETE request removes the room. Any subsequent DELETE request will return a 404 Not Found response. However, the server status will remain unchanged as the room does not exist. HTTP calls are guaranteed to reach the same end state within insight on repeated calls. Repeated calls don’t necessarily return the same response code. Returning a 404 when a second delete is done is correct and in line with REST.

Part 3 - Sensor Operations & Linking

Q5: Handling Unsupported Media Types with @Consumes

The annotation @Consumes(MediaType.APPLICATION_JSON) restricts the endpoint to application/json requests only. If a client sends some data in a different media type such as text/plain or application/xml then JAX-RS won’t be able to process this request as it won’t be able to find a suitable MessageBodyReader to read the request body into an instance of the expected Java object. Thus, an HTTP 415 Unsupported Media Type response is returned by the runtime automatically. This happens before invoking the a resource method. Thus, it allows strict input validation and makes sure no invalid data is processed by the API.

Q6: Query Parameter vs Path Segment for Filtering

Using @QueryParam (for example, ?type=CO2) is preferred because filtering is not part of the resource identity – it’s just an optional modifier on a collection. Resource path refers to sensor entity and query string is applied to filter results. If you embed the filter in the path (such as /sensors/type/CO2) it implies that this is a separate sub-resource and that is semantic wrong. It also becomes unmanageable if you start to have a lot of filters. Query parameters support optional filters that can combinably be added by the user (e.g., ?type=CO2?status=ACTIVE) without any effect on the path.

Part 4 - Deep Nesting with Sub-Resources

Q7: Benefits of the Sub-Resource Locator Pattern

The Sub-Resource Locator pattern allows a resource (SensorResource) to delegate nested path handling to a class (SensorReadingResource) at runtime. By doing this, it restricts each class to have one responsibility only. In other words, avoids building one large unmanageable controller. Also, the parent sensor Id can be passed through the constructor seamlessly. Furthermore, it allows each class to be tested independently, resulting in increased code quality and maintainability in large APIs.

Part 5 - Advanced Error Handling, Exception Mapping & Logging

Q8: Why HTTP 422 is More Semantically Accurate than 404

The URL endpoint itself does not exist – 404 Not Found However, when a client POSTs a sensor with a non-existing roomId, the endpoint /api/v1/sensors is entirely legal and was found. There is a problem in the request payload, an entity referenced is missing. HTTP 422 Unprocessable Entity is for this exact situation: the request was syntactically correct, the endpoint exists, but the contents failed business logic validation. By using 422, we clearly show the distinction and avoid misleading the client into thinking that the URL is wrong.

Q9: Cybersecurity Risks of Exposing Stack Traces

Stack traces reveal internal class names, packaging structure, method names, and line numbers. An attacker can use this information to identify the exact version of the libraries used and look up CVEs against it. They may also show internal file paths, query logic, and configuration information from deeper application layers. A global ExceptionMapper<Throwable> that catches all unexpected errors and only returns a generic “Internal server error”, in order to prevent all this information leaking to external consumers.

Q10: Why Use JAX-RS Filters for Logging Instead of Manual Logger Calls

Including Logger.info() within each resource method goes against the DRY principle and entwines infrastructure concerns into business logic. Consistent logging of requests and responses is a cross-cutting concern. The framework automatically invokes a single LoggingFilter class implementing ContainerRequestFilter and ContainerResponseFilter for every call without needing to add it to each method. This guarantees the log to be automatically generated on each API in our system. It prevents missing log when adding a new API. Also, it doesn’t make resource classes clutter with log-related codes.
