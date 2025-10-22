# Flashcard Server

Ktor-based REST API server for flashcard management and AI-powered flashcard generation.

## Features

- **CRUD Operations**: Create, read, update, delete flashcard sets
- **AI Generation**: Generate flashcards using Gemini AI
- **In-Memory Storage**: Simple storage with easy path to database migration
- **CORS Support**: Ready for web client integration
- **Comprehensive Logging**: Request/response logging with logback
- **Error Handling**: Centralized error handling with proper HTTP status codes

## Running the Server

### Prerequisites
- JDK 17 or higher
- Gemini API key (optional, can be provided per-request)

### Start Server

```bash
# From project root
./gradlew :server:run

# Or with API key
GEMINI_API_KEY=your-key-here ./gradlew :server:run
```

Server starts on `http://localhost:8080`

### Environment Variables

- `GEMINI_API_KEY` (optional): Default Gemini API key for flashcard generation. Clients can override by providing their own key in requests.

## API Endpoints

### Health Check
```
GET /health
```
Returns: `OK`

### Flashcard Operations

#### Get All Flashcard Sets
```
GET /api/v1/flashcards/sets
```
Returns: `List<FlashcardSet>`

#### Get Specific Flashcard Set
```
GET /api/v1/flashcards/sets/{id}
```
Returns: `FlashcardSet` or 404

#### Create/Save Flashcard Set
```
POST /api/v1/flashcards/sets
Content-Type: application/json

{
  "id": "uuid",
  "topic": "Kotlin Basics",
  "flashcards": [
    {
      "id": "uuid",
      "setId": "uuid",
      "front": "What is a data class?",
      "back": "A class primarily used to hold data with auto-generated methods",
      "createdAt": 1234567890
    }
  ],
  "createdAt": 1234567890
}
```
Returns: `FlashcardSet` with 201 status

#### Delete Flashcard Set
```
DELETE /api/v1/flashcards/sets/{id}
```
Returns: 204 No Content

#### Get Randomized Flashcards
```
GET /api/v1/flashcards/sets/{id}/randomized
```
Returns: `List<Flashcard>` in random order, or 404

### AI Generation

#### Generate Flashcards
```
POST /api/v1/generate
Content-Type: application/json

{
  "topic": "Python Programming",
  "count": 10,
  "userQuery": "Focus on basic syntax and data types",
  "apiKey": "optional-gemini-key"
}
```

Returns:
```json
{
  "flashcardSet": { ... },
  "error": null
}
```

Or on error:
```json
{
  "flashcardSet": null,
  "error": "Error message"
}
```

## Testing with curl

### Health Check
```bash
curl http://localhost:8080/health
```

### Get All Sets
```bash
curl http://localhost:8080/api/v1/flashcards/sets
```

### Generate Flashcards
```bash
curl -X POST http://localhost:8080/api/v1/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "JavaScript",
    "count": 5,
    "userQuery": "Focus on ES6 features",
    "apiKey": "your-gemini-key"
  }'
```

### Create Flashcard Set
```bash
curl -X POST http://localhost:8080/api/v1/flashcards/sets \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Test Topic",
    "flashcards": [
      {
        "setId": "test-id",
        "front": "Question?",
        "back": "Answer!"
      }
    ]
  }'
```

## Architecture

### Module Structure
```
server/
├── src/main/kotlin/com/flashcards/server/
│   ├── Application.kt                  # Server entry point
│   ├── plugins/                        # Ktor plugins
│   │   ├── Serialization.kt
│   │   ├── CORS.kt
│   │   ├── CallLogging.kt
│   │   ├── StatusPages.kt
│   │   └── Routing.kt
│   ├── routes/                         # API route handlers
│   │   ├── FlashcardRoutes.kt
│   │   └── GeneratorRoutes.kt
│   ├── repository/                     # Business logic
│   │   └── ServerFlashcardRepository.kt
│   ├── generator/                      # AI generation
│   │   └── ServerFlashcardGenerator.kt
│   └── storage/                        # Data persistence
│       └── InMemoryStorage.kt
└── src/main/resources/
    └── logback.xml                     # Logging configuration
```

### Dependencies
- **Ktor 3.0.3**: Server framework with Netty engine
- **kotlinx.serialization**: JSON serialization
- **Koog AI Agents**: Gemini integration for flashcard generation
- **Logback**: Logging

## Future Enhancements

- [ ] Replace in-memory storage with PostgreSQL/MongoDB
- [ ] Add authentication (JWT)
- [ ] User-specific flashcard sets
- [ ] Rate limiting for AI generation
- [ ] Metrics and monitoring
- [ ] Docker deployment
- [ ] Database migrations
