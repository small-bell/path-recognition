from keras.utils import np_utils
import numpy as np


def one_hot(data, size):
    return np_utils.to_categorical(np.array(data), size)
