import os
import cv2
import numpy as np
import tensorflow as tf
from tensorflow.keras.utils import to_categorical

# 데이터 경로 설정
DATASET_PATH = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/labels"

# 모델 입력 크기 설정
SEQUENCE_LENGTH = 10  # 사용할 연속된 프레임 개수
IMAGE_SIZE = (64, 64)  # 이미지 크기 (CNN 입력용)

# 레이블 매핑 (0: 앉는 중, 1: 완전히 앉음)
LABEL_MAPPING = {"SITTING_DOWN": 0, "SITTING": 1}

# 데이터 저장 리스트
X_data, Y_data = [], []

# 1️⃣ 폴더별 데이터 로딩
for folder_name in os.listdir(DATASET_PATH):
    folder_path = os.path.join(DATASET_PATH, folder_name)

    if not os.path.isdir(folder_path):
        continue  # 폴더가 아니면 스킵

    # 2️⃣ 폴더 내 이미지 파일 정렬 후 불러오기
    frames = sorted([os.path.join(folder_path, f) for f in os.listdir(folder_path) if f.endswith(".jpg")])

    if len(frames) < SEQUENCE_LENGTH:
        continue  # 최소 SEQUENCE_LENGTH 개수보다 적으면 스킵

    # 3️⃣ 이미지 읽고 전처리
    images = []
    for frame in frames[:SEQUENCE_LENGTH]:  # SEQUENCE_LENGTH만큼만 사용
        img = cv2.imread(frame)
        img = cv2.resize(img, IMAGE_SIZE)  # 크기 변경
        img = img / 255.0  # 정규화
        images.append(img)

    # 4️⃣ numpy 배열로 변환 후 저장
    X_data.append(np.array(images))  # (10, 64, 64, 3) 형태
    label_file = os.path.join(folder_path, folder_name + ".txt")

    if os.path.exists(label_file):
        with open(label_file, "r") as f:
            label = f.readline().strip()  # 레이블 읽기
            Y_data.append(LABEL_MAPPING.get(label, 0))  # 매핑된 정수값 사용

# 5️⃣ numpy 배열 변환 및 원-핫 인코딩
X_data = np.array(X_data)  # (데이터 개수, 10, 64, 64, 3)
Y_data = np.array(Y_data)  # (데이터 개수,)
Y_data = to_categorical(Y_data, num_classes=2)  # 원-핫 인코딩

print("데이터셋 크기:", X_data.shape, Y_data.shape)  # 예: (1000, 10, 64, 64, 3), (1000, 2)
