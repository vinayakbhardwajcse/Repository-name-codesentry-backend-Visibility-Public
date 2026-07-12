# 🔍 CodeSentry — AI-Powered Java Code Review System

[![Live Demo](https://img.shields.io/badge/Live-Demo-blue)](https://codesentry-ui.vercel.app)
[![Backend](https://img.shields.io/badge/Backend-Render-green)](https://codesentry-backend-ztuy.onrender.com)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue)](https://reactjs.org)

## 🌐 Live Demo
**Frontend:** https://codesentry-ui.vercel.app
**Backend:** https://codesentry-backend-ztuy.onrender.com

---

## 📋 Overview

CodeSentry is a full-stack AI-powered static analysis tool that detects bugs, security vulnerabilities, and code smells in Java source code. It combines traditional static analysis using Abstract Syntax Tree (AST) parsing with Google Gemini AI to provide intelligent fix suggestions in plain English.

Built as a Final Year B.Tech Computer Science Engineering Project at RKGIT, targeting SDE placement roles.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔍 Static Analysis | Detects 6 categories of Java bugs using AST parsing |
| 🤖 AI Explanations | Google Gemini AI generates plain-English fix suggestions |
| 📊 Health Score | 0-100 code quality score based on issue severity |
| 🐙 GitHub Scanner | Automatically scans all Java files in any GitHub repository |
| 📄 PDF Reports | Professional downloadable reports using iText7 |
| 🌙 Dark Mode | Modern UI with light/dark theme toggle |
| 📁 File Upload | Drag and drop .java file support |

---

## 🐛 Detection Rules

| Rule | Category | Severity |
|------|----------|----------|
| Empty Catch Block | Bug Risk | HIGH |
| Unused Variable | Code Smell | LOW |
| Long Method | Code Smell | MEDIUM |
| Magic Number | Style | LOW |
| SQL Injection Risk | Security | CRITICAL |
| Resource Leak | Bug Risk | HIGH |
---

## 🛠️ Tech Stack

### Backend
- **Java 21** — Core programming language
- **Spring Boot 3.5** — REST API framework
- **JavaParser 3.26** — AST based static analysis
- **Google Gemini AI** — AI powered explanations
- **iText7** — PDF report generation
- **Docker** — Containerized deployment

### Frontend
- **React 18** — UI framework
- **Axios** — HTTP client
- **CSS3** — Custom styling with dark mode

### Deployment
- **Render** — Backend hosting (Docker)
- **Vercel** — Frontend hosting (CI/CD)
- **GitHub** — Version control

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- Node.js 22+
- Gemini API key

### Backend Setup
```bash
# Clone repository
git clone https://github.com/vinayakbhardwajcse/Repository-name-codesentry-backend-Visibility-Public.git

# Add your keys to application.properties
gemini.api.key=YOUR_GEMINI_KEY
github.token=YOUR_GITHUB_TOKEN

# Run
mvn spring-boot:run
```

### Frontend Setup
```bash
# Clone frontend repository
git clone https://github.com/vinayakbhardwajcse/-codesentry-frontend.git

# Install dependencies
npm install

# Start
npm start
```

---

## 📸 Screenshots

### Code Review Dashboard
- Paste or upload Java code
- Get instant health score
- View AI-powered explanations

### GitHub Repository Scanner
- Paste any public GitHub URL
- Scan all Java files automatically
- View per-file health scores

### PDF Report
- Professional downloadable report
- Includes all issues and AI explanations

---

## 🔌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/scan/text | Scan Java code as text |
| POST | /api/scan/file | Upload and scan .java file |
| POST | /api/scan/report | Generate PDF report |
| POST | /api/github/scan | Scan GitHub repository |
| GET | /api/scan/status | Health check |

---

## 🎯 Design Patterns Used

- **Interface Pattern** — Rule interface for Open/Closed Principle
- **Builder Pattern** — CodeIssue and ScanResult models
- **Service Layer Pattern** — Separation of concerns
- **Strategy Pattern** — Pluggable analysis rules

---

## 📊 How Health Score is Calculated
---

## 🔮 Future Enhancements

- [ ] Scan History with MySQL database
- [ ] User Authentication (Spring Security)
- [ ] VS Code Extension
- [ ] Support for Python and JavaScript
- [ ] Real-time analysis as you type

---

## 👨‍💻 Developer

**Vinayak Bhardwaj**
B.Tech Computer Science Engineering
Raj Kumar Goel Institute of Technology (RKGIT)
Batch: 2023-2027

[![GitHub](https://img.shields.io/badge/GitHub-vinayakbhardwajcse-black)](https://github.com/vinayakbhardwajcse)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue)](https://linkedin.com/in/vinayakbhardwajcse)

---

## 📄 License

This project is built for academic purposes as a Final Year Project.
---

## 🏗️ System Architecture
