from sklearn.datasets import load_files
from keras.utils import np_utils
import numpy as np
from glob import glob
from keras.applications.resnet50 import ResNet50
from keras.preprocessing import image
from PIL import ImageFile
from tqdm import tqdm
from keras.layers import Conv2D, MaxPooling2D, BatchNormalization, Activation, GlobalAveragePooling2D, Dropout
from keras.layers import Dropout, Flatten, Dense
from keras.models import Sequential
from keras.callbacks import ModelCheckpoint

from extract_bottleneck_features import *

ImageFile.LOAD_TRUNCATED_IMAGES = True


# 加载数据集
def load_dataset(path):
    data = load_files(path)
    dog_files = np.array(data['filenames'])
    dog_targets = np_utils.to_categorical(np.array(data['target']), 133)
    print(dog_files)
    return dog_files, dog_targets


test_files, test_targets = load_dataset('inference/test')

# print(test_targets)
