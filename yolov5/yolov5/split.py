"""
import os
import random
import shutil

# 경로 설정 (사용자에 맞게 수정)
base_dir = "C:/PetTrainer_Pro/yolov5/yolov5/data/sit_dataset"
image_dir = os.path.join(base_dir, "images")  # 변환된 이미지 폴더
label_dir = os.path.join(base_dir, "labels")  # 변환된 라벨 폴더
output_dir = os.path.join(base_dir, "split_dataset")  # 데이터셋이 저장될 폴더

# train, val, test 비율
train_ratio = 0.8
val_ratio = 0.1
test_ratio = 0.1

# output 폴더 생성
for split in ["train", "val", "test"]:
    os.makedirs(os.path.join(output_dir, split, "images"), exist_ok=True)
    os.makedirs(os.path.join(output_dir, split, "labels"), exist_ok=True)

# 이미지 파일 목록 가져오기
image_files = [f for f in os.listdir(image_dir) if f.endswith('.jpg') or f.endswith('.png')]
random.shuffle(image_files)  # 랜덤 섞기

# 데이터셋 나누기
num_total = len(image_files)
num_train = int(num_total * train_ratio)
num_val = int(num_total * val_ratio)

train_files = image_files[:num_train]
val_files = image_files[num_train:num_train + num_val]
test_files = image_files[num_train + num_val:]

# 파일 복사 함수
def copy_files(file_list, split):
    for file in file_list:
        image_path = os.path.join(image_dir, file)
        label_path = os.path.join(label_dir, file.replace('.jpg', '.txt').replace('.png', '.txt'))

        if os.path.exists(label_path):  # 라벨 파일이 있는 경우에만 저장
            shutil.copy(image_path, os.path.join(output_dir, split, "images", file))
            shutil.copy(label_path, os.path.join(output_dir, split, "labels", os.path.basename(label_path)))

# 분할된 데이터 복사
copy_files(train_files, "train")
copy_files(val_files, "val")
copy_files(test_files, "test")

print(f"✅ 데이터셋 분할 완료!")
print(f"  - Train: {len(train_files)}개")
print(f"  - Val: {len(val_files)}개")
print(f"  - Test: {len(test_files)}개")
"""


## YOLO 학습을 위해 train/val/test 폴더를 자동으로 정리
## 이미지와 라벨을 80:10:10 비율로 분할
## 파일을 올바른 폴더로 이동 (shutil.move)
## 분할된 데이터 개수 출력

import os
import shutil
import random

# 데이터셋 경로
dataset_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/sit_dataset"
image_path = os.path.join(dataset_path, "SIT")  # SIT 폴더 경로
label_path = os.path.join(dataset_path, "labels")  # labels 폴더 경로

# Train, Val, Test 폴더 경로
train_img_path = os.path.join(dataset_path, "train/images")
train_label_path = os.path.join(dataset_path, "train/labels")
val_img_path = os.path.join(dataset_path, "val/images")
val_label_path = os.path.join(dataset_path, "val/labels")
test_img_path = os.path.join(dataset_path, "test/images")
test_label_path = os.path.join(dataset_path, "test/labels")

# 폴더 없으면 생성
for path in [train_img_path, train_label_path, val_img_path, val_label_path, test_img_path, test_label_path]:
    os.makedirs(path, exist_ok=True)

# 모든 dog-sit-XXXX 폴더의 이미지 파일 리스트 가져오기
image_files = []
label_files = []
for root, dirs, files in os.walk(image_path):  # SIT 폴더 내 하위 디렉토리들 탐색
    for file in files:
        if file.endswith(".jpg"):  # .jpg 파일만 선택
            img_file_path = os.path.join(root, file)
            label_file_path = os.path.join(label_path, os.path.relpath(img_file_path, image_path).replace(".jpg", ".txt"))
            image_files.append(img_file_path)
            label_files.append(label_file_path)

# 데이터 섞기 (랜덤)
data = list(zip(image_files, label_files))
random.shuffle(data)

# 데이터셋 분할 (80% Train, 10% Val, 10% Test)
num_train = int(len(data) * 0.8)
num_val = int(len(data) * 0.1)

train_data = data[:num_train]
val_data = data[num_train:num_train + num_val]
test_data = data[num_train + num_val:]

# 파일 이동 함수
def move_files(data, dst_img_dir, dst_label_dir):
    for img_src, label_src in data:
        # 이미지 및 라벨 파일의 상대 경로를 계산
        img_rel_path = os.path.relpath(img_src, image_path)
        label_rel_path = os.path.relpath(label_src, label_path)

        # 대상 경로
        img_dst = os.path.join(dst_img_dir, img_rel_path)
        label_dst = os.path.join(dst_label_dir, label_rel_path)

        # 대상 폴더가 없다면 생성
        os.makedirs(os.path.dirname(img_dst), exist_ok=True)
        os.makedirs(os.path.dirname(label_dst), exist_ok=True)

        # 이미지 파일 이동
        shutil.move(img_src, img_dst)

        # 라벨 파일 이동
        shutil.move(label_src, label_dst)

# 데이터 이동
move_files(train_data, train_img_path, train_label_path)
move_files(val_data, val_img_path, val_label_path)
move_files(test_data, test_img_path, test_label_path)

# 결과 출력
print(f"데이터셋 분할 완료!")
print(f"Train: {len(train_data)}개, Val: {len(val_data)}개, Test: {len(test_data)}개")
