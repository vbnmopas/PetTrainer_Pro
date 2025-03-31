###웹캠스트리밍영상을 받아 dog_behavior_model_v3.h5 모델로 행동 인식
import os
import cv2
import time
import numpy as np
import tensorflow as tf
from flask import Flask, request, jsonify, Response
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.preprocessing.sequence import pad_sequences
from ultralytics import YOLO
from collections import deque, Counter
import json  # JSON 변환을 위한 모듈 추가

app = Flask(__name__)

# 📌 YOLO 모델 (강아지 감지용)
yolo_model = YOLO("yolov5s.pt")  # COCO Pre-trained YOLO 모델

# 📌 강아지 행동 인식 모델
behavior_model = tf.keras.models.load_model("dog_behavior_model_v3.h5")

# 📌 시퀀스 전처리 함수
def preprocess_sequence(image_list, img_size=(64, 64), max_seq_length=30):
    sequence = [cv2.resize(img, img_size) / 255.0 for img in image_list]
    sequence = pad_sequences([sequence], maxlen=max_seq_length, dtype='float32', padding='post', truncating='post', value=0)
    return np.array(sequence)

# 📌 10초 동안 행동 인식 수행 함수
def recognize_behavior(command, video_url="http://192.168.180.228:81/stream"):
    cap = cv2.VideoCapture(video_url)  # 비디오 스트리밍 URL로 변경
    frame_buffer = []
    start_time = time.time()

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
        
        # YOLO로 강아지 감지
        results = yolo_model(frame)
        for result in results:
            for box in result.boxes:
                if int(box.cls) == 16:  # COCO에서 강아지 클래스 ID
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    dog_crop = frame[y1:y2, x1:x2]  # 강아지 영역 자르기
                    frame_buffer.append(dog_crop)

        # 10초 지나면 종료
        if time.time() - start_time > 10:
            break

    cap.release()

    # 📌 프레임이 부족하면 실패 처리
    if len(frame_buffer) == 0:
        return "인식 실패", False

    # 📌 행동 예측 수행
    input_sequence = preprocess_sequence(frame_buffer)
    prediction = behavior_model.predict(input_sequence)
    predicted_class = np.argmax(prediction)  # 0 = 앉기, 1 = 엎드리기

    behavior = "앉기" if predicted_class == 0 else "엎드리기"

    # 📌 명령과 비교 후 성공 여부 판단
    success = (behavior == command)
    result_msg = "✅ 행동 성공!" if success else "❌ 행동 실패!"

    print(f"현재 행동: {behavior}")
    print(f"✅ 최종 판단: {behavior} (명령: {command}) → {result_msg}")

    return result_msg, success

# 📌 Flask API 엔드포인트
@app.route('/send', methods=['POST'])
def receive_message():
    data = request.get_json()
    command = data.get("message", "")
    print(f"📩 받은 명령: {command}")

    result_msg, success = recognize_behavior(command)

    # 📌 JSON 응답으로 앱에 결과 보내기
    # ✅ JSON 응답에서 ensure_ascii=False 적용!
    response_data = {
        "status": "success",
        "command": command,
        "result": "성공" if success else "실패",
        "message": result_msg
    }

    # ✅ `json.dumps()` 사용해서 ensure_ascii=False 적용
    response_json = json.dumps(response_data, ensure_ascii=False)

    return Response(response_json, content_type="application/json; charset=utf-8"), 200

if __name__ == '__main__':
    app.run(host='0:0:0:0', port=5000, debug=True)


