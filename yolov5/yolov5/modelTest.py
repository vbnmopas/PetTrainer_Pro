
"""
from tensorflow.keras.models import load_model
import cv2
import numpy as np

# 저장된 모델 로드
model = load_model('dog_behavior_model.h5')

def preprocess_video(video_path, img_size=(64, 64), sequence_length=14):
    # 비디오 파일 열기
    cap = cv2.VideoCapture(video_path)
    
    frames = []
    
    while True:
        ret, frame = cap.read()
        if not ret:
            break  # 비디오 끝에 도달
        
        # 영상 크기 조정 및 모델에 맞는 형식으로 변환
        frame_resized = cv2.resize(frame, img_size)
        frame_array = np.array(frame_resized)
        frames.append(frame_array)
    
    cap.release()

    # 영상 시퀀스를 numpy 배열로 변환
    frames = np.array(frames)

    # 14개의 연속된 프레임을 하나의 시퀀스로 묶음
    sequences = []
    for i in range(len(frames) - sequence_length + 1):
        sequences.append(frames[i:i + sequence_length])

    sequences = np.array(sequences)
    
    return sequences

# 예시: 비디오 파일을 처리하여 프레임을 얻어옴
video_path = 'C:/testimage/bodylowerDog.mp4'
video_frames = preprocess_video(video_path)
print(f"영상 프레임 크기: {video_frames.shape}")
print(f"영상 프레임 값: {video_frames}")

# 예측 수행
predictions = model.predict(video_frames)

# 예측된 클래스 확인
for i, prediction in enumerate(predictions):
    predicted_class = np.argmax(prediction)  # 0: sit, 1: lying
    print(f"시퀀스 {i}에서의 예측: {'앉기' if predicted_class == 0 else '누워있기'}")



import cv2
import numpy as np  # numpy 임포트

video_path = "C:/capstonimage/dog.bmp"  # 비디오 경로 지정
cap = cv2.VideoCapture(video_path)

if not cap.isOpened():
    print("비디오 파일을 열 수 없습니다. 경로를 확인하세요.")
else:
    print("비디오 파일이 성공적으로 열렸습니다.")

frames = []
while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break
    frames.append(frame)

cap.release()
frames = np.array(frames)
print(f"영상 프레임 크기: {frames.shape}")
print(f"영상 프레임 값: {video_frames}")

"""
#훈련 데이터 정확도: 1.0000, 손실: 0.0000
#검증 데이터 정확도: 0.9744, 손실: 0.0475

