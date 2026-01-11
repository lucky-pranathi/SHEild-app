# SHEild – Smart Wearable Self-Defense Device
SHEild is a compact wearable safety system designed to protect women during emergency situations. It works autonomously without relying on smartphones or continuous network connectivity. The device uses embedded sensors, RTOS-based control, and an ML-assisted threat detection model to identify danger and activate a controlled non-lethal deterrent. Also manually trigger of deterrent is present.
# Features
•	Autonomous operation without mobile dependency

•	Real-time sensor monitoring and threat detection

•	Non-lethal electric shock deterrent mechanism

•	RTOS for reliable and time-critical task execution

•	ML model to analyze panic or abnormal activity

•	Android app for user login, contact management, and configuration
# Technology Stack
•	Hardware: ESP32, GPS sensor, shock module, push button, buzzer, GSR sensor, Servo motor

•	Software: FreeRTOS , C++, Python (ML)

•	Mobile App: Android Studio (Java/XML)

•	Communication: Bluetooth, Secure data sync for configuration
# How It Works
1.	Sensors continuously monitor user activity.
2.	ML model detects panic or unusual behaviour.
3.	RTOS schedules high-priority emergency tasks.
4.	Device triggers alert and activates deterrent.
5.	Android app manages user settings (non-critical operations).
# Status
Prototype tested with functional sensing, ML detection, and deterrent activation.
# License
This project is for educational and research purposes.
