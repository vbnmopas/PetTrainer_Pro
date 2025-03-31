from flask import Flask, Response
import cv2
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import img_to_array
from collections import deque  # 시퀀스 데이터를 저장할 큐

app = Flask(__name__)

# 📌 모델 로드 (모델 파일 경로 수정)
model = load_model("dog_behavior_model_v3.h5")

# 📌 클래스 라벨 (0: 앉기, 1: 눕기)
class_labels = {0: "Sitting", 1: "Lying"}

# 📌 웹캠 열기
cap = cv2.VideoCapture(0)

# 📌 시퀀스 데이터를 저장할 큐 (최근 14개 프레임 저장)
frame_sequence = deque(maxlen=14)

def generate_frames():
    while True:
        success, frame = cap.read()
        if not success:
            break

        # 📌 프레임 크기 조정 (64x64)
        img = cv2.resize(frame, (64, 64))
        img = img_to_array(img).astype('float32') / 255.0  # 정규화
        frame_sequence.append(img)  # 시퀀스에 추가

        # 📌 14개 프레임이 모이면 예측 수행
        if len(frame_sequence) == 14:
            input_data = np.expand_dims(np.array(frame_sequence), axis=0)  # (1, 14, 64, 64, 3)
            prediction = model.predict(input_data)

            # 📌 예측값 확인
            class_idx = np.argmax(prediction[0])  # 가장 높은 확률을 가진 클래스의 인덱스
            class_name = class_labels[class_idx]

            # 📌 영상에 결과 표시 (노란색)
            # cv2.putText(frame, class_name, (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 255), 2)

        # 📌 프레임을 JPEG로 인코딩 후 스트리밍
        _, buffer = cv2.imencode('.jpg', frame)
        frame = buffer.tobytes()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')

@app.route('/video')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == "__main__":
    app.run(host='192.168.0.23', port=5000, debug=False)
    