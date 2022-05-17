from flask import Flask, request
from server.utils.RC4Util import encrypt
import time
from models.PTHNet import *
from models.bottleneck_feature import *

net = PTHNet()
net.load_model("../weights/pthnet.h5")

app = Flask(__name__)


@app.route("/upload", methods=["POST"])
def upload():
    image = request.files.get("file")
    file_name = encrypt(request.remote_addr
                        + str(request.environ.get('REMOTE_PORT')))
    file_name = str(file_name)[2:-1] + '-' + str(int(time.time())) + '.png'
    if image is None:
        return "文件上传为空"

    image.save("upload/" + file_name)
    res = net.test_one("upload/" + file_name)
    return {
        'res': str(res)
    }


if __name__ == '__main__':
    app.run(host="127.0.0.1", port=8000, debug=True)
