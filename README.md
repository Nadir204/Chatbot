
<div align="center">
  <h1>✨ Nadir Hossen | Anonna | Rede ✨</h1>
  <h3>🚀 Chatbot Developers | CSE Students</h3>
</div>

# Chatbot
Softwere Engeneering Course Project
After download all the file ,
Rename the "Chatbot-main" folder name to "Chatbot"


## ✨ Features
- User Registration & Login
- Real-time AI Chat powered by Groq
- Chat History saved per user
- Memory system (last 5 messages)
- Multi-user support
- Password strength validation
- Persistent local database
- H2 database

## 🗂️ Project Structure
```
Chatbot/
│
├── data/
│   └── chatbotdb.mv.db
│
├── src/main/java/com/example/chatbot/
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── ChatController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── ChatService.java
│   │   └── MemoryService.java
│   ├── model/
│   │   ├── User.java
│   │   └── ChatMessage.java
│   ├── repo/
│   │   ├── UserRepo.java
│   │   └── ChatRepo.java
│   └── ChatbotApplication.java
│
└── src/main/resources/
    ├── application.properties
    └── static/
        ├── login.html
        ├── chat.html
        ├── stylelogin.css
        ├── stylechat.css
        ├── scriptlogin.js
        └── scriptchat.js
```

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /auth/register | Register new user |
| POST | /auth/login | Login user |
| POST | /chat | Send message to bot |
| GET | /chat/history | Get chat history |




api reuse-------------------------------------------------
APi from : https://console.groq.com/

Model:llama-4-scout · groq


### 2. Create .env file
Create .env file inside Chatbot folder structure:
```
Chatbot/
|
|-.env
```
And inside .env file declare Api like :
GROQ_API_KEY={Your Api key }

[Don't change variable name]

-------------------------------Model Chose and change in chatservice------------------------
GROQ_URL --according to api
GROQ_MODEL ---according to your model


### 3. Run the application
mvn spring-boot:run
