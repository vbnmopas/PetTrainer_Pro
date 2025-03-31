"""
import torch
print("PyTorch version:", torch.__version__)
print("CUDA version:", torch.version.cuda)
print("CUDA available:", torch.cuda.is_available())
print(torch.version.cuda)

"""
"""
import os
import shutil

# 이미지 폴더 경로
image_folder = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/train/images"
# 레이블 폴더 경로
label_folder = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/train/labels"

# 이미지 폴더 내 하위 폴더들 확인
subfolders = [f for f in os.listdir(image_folder) if os.path.isdir(os.path.join(image_folder, f))]

# 하위 폴더 이름에 맞게 레이블 파일 이름을 바꾸는 작업
for subfolder in subfolders:
    # 폴더 이름에서 '20201024_' 부분을 제외한 나머지 이름을 사용 (예: 'dog-bodylower-000062')
    base_name = subfolder.split('_', 1)[-1]  # '20201024_'을 제거한 나머지 부분을 사용

    # 해당 레이블 파일 찾기
    label_filename = base_name + '.txt'  # 예: dog-bodylower-000062.txt
    label_file_path = os.path.join(label_folder, label_filename)

    # 레이블 파일이 존재하는지 확인
    if os.path.exists(label_file_path):
        new_label_filename = subfolder + '.txt'  # 폴더 이름에 맞게 레이블 파일 이름 변경
        new_label_file_path = os.path.join(label_folder, new_label_filename)
        
        # 레이블 파일 이름 변경
        shutil.move(label_file_path, new_label_file_path)
        print(f"Label file renamed: {label_filename} -> {new_label_filename}")
    else:
        print(f"No label file found for folder {subfolder}. Expected label file: {label_filename}")

"""
import os
import re
import shutil

# 경로 설정
image_root = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/bodylower_dataset/BODYLOWER"  # 이미지 폴더 (각 폴더 안에 여러 이미지)
label_root = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/bodylower_dataset/bodylower_labels"  # 레이블 파일이 있는 폴더
output_root = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/labels_x"  # 최종 저장할 폴더

# 결과 저장 폴더 생성
os.makedirs(output_root, exist_ok=True)

# 이미지 폴더 목록 가져오기
image_folders = os.listdir(image_root)

for folder_name in image_folders:
    folder_path = os.path.join(image_root, folder_name)

    if not os.path.isdir(folder_path):  # 폴더가 아닐 경우 스킵
        continue

    # "dog-sit-000273" 형식의 폴더명 추출 (mp4 확장자 제거)
    match = re.search(r"dog-bodylower-\d+", folder_name)
    if match:
        label_file_name = match.group(0) + ".txt"  # 예: dog-sit-000273.txt
        label_file_path = os.path.join(label_root, label_file_name)

        # 레이블 파일이 존재하는 경우
        if os.path.exists(label_file_path):
            # 폴더 내 모든 이미지 파일 찾기
            image_files = [f for f in os.listdir(folder_path) if f.endswith(('.jpg', '.png', '.jpeg'))]

            # 출력 폴더 생성
            folder_output = os.path.join(output_root, folder_name)
            os.makedirs(folder_output, exist_ok=True)

            # 이미지 복사
            for img in image_files:
                shutil.copy(os.path.join(folder_path, img), folder_output)

            # 레이블 파일 복사
            shutil.copy(label_file_path, folder_output)

            print(f"✅ {folder_name} → {label_file_name} 매칭 완료!")
        else:
            print(f"⚠️ {folder_name}에 해당하는 레이블 파일 없음: {label_file_name}")
    else:
        print(f"❌ {folder_name} 폴더에서 'dog-sit-xxxxxx' 패턴을 찾을 수 없음")
