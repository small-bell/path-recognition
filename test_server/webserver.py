from flask import Flask, request
import time


app = Flask(__name__)


@app.route("/upload", methods=["POST"])
def upload():
    image = request.files.get("file")
    print(image)
    image.save("test.png")
    if image is None:
        return "null"

    return {
        'res': '123'
    }

@app.route('/')
def hello_world():
	return 'Hello World'


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=8000, debug=True)
