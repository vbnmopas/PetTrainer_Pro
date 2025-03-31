import os
import cv2
import numpy as np
import tensorflow as tf
from tensorflow.keras.preprocessing.image import img_to_array
from ultralytics import YOLO
from tensorflow.keras.preprocessing.sequence import pad_sequences

# 📌 1️⃣ COCO YOLOv5 모델 로드 (강아지 감지용)
yolo_model = YOLO("yolov5s.pt")  # COCO Pre-trained YOLO 모델

# 📌 2️⃣ 강아지 행동 인식 모델 로드
behavior_model = tf.keras.models.load_model("dog_behavior_model_v3.h5")  # 학습한 모델

# 📌 3️⃣ 이미지 시퀀스 전처리 함수
def preprocess_sequence(image_list, img_size=(64, 64), max_seq_length=30):
    sequence = []
    for img in image_list:
        img = cv2.resize(img, img_size)  # 크기 조정
        img_array = img_to_array(img) / 255.0  # 정규화
        sequence.append(img_array)

    sequence = pad_sequences([sequence], maxlen=max_seq_length, dtype='float32', padding='post', truncating='post', value=0)
    return np.array(sequence)

# 📌 4️⃣ 강아지 행동 인식 실행 함수
def detect_dog_behavior(video_path):
    cap = cv2.VideoCapture(video_path)
    frame_list = []  # 행동 분석을 위한 프레임 저장

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
        
        # 강아지 감지
        results = yolo_model(frame)
        for result in results:
            boxes = result.boxes
            for box in boxes:
                if int(box.cls) == 16:  # COCO에서 "강아지" 클래스 ID (16)
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    score = box.conf[0].item()  # 신뢰도 점수 추출
                    dog_crop = frame[y1:y2, x1:x2]  # 강아지 부분 잘라내기
                    
                    # 바운딩 박스 그리기
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)  # 초록색으로 바운딩 박스 그리기
                    
                    # 강아지 신뢰도 텍스트 추가
                    label = f"Dog: {score:.2f}"  # 신뢰도 표시
                    cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

                    frame_list.append(dog_crop)

        # 행동 분석 후 프레임에 텍스트 추가
        if len(frame_list) > 0:
            input_sequence = preprocess_sequence(frame_list)
            prediction = behavior_model.predict(input_sequence)
            predicted_class = np.argmax(prediction)

            behavior = "SIT" if predicted_class == 0 else "BODYLOWER"
            cv2.putText(frame, f"Behavior: {behavior}", (20, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 2)  # 행동 텍스트 추가

        # 프레임 출력
        cv2.imshow("Dog Behavior Detection", frame)

        # 'q'를 눌러서 비디오를 종료
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

# 실행 예시 (테스트용 영상 파일 경로 입력)
detect_dog_behavior("C:/testimage/sitDog5.mp4")
