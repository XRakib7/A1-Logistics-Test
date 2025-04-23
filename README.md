# 🚚 A1 Logistics – Android App (Open Source)

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)  
[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen)](https://developer.android.com)  
[![Firebase](https://img.shields.io/badge/Firebase-integrated-orange)](https://firebase.google.com)  

A **modern logistics management Android app** built from scratch to streamline parcel tracking and fleet operations for delivery businesses. A1 Logistics is **feature-rich**, **secure**, and **optimized for performance**. Designed for Bangladeshi eCommerce logistics but adaptable for global use cases.

<p align="center">  
  <img src="screenshots/demo.gif" alt="A1 Logistics Demo" width="300"/>  
</p>

---

## 📸 Screenshots

| Splash Screen | Login | Merchant Dashboard | Status History |
|---|---|---|---|
| ![Splash](screenshots/splash.png) | ![Login](screenshots/login.png) | ![Dashboard](screenshots/dashboard.png) | ![History](screenshots/history.png) |

---

## ✨ Key Features

### 🧑‍💼 Role-Based Access
- **Admin Dashboard**: Full control, analytics, and system management
- **Merchant Dashboard**: Parcel management, tracking, and filtering
- **Secure Authentication**: Role-based (Admin, Merchant), hidden admin signup

### 📦 Package Management
- **Unique Package IDs**: Format – `A1-M001-0001`, `A1-M001-0002`, etc.
- **User ID System**:
  - Merchants: `M001`, `M002`, …
  - Admins: `A001`, `A002`, …
- **Status Tracking**: Real-time updates with full **status history** in **chronological order**
- **Advanced Filters**: Filter parcels by status, date, or last update
- **Excel Export**: Download single or bulk package data

### 📱 App UX/UI
- Clean, **modern UI** with **smooth animations**
- Fast and responsive design with **minimal load times**
- **Splash screen**, custom-designed login/signup pages

---

## 🛠 Tech Stack

- **Language**: Java (w/ Gradle Kotlin DSL)
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Excel Export**: Apache POI
- **Minimum SDK**: Android 8 (API 26)

### 🔌 External Libraries
- `Firebase SDK` – Auth, Firestore
- `Apache POI` – Excel export
- `Material Design Components` – UI styling

---

## 🚀 Installation Guide

### 1. Clone the Repository

```bash
git clone https://github.com/xrakib7/a1-logistics.git
cd a1-logistics
