import os
import numpy as np
from tensorflow.keras.preprocessing.image import load_img, img_to_array
from tensorflow.keras.preprocessing.sequence import pad_sequences
from sklearn.model_selection import train_test_split
from tensorflow.keras.models import load_model
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, LSTM, Flatten

# 📌 1️⃣ 기존 모델 로드
model_path = "dog_behavior_model_v3.h5"
if os.path.exists(model_path):
    model = load_model(model_path)
    print("✅ 기존 모델 로드 완료!")
else:
    raise FileNotFoundError(f"❌ 모델 파일을 찾을 수 없습니다: {model_path}")

# 📌 2️⃣ 데이터 로드 및 전처리 함수
def load_images_from_folder(folder, img_size=(64, 64)):
    images = []
    for filename in sorted(os.listdir(folder)):
        if filename.endswith(('.jpg', '.png')):
            img_path = os.path.join(folder, filename)
            img = load_img(img_path, target_size=img_size)
            img_array = img_to_array(img) / 255.0  # 정규화
            images.append(img_array)
    return images

def create_sequence_data(base_folder, img_size=(64, 64)):
    image_data = []
    labels = []
    max_seq_length = 0

    for foldername, _, filenames in os.walk(base_folder):
        label_file = os.path.join(foldername, f"{os.path.basename(foldername)}.txt")

        # 📌 `.mp4` 확장자를 제거한 텍스트 파일 매칭 처리
        if not os.path.exists(label_file):
            label_file = os.path.join(foldername, f"{os.path.basename(foldername).replace('.mp4', '')}.txt")
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
        raise ValueError(f"❌ 데이터가 없습니다. 경로를 확인하세요: {base_folder}")

    image_data = pad_sequences(image_data, maxlen=max_seq_length, dtype='float32', padding='post', truncating='post', value=0)
    labels = np.array(labels)

    return image_data, labels, max_seq_length

# 📌 3️⃣ 새로운 데이터셋 로드 (TURN 행동 추가 학습)
new_dataset_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/pTurn/TURN"
x_train_new, y_train_new, max_seq_length = create_sequence_data(new_dataset_path)
x_train, x_val, y_train, y_val = train_test_split(x_train_new, y_train_new, test_size=0.2, random_state=42)

print(f"📌 추가 학습 데이터 크기: {x_train.shape}")
print(f"📌 검증 데이터 크기: {x_val.shape}")

# 📌 4️⃣ 기존 모델의 출력 레이어 수정
# 모델의 마지막 레이어가 3개의 클래스를 분류할 수 있도록 수정 (0, 1, 2)
model.pop()  # 기존 마지막 레이어 제거
model.add(Dense(3, activation='softmax', name='output_layer'))  # 3개 클래스 출력 (앉기, 눕기, TURN), 고유 이름 지정

# 📌 5️⃣ 기존 모델 추가 학습
model.compile(optimizer=Adam(learning_rate=0.0005), loss='sparse_categorical_crossentropy', metrics=['accuracy'])

history = model.fit(
    x_train, y_train,
    epochs=10,
    batch_size=32,
    validation_data=(x_val, y_val)
)

# 📌 6️⃣ 새 모델 저장
new_model_path = "dog_behavior_model_v4.h5"
model.save(new_model_path)
print(f"✅ 새로운 모델 저장 완료: {new_model_path}")

# 📌 7️⃣ 학습 결과 평가
train_loss, train_acc = model.evaluate(x_train, y_train)
val_loss, val_acc = model.evaluate(x_val, y_val)

print(f"🎯 추가 학습 후 훈련 데이터 정확도: {train_acc:.4f}, 손실: {train_loss:.4f}")
print(f"🎯 추가 학습 후 검증 데이터 정확도: {val_acc:.4f}, 손실: {val_loss:.4f}")

# 📌 8️⃣ 모델 구조 확인
model.summary()
