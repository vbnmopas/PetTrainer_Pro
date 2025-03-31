"""
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv3D, MaxPooling3D, Flatten, Dense, Dropout
from tensorflow.keras.optimizers import Adam

# 모델 정의
model = Sequential()

# Conv3D Layer 1
model.add(Conv3D(32, kernel_size=(3, 3, 3), activation='relu', input_shape=(14, 64, 64, 3)))
model.add(MaxPooling3D(pool_size=(2, 2, 2)))

# Conv3D Layer 2
model.add(Conv3D(64, kernel_size=(3, 3, 3), activation='relu'))
model.add(MaxPooling3D(pool_size=(2, 2, 2)))

# Flatten Layer
model.add(Flatten())

# Dense Layer
model.add(Dense(64, activation='relu'))

# Dropout Layer
model.add(Dropout(0.5))

# Output Layer (2 classes for sitting or lying down)
model.add(Dense(2, activation='softmax'))

# 모델 컴파일
model.compile(optimizer=Adam(), loss='sparse_categorical_crossentropy', metrics=['accuracy'])

# 모델 요약 출력
model.summary()

# 모델 학습
history = model.fit(train_data, train_labels, epochs=10, batch_size=32, validation_split=0.2)

# 모델 학습 과정 출력
print(history.history)

# 모델 평가
# 예시로 테스트 데이터를 사용하고 있다면
# test_data, test_labels = <테스트 데이터 로드>
# model.evaluate(test_data, test_labels)

# 학습된 모델 저장
model.save('dog_behavior_model.h5')

from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv3D, MaxPooling3D, Flatten, Dense, Dropout

model = Sequential()

# Conv3D 및 MaxPooling3D 계층 추가
model.add(Conv3D(32, kernel_size=(3, 3, 3), activation='relu', input_shape=(10, 64, 64, 3)))
model.add(MaxPooling3D(pool_size=(2, 2, 2)))

model.add(Conv3D(64, kernel_size=(3, 3, 3), activation='relu'))
model.add(MaxPooling3D(pool_size=(2, 2, 2)))

model.add(Flatten())

# Dense 레이어 추가
model.add(Dense(64, activation='relu'))
model.add(Dropout(0.5))  # 과적합 방지를 위한 드롭아웃 추가

# 출력층: 다중 클래스 분류 (두 클래스)
model.add(Dense(2, activation='softmax'))  # 두 클래스의 확률 분포를 출력

# 모델 컴파일 (다중 클래스 분류용)
model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

# 모델 요약 출력
model.summary()
"""
import os
import numpy as np
from tensorflow.keras.preprocessing.image import load_img, img_to_array
from tensorflow.keras.preprocessing.sequence import pad_sequences
from sklearn.model_selection import train_test_split
from sklearn.utils.class_weight import compute_class_weight
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv3D, MaxPooling3D, Flatten, Dense, Dropout, LSTM, TimeDistributed
from tensorflow.keras.optimizers import Adam

# 📌 1️⃣ 데이터 로드 및 전처리
def load_images_from_folder(folder, img_size=(64, 64)):
    images = []
    for filename in sorted(os.listdir(folder)):  
        if filename.endswith('.jpg') or filename.endswith('.png'):
            img_path = os.path.join(folder, filename)
            img = load_img(img_path, target_size=img_size)  
            img_array = img_to_array(img) / 255.0  # 정규화
            images.append(img_array)
    return images

def create_sequence_data(base_folder, img_size=(64, 64)):
    image_data = []
    labels = []
    max_seq_length = 0

    for foldername, _, _ in os.walk(base_folder):
        label_file = os.path.join(foldername, f"{os.path.basename(foldername)}.txt")
        if not os.path.exists(label_file):
            continue
        
        with open(label_file, 'r') as file:
            label_values = file.readline().strip().split()
            label = int(label_values[0]) 
        
        images = load_images_from_folder(foldername, img_size=img_size)
        max_seq_length = max(max_seq_length, len(images))

        if len(images) > 0:
            image_data.append(images)
            labels.append(label)

    if len(image_data) == 0:
        print("데이터가 없습니다. 경로를 확인하세요.")
        return np.array([]), np.array([])

    image_data = pad_sequences(image_data, maxlen=max_seq_length, dtype='float32', padding='post', truncating='post', value=0)
    labels = np.array(labels)

    return image_data, labels, max_seq_length

# 데이터 경로
dataset_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/labels"
train_data, train_labels, max_seq_length = create_sequence_data(dataset_path)

print(f"훈련 데이터 크기: {train_data.shape}")
print(f"훈련 레이블 크기: {train_labels.shape}")

# 데이터 분할
x_train, x_val, y_train, y_val = train_test_split(train_data, train_labels, test_size=0.2, random_state=42)

# 클래스 가중치 계산
class_weights = compute_class_weight(class_weight="balanced", classes=np.unique(train_labels), y=train_labels)
class_weight_dict = {i: class_weights[i] for i in range(len(class_weights))}

print(f"📌 클래스 가중치: {class_weight_dict}")

# 📌 2️⃣ 모델 생성 및 학습
model = Sequential()
model.add(Conv3D(32, kernel_size=(3, 3, 3), activation='relu', input_shape=(max_seq_length, 64, 64, 3)))
model.add(MaxPooling3D(pool_size=(2, 2, 2)))
model.add(Conv3D(64, kernel_size=(3, 3, 3), activation='relu'))
model.add(MaxPooling3D(pool_size=(2, 2, 2)))
model.add(TimeDistributed(Flatten()))
model.add(LSTM(64, return_sequences=False))
model.add(Dense(64, activation='relu'))
model.add(Dropout(0.5))
model.add(Dense(2, activation='softmax'))  

# 모델 컴파일
model.compile(optimizer=Adam(learning_rate=0.0005), loss='sparse_categorical_crossentropy', metrics=['accuracy'])
model.summary()

# 모델 학습
history = model.fit(x_train, y_train, epochs=20, batch_size=32, validation_data=(x_val, y_val), class_weight=class_weight_dict)

# 학습된 모델 저장
model.save('dog_behavior_model_v2.h5')

# 학습 결과 평가
train_loss, train_acc = model.evaluate(x_train, y_train)
val_loss, val_acc = model.evaluate(x_val, y_val)

print(f"✅ 훈련 데이터 정확도: {train_acc:.4f}, 손실: {train_loss:.4f}")
print(f"✅ 검증 데이터 정확도: {val_acc:.4f}, 손실: {val_loss:.4f}")
