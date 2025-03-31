"""
import os
from collections import defaultdict

# 라벨 파일이 저장된 최상위 폴더 경로
label_root_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/labels"

# 클래스별 개수 카운트
class_counts = defaultdict(int)

# 모든 하위 폴더 탐색
for root, _, files in os.walk(label_root_path):
    for file in files:
        if file.endswith(".txt"):  # 라벨 파일만 읽기
            file_path = os.path.join(root, file)
            with open(file_path, "r") as f:
                lines = f.readlines()
                for line in lines:
                    class_idx = int(line.split()[0])  # YOLO 포맷에서 첫 번째 값이 클래스 인덱스
                    class_counts[class_idx] += 1

# 결과 출력
print(f"🟢 앉기(Sitting) 데이터 개수: {class_counts[0]}")
print(f"🔵 눕기(Lying) 데이터 개수: {class_counts[1]}")

# 비율 계산
total = sum(class_counts.values())
if total > 0:
    print(f"📊 앉기 비율: {class_counts[0] / total * 100:.2f}%")
    print(f"📊 눕기 비율: {class_counts[1] / total * 100:.2f}%")
else:
    print("❌ 라벨 데이터가 없습니다!")

"""


import cv2
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import img_to_array
from collections import deque

# 📌 모델 로드
model = load_model("dog_behavior_model_v2.h5")

# 📌 클래스 라벨 (0: 앉기, 1: 눕기)
class_labels = {0: "Sitting", 1: "Lying"}

# 📌 비디오 불러오기
video_path = "C:/testimage/sitDog2.mp4"
cap = cv2.VideoCapture(video_path)

# 📌 시퀀스 데이터를 저장할 큐 (최근 14개 프레임 저장)
frame_sequence = deque(maxlen=14)

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    # 📌 프레임 크기 조정 (64x64로 변경)
    img = cv2.resize(frame, (64, 64))
    img = img_to_array(img) / 255.0  # **정규화**
    frame_sequence.append(img)

    # 📌 14개 프레임이 모이면 예측 수행
    if len(frame_sequence) == 14:
        input_data = np.expand_dims(np.array(frame_sequence), axis=0)  # (1, 14, 64, 64, 3)
        prediction = model.predict(input_data)

        # 📌 예측값 확인
        class_idx = np.argmax(prediction)
        class_name = class_labels[class_idx]
        
        # 📌 확률 값 표시
        print(f"🎯 예측 확률값: {prediction}")
        print(f"🎯 예측 클래스 인덱스: {class_idx}, 클래스명: {class_name}")

        # 📌 영상에 결과 표시
        cv2.putText(frame, class_name, (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 255), 2)

    cv2.imshow("Dog Behavior Detection", frame)

    # 'q' 키를 누르면 종료
    if cv2.waitKey(25) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
