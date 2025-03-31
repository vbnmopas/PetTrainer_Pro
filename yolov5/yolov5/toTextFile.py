############# 파일명 그대로 (.mp4붙은건 제거 후) txt로 변환
"""
import os
import json
from glob import glob

# JSON 파일이 있는 디렉토리
json_dir = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/backup/SIT"
output_dir = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/SIT_dataset/SIT"

# 출력 폴더 생성
os.makedirs(output_dir, exist_ok=True)

# JSON 파일 목록 가져오기
json_files = glob(os.path.join(json_dir, "*.json"))

for json_file in json_files:
    with open(json_file, "r", encoding="utf-8") as f:
        data = json.load(f)

    # JSON 파일명에서 .mp4만 제거 (기존 파일명 유지)
    base_filename = os.path.basename(json_file).replace(".mp4.json", "").replace(".json", "")

    # 변환된 YOLO 라벨 저장 경로
    label_file_path = os.path.join(output_dir, f"{base_filename}.txt")

    with open(label_file_path, "w", encoding="utf-8") as txt_file:
        for annotation in data["annotations"]:
            bbox = annotation.get("bounding_box", None)
            if bbox:
                # YOLO 형식 변환
                img_width = data["metadata"]["width"]
                img_height = data["metadata"]["height"]
                
                x_center = (bbox["x"] + bbox["width"] / 2) / img_width
                y_center = (bbox["y"] + bbox["height"] / 2) / img_height
                width = bbox["width"] / img_width
                height = bbox["height"] / img_height
                
                # YOLO 클래스 (sit=0, bodylower=1)
                class_id = 0 if "sit" in base_filename else 1
                
                # TXT 파일 작성
                txt_file.write(f"{class_id} {x_center:.6f} {y_center:.6f} {width:.6f} {height:.6f}\n")
    
    print(f"✅ 변환 완료: {label_file_path}")

print("🎉 모든 JSON 파일 변환 완료!")
"""
###텍스트파일명과 같은 폴더(폴더안에 이미지파일)로 이동 - 폴더명에 .mp4 붙어있는건 따로 해야됨됨
import os
import shutil
from glob import glob

# 원본 텍스트 파일이 있는 디렉토리
source_dir = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/SIT_dataset/SIT"
# 대상 폴더가 위치한 기본 디렉토리
target_base_dir = "C:/PetTrainer_Pro/yolov5/yolov5/data/dataset_dog/SIT"

# 텍스트 파일 목록 가져오기
txt_files = glob(os.path.join(source_dir, "*.txt"))

for txt_file in txt_files:
    # 파일명에서 확장자 제거하여 폴더명 생성
    file_name = os.path.basename(txt_file).replace(".txt", "")
    target_folder = os.path.join(target_base_dir, file_name)

    # 폴더가 존재하는지 확인 후 이동
    if os.path.exists(target_folder):
        shutil.move(txt_file, os.path.join(target_folder, os.path.basename(txt_file)))
        print(f"✅ 이동 완료: {txt_file} → {target_folder}")
    else:
        print(f"⚠️ 폴더 없음 (생략): {target_folder}")

print("🎉 모든 파일 이동 완료!")
