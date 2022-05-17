import cv2
from PIL import ImageFile
import numpy as np


def PIL_to_cv2(src_image):
    return cv2.cvtColor(np.asarray(src_image), cv2.COLOR_RGB2BGR)

