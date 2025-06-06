# 🚚 A1 Logistics – Android App (Open Source)

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/XRakib7/A1-Logistics-Test/blob/main/Licences)

## License

This project is licensed for **personal and non-commercial use only**. If you'd like to use it for business or commercial purposes, please [contact me](email :hackbet07@gmail.com).

Full license: [LICENSE](https://github.com/XRakib7/A1-Logistics-Test/blob/main/Licences)


[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen)](https://developer.android.com)  
[![Firebase](https://img.shields.io/badge/Firebase-integrated-orange)](https://firebase.google.com)  

A **modern logistics management Android app** built from scratch to streamline parcel tracking and fleet operations for delivery businesses. A1 Logistics is **feature-rich**, **secure**, and **optimized for performance**. Designed for Bangladeshi eCommerce logistics but adaptable for global use cases.

<p align="center">  
  <img src="screenshots/demo.gif" alt="A1 Logistics Demo" width="300"/>  
</p>

---

## 📸 Screenshots

| Splash Screen | Login | Merchant Dashboard | Packages Details | Status History |
|---|---|---|---|---|
| ![Splash](https://github.com/XRakib7/A1-Logistics-Test/blob/main/A1%20Logistics%20Screenshots/Screenshot_20250424-020146.png) | ![Login](https://github.com/XRakib7/A1-Logistics-Test/blob/main/A1%20Logistics%20Screenshots/Screenshot_20250424-020150.png) | ![Dashboard](https://github.com/XRakib7/A1-Logistics-Test/blob/main/A1%20Logistics%20Screenshots/Screenshot_20250424-020350.png) | ![Package](https://github.com/XRakib7/A1-Logistics-Test/blob/main/A1%20Logistics%20Screenshots/Screenshot_20250424-020503.png) | ![History](https://github.com/XRakib7/A1-Logistics-Test/blob/main/A1%20Logistics%20Screenshots/Screenshot_20250424-021715.png) |

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