"""
import os
import numpy as np
from tensorflow.keras.preprocessing.image import load_img, img_to_array
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv3D, MaxPooling3D, Flatten, Dense, Dropout, BatchNormalization
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.callbacks import EarlyStopping
from sklearn.model_selection import train_test_split

# 🟢 데이터 로드 함수
def load_images_from_folder(folder, img_size=(64, 64)):
    images = []
    for filename in os.listdir(folder):
        if filename.endswith('.jpg') or filename.endswith('.png'):
            img_path = os.path.join(folder, filename)
            img = load_img(img_path, target_size=img_size)  # 크기 조정
            img_array = img_to_array(img)
            images.append(img_array)
    return images

def create_sequence_data(base_folder, img_size=(64, 64)):
    image_data = []
    labels = []
    max_seq_length = 0  # 가장 긴 시퀀스 찾기

    for foldername, _, _ in os.walk(base_folder):
        label_file = os.path.join(foldername, f"{os.path.basename(foldername)}.txt")
        if not os.path.exists(label_file):
            continue

        with open(label_file, 'r') as file:
            label = int(file.readline().strip().split()[0])  # 첫 번째 값을 레이블로 사용

        images = load_images_from_folder(foldername, img_size=img_size)
        max_seq_length = max(max_seq_length, len(images))

        if len(images) > 0:
            image_data.append(images)
            labels.append(label)

    if len(image_data) == 0:
        print("훈련 데이터가 없습니다. 데이터 경로를 확인하세요.")
        return np.array([]), np.array([])

    # 패딩 적용
    image_data = pad_sequences(image_data, maxlen=max_seq_length, dtype='float32', padding='post', truncating='post', value=0)
    labels = np.array(labels)

    return image_data, labels

# 🟢 데이터 로드 및 분할
dataset_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/labels"
train_data, train_labels = create_sequence_data(dataset_path)

# 데이터 분할 (훈련:검증 = 8:2)
x_train, x_val, y_train, y_val = train_test_split(train_data, train_labels, test_size=0.2, random_state=42)

# 클래스 분포 출력
unique, counts = np.unique(y_train, return_counts=True)
print(f"훈련 데이터 분포: {dict(zip(unique, counts))}")

unique, counts = np.unique(y_val, return_counts=True)
print(f"검증 데이터 분포: {dict(zip(unique, counts))}")

# 🟢 모델 정의
model = Sequential()

# Conv3D Layer 1
model.add(Conv3D(32, kernel_size=(3, 3, 3), activation='relu', input_shape=(x_train.shape[1], 64, 64, 3)))
model.add(MaxPooling3D(pool_size=(2, 2, 2)))
model.add(BatchNormalization())  # 배치 정규화 적용
model.add(Dropout(0.3))

# Conv3D Layer 2
model.add(Conv3D(64, kernel_size=(3, 3, 3), activation='relu'))
model.add(MaxPooling3D(pool_size=(2, 2, 2)))
model.add(BatchNormalization())
model.add(Dropout(0.4))

# Flatten Layer
model.add(Flatten())

# Dense Layer
model.add(Dense(128, activation='relu'))  # 64 → 128 변경
model.add(Dropout(0.5))

# Output Layer (2 classes: Sitting or Lying)
model.add(Dense(2, activation='softmax'))

# 🟢 모델 컴파일 (학습률 0.0003 조정)
model.compile(optimizer=Adam(learning_rate=0.0003), loss='sparse_categorical_crossentropy', metrics=['accuracy'])

# 모델 요약
model.summary()

# 🟢 Early Stopping 설정
early_stopping = EarlyStopping(monitor='val_loss', patience=5, restore_best_weights=True)

# 🟢 모델 학습
history = model.fit(x_train, y_train, epochs=30, batch_size=32, validation_data=(x_val, y_val), callbacks=[early_stopping])

# 학습 과정 출력
print(history.history)

# 🟢 모델 평가
train_loss, train_acc = model.evaluate(x_train, y_train)
val_loss, val_acc = model.evaluate(x_val, y_val)

print(f"훈련 데이터 정확도: {train_acc:.4f}, 손실: {train_loss:.4f}")
print(f"검증 데이터 정확도: {val_acc:.4f}, 손실: {val_loss:.4f}")

# 모델 저장
model.save('dog_behavior_model.h5')
"""

import cv2
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import img_to_array
from collections import deque  # 시퀀스 데이터를 저장할 큐

# 📌 모델 로드

# 📌 클래스 라벨 (0: 앉기, 1: 눕기)
class_labels = {0: "Sitting", 1: "Lying"}

# 📌 웹캠 열기 (0: 기본 카메라)
cap = cv2.VideoCapture(0)

# 📌 시퀀스 데이터를 저장할 큐 (최근 14개 프레임 저장)q
frame_sequence = deque(maxlen=14)

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        print("❌ 카메라를 불러올 수 없습니다.")
        break

    # 📌 프레임 크기 조정 (64x64로 변경)
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

        print(f"🔍 예측 확률값: {prediction[0]}")
        print(f"🎯 예측 클래스 인덱스: {class_idx}, 클래스명: {class_name}")

        # 📌 영상에 결과 표시 (노란색)
        cv2.putText(frame, class_name, (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 255), 2)

    cv2.imshow("Dog Behavior Detection - Real-time", frame)

    # 'q' 키를 누르면 종료
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()


