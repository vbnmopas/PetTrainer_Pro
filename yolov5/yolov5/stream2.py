import cv2
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import img_to_array
from collections import deque
from tensorflow.keras.preprocessing.sequence import pad_sequences
from ultralytics import YOLO
from flask import Flask, Response

app = Flask(__name__)

# 📌 웹캠 스트리밍 주소 (MJPEG 스트리밍 URL)
# IP_CAMERA_URL = "http://192.168.180.228:8080/video"  # MJPEG 스트리밍 경로
IP_CAMERA_URL = cv2.VideoCapture(0)


# 📌 YOLO 모델 (강아지 감지용)
yolo_model = YOLO("yolov5s.pt")  # COCO Pre-trained YOLO 모델

# 📌 강아지 행동 인식 모델
behavior_model = load_model("dog_behavior_model_v2.h5")

# 📌 클래스 라벨 (0: 앉기, 1: 눕기)
class_labels = {0: "Sitting", 1: "BodyLower"}

# 📌 웹캠 열기 (MJPEG 스트리밍 URL)
cap = cv2.VideoCapture(IP_CAMERA_URL)

# 📌 시퀀스 데이터를 저장할 큐 (최근 14개 프레임 저장)
frame_sequence = deque(maxlen=14)
frame_buffer = deque(maxlen=30)  # 강아지 영역을 위한 버퍼

def preprocess_sequence(image_list, img_size=(64, 64), max_seq_length=30):
    sequence = [cv2.resize(img, img_size) / 255.0 for img in image_list]
    sequence = pad_sequences([sequence], maxlen=max_seq_length, dtype='float32', padding='post', truncating='post', value=0)
    return np.array(sequence)

def generate_frames():
    while True:
        success, frame = cap.read()  # MJPEG 스트리밍에서 프레임 읽기
        if not success:
            break

        # 📌 YOLO로 강아지 감지
        results = yolo_model(frame)
        dog_detected = False  # 강아지가 감지되었는지 여부를 추적

        for result in results:
            for box in result.boxes:
                if int(box.cls) == 16:  # COCO에서 강아지 클래스 ID (16은 강아지)
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    dog_crop = frame[y1:y2, x1:x2]  # 강아지 영역 자르기
                    frame_buffer.append(dog_crop)
                    dog_detected = True  # 강아지가 감지되었음을 표시

                    # 📌 강아지 영역에 바운딩 박스 그리기
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)  # 초록색 박스

        # 📌 강아지가 감지되었을 경우에만 행동 예측 수행
        if dog_detected and len(frame_buffer) == 30:  # 30개 프레임이 모이면 예측 수행
            input_sequence = preprocess_sequence(list(frame_buffer))
            prediction = behavior_model.predict(input_sequence)
            predicted_class = np.argmax(prediction)  # 0 = 앉기, 1 = 눕기

            # 예측된 행동
            behavior = class_labels[predicted_class]

            # 📌 행동 텍스트 표시
            cv2.putText(frame, behavior, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 255, 255), 2)

        # 📌 강아지가 감지되지 않았다면 행동 인식을 하지 않음
        elif not dog_detected:
            cv2.putText(frame, "No dog detected", (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)

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
