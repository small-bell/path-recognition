import warnings

warnings.filterwarnings("ignore")
import os

os.environ["TF_CPP_MIN_LOG_LEVEL"] = "2"

from bottleneck_feature import *
from PTHNet import *

if __name__ == '__main__':
    imgs, tags = do_feature()
    net = PTHNet()
    for idx, img in enumerate(imgs):
        net.fit(img, tags[idx])
    net.save_model("../weights/pthnet.h5")