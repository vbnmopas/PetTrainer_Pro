import torch
import cv2
import numpy as np
from ultralytics import YOLO

# COCO 데이터셋에서 학습된 YOLOv5 모델 로드
yolo_model = YOLO("yolov5s.pt")  # 또는 "yolov5m.pt", "yolov5l.pt" 사용 가능

# 테스트할 이미지 경로 설정
image_path = "C:/testimage/sitDog.png"  # 여기에 사용할 이미지 경로 입력

# 이미지 로드
image = cv2.imread(image_path)
if image is None:
    raise FileNotFoundError(f"이미지를 찾을 수 없습니다: {image_path}")

# YOLOv5 모델을 사용하여 객체 탐지 수행
results = yolo_model(image)

# COCO 데이터셋에서 강아지의 클래스 ID (16번)
dog_class_id = 16

# 탐지된 객체를 출력
for result in results:
    boxes = result.boxes.xyxy.cpu().numpy()  # 바운딩 박스 좌표
    scores = result.boxes.conf.cpu().numpy()  # 신뢰도 점수
    classes = result.boxes.cls.cpu().numpy()  # 클래스 ID

    for box, score, cls in zip(boxes, scores, classes):
        if int(cls) == dog_class_id:  # 강아지일 경우
            x1, y1, x2, y2 = map(int, box)
            cv2.rectangle(image, (x1, y1), (x2, y2), (0, 255, 0), 2)
            label = f"Dog: {score:.2f}"
            cv2.putText(image, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

# 결과 이미지 저장 및 출력
output_path = "output.jpg"
cv2.imwrite(output_path, image)
cv2.imshow("Dog Detection", image)
cv2.waitKey(0)
cv2.destroyAllWindows()
