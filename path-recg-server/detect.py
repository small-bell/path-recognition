from models.PTHNet import *
from models.bottleneck_feature import *


TEST_PATH = "inference/test/1/1.png"
if __name__ == '__main__':
    net = PTHNet()
    # net = load_model("weights/pthnet.h5")
    net.load_model("weights/pthnet.h5")
    res = net.test_one(TEST_PATH)
    print(np.argmax(res))