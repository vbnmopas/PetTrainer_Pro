# **Petcare+ : AI 기반 스마트 반려동물 케어 로봇**  

## 🔹 프로젝트 개요  
**Petcare+**는 반려동물의 건강 관리와 훈련을 체계적으로 지원하는 **지능형 반려동물 케어 로봇**입니다.  
정량 급식, 훈련 수행 관리, 양방향 소통, 데이터 기반 건강 피드백을 하나의 통합 시스템으로 구현하여 보호자가 효율적으로 반려동물을 돌볼 수 있도록 돕습니다.  


---

## 🏗 시스템 아키텍처

- **ESP32 + 웹캠** : 반려동물 영상 촬영 및 로봇 제어
- **YOLOv5 기반 AI 모델** : 반려동물 행동 인식 (앉기, 눕기 등)
- **MQTT/HTTP 서버** : 앱 ↔ 로봇 간 통신
- **Android 앱** : 사용자 인터페이스, 실시간 모니터링 및 제어

---

## ⚙️ 기술 스택
### 백엔드 / AI
- Python 3.x
- PyTorch (AI 모델)
- YOLOv5 (AI 모델)
- OpenCV (영상 처리)
- Flask (HTTP 서버)
- Paho-MQTT

### 프론트엔드 / 모바일
- Kotlin
- Android Studio
- SurfaceView / MediaPlayer (스트리밍)

### 하드웨어
- ESP32-CAM (웹캠, 급식기 제어)
- DC 모터, 서보모터 (급식 제어)

### 인프라 / 기타
- MQTT Broker (Mosquitto)
- Git / Gradle

---

## 📁 프로젝트 구조
```
PatTrainer_Pro-main/
 ├── app/                  # Android 앱
 ├── yolov5/               # YOLOv5 AI 모델
 │   ├── data/             # 데이터셋
 │   ├── models/           # 모델 정의
 │   ├── utils/            # 유틸리티
 │   ├── detect.py         # 객체 탐지
 │   ├── train.py          # 모델 학습
 │   ├── stream.py         # 영상 스트리밍
 │   ├── mqtt_server.py    # MQTT 서버
 │   ├── http_server.py    # HTTP 서버
 │   └── requirements.txt  # 의존성
 ├── build.gradle
 ├── settings.gradle
 └── README.md
```

---

## 🚀 주요 기능
### 1. AI 기반 훈련 수행 관리
- 명령 입력 후 **YOLOv5 모델**을 활용해 반려동물 행동(앉기, 엎드리기 등) 인식  
- 명령과 실제 행동을 비교하여 훈련 성공 여부 판단  
- 훈련 데이터 기록 및 분석을 통한 피드백 제공
- 학습된 모델을 이용한 정확도 높은 인식

### 2. 스마트 급식 시스템
- **즉시 급식** / **예약 급식** 모드 지원
- 앱에서 버튼 클릭 → ESP32 급식기 모터 제어

### 3. 양방향 소통 시스템
- **ESP32-CAM을 활용한 실시간 스트리밍**으로 원격 반려동물 모니터링  
- 사용자의 얼굴을 로봇 디스플레이에 출력하여 상호작용  
- 마이크 버튼을 이용한 음성 송수신 기능 지원
- 앱 ↔ 서버 ↔ ESP32 간 MQTT 통신

### 4. 데이터 기반 건강 피드백
- 챗봇 탭에서 **급식 및 훈련 기록을 GPT에 자동 전송**하여 분석  
- 분석 결과를 기반으로 **반려동물의 건강 상태 및 훈련 진척도 제공**  
- 보호자가 별도의 입력 없이도 실시간 모니터링 가능  


---

## 🔧 설치 및 실행
### 1. YOLOv5 환경 세팅
```bash
cd yolov5
pip install -r requirements.txt
```

### 2. 모델 학습
```bash
python train.py --img 320 --batch 16 --epochs 50 --data data/custom.yaml --weights yolov5s.pt
```

### 3. 행동 인식 실행
```bash
python detect.py --weights runs/train/exp/weights/best.pt --source 0
```

### 4. 스트리밍 실행
```bash
python stream.py
```

### 5. MQTT 서버 실행
```bash
python mqtt_server.py
```

---

## ✨ 기대 효과
- 반려동물의 행동 모니터링 및 건강 관리
- 원격 급식 및 자동 돌봄
- AI 기반 맞춤형 반려동물 케어 서비스 제공



##
**Petcare+는 AI와 IoT 기술을 활용하여 보호자가 언제 어디서나 반려동물을 체계적으로 관리할 수 있도록 지원하는 스마트 케어 솔루션입니다.**  
