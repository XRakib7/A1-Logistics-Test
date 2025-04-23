# A1 Logistics :truck:  

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)  
[![Android](https://img.shields.io/badge/Android-6.0%2B-brightgreen)](https://developer.android.com)  

A modern Android logistics app for efficient shipment tracking and route optimization. Built to simplify fleet management for drivers and administrators.  

<p align="center">  
  <img src="screenshots/demo.gif" alt="A1 Logistics Demo" width="300"/>  
</p>  

## Key Features :star2:  
- **Role-based authentication** (Driver/Admin)  
- **Real-time GPS tracking** with Google Maps integration  
- **Route optimization** using distance/time algorithms  
- **Document scanner** for shipment barcodes/invoices  
- **Push notifications** for delivery updates  
- **Offline mode** for remote areas with sync on reconnection  

## Tech Stack :wrench:  
- **Frontend**: Kotlin, Jetpack Compose  
- **Backend**: Firebase (Auth, Realtime DB, Cloud Messaging)  
- **Libraries**:  
  - `Retrofit` for REST API calls  
  - `WorkManager` for background sync  
  - `ZXing` for barcode scanning  
  - `Room` for local caching  

## Installation :arrow_down:  
1. Clone the repo:  
   ```bash  
   git clone https://github.com/your-username/A1-Logistics.git  
