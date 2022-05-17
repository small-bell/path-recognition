import warnings

from models.bottleneck_feature import do_single_feature

warnings.filterwarnings("ignore")
import os

os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"
from sklearn.datasets import load_files
from keras.utils import np_utils
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
from glob import glob
from imageio import imread
from keras.applications.resnet50 import ResNet50
from keras.preprocessing import image
from PIL import ImageFile
from tqdm import tqdm
from keras.layers import Conv2D, MaxPooling2D, BatchNormalization, Activation, GlobalAveragePooling2D, Dropout
from keras.layers import Dropout, Flatten, Dense
from keras.models import Sequential
from keras.callbacks import ModelCheckpoint
from keras.models import load_model

from keras.applications.resnet50 import preprocess_input, decode_predictions

from extract_bottleneck_features import *

ImageFile.LOAD_TRUNCATED_IMAGES = True


class PTHNet:
    def __init__(self):
        Resnet50_model = Sequential()
        Resnet50_model.add(GlobalAveragePooling2D(input_shape=(7, 7, 2048)))
        Resnet50_model.add(BatchNormalization())
        Resnet50_model.add(Dropout(0.4))
        Resnet50_model.add(Dense(2, activation='softmax'))

        Resnet50_model.summary()

        Resnet50_model.compile(loss='categorical_crossentropy',
                               optimizer='rmsprop', metrics=['accuracy'])

        self.model = Resnet50_model

    def fit(self, data, tag):
        self.model.fit(data, tag)

    def evaluate(self):
        self.model.evaluate()

    def predict(self, data):
        return self.model.predict(data)

    def load_weight(self, path):
        self.model.load_weights(path)

    def save_model(self, path):
        self.model.save(path)

    def load_model(self, path):
        self.model = load_model(path)

    def test_accuracy(self, features, test_targets):
        Resnet50_predictions = [
            np.argmax(
                self.model.predict(np.expand_dims(feature, axis=0))
            ) for feature in features
        ]
        test_accuracy = 100 * np.sum(np.array(Resnet50_predictions) == np.argmax(test_targets, axis=1)) / len(
            Resnet50_predictions)
        print('Test accuracy: %.4f%%' % test_accuracy)

    def test_one(self, path):
        feature = do_single_feature(path)
        return self.model.predict(feature)