from utils.features_utils import *
from utils.dataset_loader import *


def do_feature(path="../inference/train"):
    imgs, tags = get_data(path)

    data = []
    for idx, img in enumerate(imgs):
        feature = extract_Resnet50(img)
        data.append(feature)
    return data, tags


def do_single_feature(path):
    img = get_single_img(path)
    return extract_Resnet50(img)
