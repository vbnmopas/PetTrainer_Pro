"""
#이미지에 대한 라벨 파일이 존재하는지 확인하는 코드드
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
            
            # 이미지에 해당하는 라벨이 존재하는지 확인
            if os.path.exists(label_file_path):
                image_files.append(img_file_path)
                label_files.append(label_file_path)
            else:
                print(f"라벨 파일 누락: {label_file_path} (이미지: {img_file_path})")

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
"""
"""
# 유효한 라벨 파일인지 확인하는 코드
import os

# 이미지와 라벨 경로
image_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/sit_dataset/SIT"
label_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/sit_dataset/labels"

# 이미지 파일 리스트
image_files = []
for root, dirs, files in os.walk(image_path):
    for file in files:
        if file.endswith(".jpg"):
            image_files.append(os.path.join(root, file))

# 라벨 파일이 유효한지 확인하는 함수
def validate_label(label_file):
    if not os.path.exists(label_file):
        print(f"라벨 파일 없음: {label_file}")
        return False
    
    # 라벨 파일이 비어있는지 확인
    with open(label_file, "r") as f:
        lines = f.readlines()
        if len(lines) == 0:
            print(f"라벨 파일 비어 있음: {label_file}")
            return False

    # 라벨 파일 내용의 유효성 검사 (예: 클래스가 올바른 범위인지 등)
    for line in lines:
        parts = line.strip().split()
        if len(parts) != 5:
            print(f"라벨 파일 포맷 오류: {label_file}")
            return False
        
        # 클래스 번호는 정수여야 하고, 이미지 내에서의 비율 값이 0~1 사이여야 함
        try:
            class_id = int(parts[0])
            if class_id < 0:  # 클래스 ID가 음수인 경우 오류
                print(f"잘못된 클래스 ID: {label_file}")
                return False
        except ValueError:
            print(f"클래스 ID가 정수가 아님: {label_file}")
            return False

    return True

# 이미지와 라벨 파일 일치 확인
for img_file in image_files:
    # 이미지에 해당하는 라벨 파일 경로
    label_file = img_file.replace(image_path, label_path).replace(".jpg", ".txt")
    
    # 라벨 파일이 유효한지 검사
    if not validate_label(label_file):
        print(f"유효하지 않은 라벨 파일: {label_file}")
    else:
        print(f"유효한 라벨 파일: {label_file}")
"""
import os

def get_folder_from_label(label_name, base_path):
    """
    라벨 파일명을 기준으로 해당하는 폴더를 찾는 함수
    예: dog-bodylower-009620.txt -> 'dog-bodylower-009620' 포함하는 폴더 찾기
    """
    for folder in os.listdir(base_path):
        if label_name in folder:
            return folder
    return None

# SIT & BODYLOWER 데이터셋 경로
sit_images_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/sit_dataset/SIT"
sit_labels_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/sit_dataset/sit_labels"

bodylower_images_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/bodylower_dataset/BODYLOWER"
bodylower_labels_path = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/bodylower_dataset/bodylower_labels"

# SIT과 BODYLOWER 데이터셋 체크 리스트
datasets = {
    "SIT": (sit_images_path, sit_labels_path),
    "BODYLOWER": (bodylower_images_path, bodylower_labels_path)
}

for name, (images_path, labels_path) in datasets.items():
    # 이미지와 라벨 파일 목록
    image_files = {os.path.splitext(f)[0] for f in os.listdir(images_path) if f.endswith(".jpg")}
    label_files = {os.path.splitext(f)[0] for f in os.listdir(labels_path) if f.endswith(".txt")}

    # 매칭 확인
    missing_labels = image_files - label_files
    missing_images = label_files - image_files

    print(f"📂 {name} 데이터셋:")
    print(f"  - 이미지 개수: {len(image_files)}")
    print(f"  - 라벨 개수: {len(label_files)}")
    
    # 매칭되지 않는 라벨을 체크
    if missing_labels:
        print(f"  ⚠ 라벨이 없는 이미지 개수: {len(missing_labels)} (예: {list(missing_labels)[:5]})")
    
    # 매칭되지 않는 이미지가 있는 라벨을 체크
    if missing_images:
        print(f"  ⚠ 이미지가 없는 라벨 개수: {len(missing_images)} (예: {list(missing_images)[:5]})")

    # 라벨 파일명과 폴더 이름 매칭 검사
    for label in label_files:
        folder = get_folder_from_label(label, images_path)
        if folder:
            print(f"  ✅ '{label}' 라벨이 '{folder}' 폴더에서 확인되었습니다.")
        else:
            print(f"  ❌ '{label}' 라벨과 일치하는 폴더를 찾을 수 없습니다.")

    print("-" * 50)
