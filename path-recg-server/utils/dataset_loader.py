"""
获取数据
"""
import warnings
import os

from tqdm import tqdm

warnings.filterwarnings("ignore")
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"

from keras.preprocessing import image
import numpy as np
from PIL import ImageFile
from sklearn.datasets import load_files
from keras.utils import np_utils
import numpy as np

ImageFile.LOAD_TRUNCATED_IMAGES = True


class ImageAndName:
    def __init__(self, _name, _img):
        self.name = _name
        self.img = _img


# 加载数据集
def load_dataset(path, size=2):
    data = load_files(path)
    files = np.array(data['filenames'])
    targets = np_utils.to_categorical(np.array(data['target']), size)
    return files, targets


# 加载图片为tensor
def path_to_tensor(img_path):
    img = image.load_img(img_path, target_size=(224, 224))
    x = image.img_to_array(img)
    res = np.expand_dims(x, axis=0)
    return res


def paths_to_tensor(img_paths):
    list_of_tensors = [path_to_tensor(img_path) for img_path in tqdm(img_paths)]
    return np.vstack(list_of_tensors)


def folder_to_tensor_objects(img_path):
    image_files = []
    for root, dirs, files in os.walk(img_path):
        for file in files:
            if file.endswith(".png") and root == img_path:
                image_files.append(img_path + "/" + file)
    images = []
    for img in image_files:
        images.append(ImageAndName(img.split("/")[-1].split(".")[0], path_to_tensor(img)))
    return images


def load_tag(tag_path):
    tags = {}
    with open(tag_path + "/tag.txt") as f:
        tag_number = 1
        for i in f.readlines():
            tags[tag_number] = int(i)
            tag_number = tag_number + 1
    return tags


# def get_data():
#     res = folder_to_tensor_objects("../inference/train/image")
#     tags = load_tag("../inference/train/tags")
#     return res, tags

def get_data(path="../inference/train"):
    origin_data, origin_tags = load_dataset(path)
    data = [path_to_tensor(i) for i in origin_data]
    tags = [np.expand_dims(i, axis=0) for i in origin_tags]
    return data, tags


def get_single_img(path):
    return path_to_tensor(path)

# if __name__ == '__main__':
#     data, tags = get_data()
#     print(tags)
