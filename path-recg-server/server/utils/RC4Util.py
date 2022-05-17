from Crypto.Cipher import AES
from binascii import b2a_hex, a2b_hex


def add_to_16(text):
    if len(text.encode('utf-8')) % 16:
        add = 16 - (len(text.encode('utf-8')) % 16)
    else:
        add = 0
    text = text + ('\0' * add)
    return text.encode('utf-8')


# 加密函数
def encrypt(text, key="8E5EC1AC2191167DF9B753BA93A1E7B8", iv="0102030405060708"):
    mode = AES.MODE_CBC
    text = add_to_16(text)
    cryptos = AES.new(key=key.encode('utf-8'), mode=mode, iv=iv.encode('utf-8'))
    cipher_text = cryptos.encrypt(text)
    return b2a_hex(cipher_text)


def decrypt(text, key, iv):
    mode = AES.MODE_CBC
    cryptos = AES.new(key=key.encode('utf-8'), mode=mode, iv=iv.encode('utf-8'))
    plain_text = cryptos.decrypt(a2b_hex(text))
    return bytes.decode(plain_text).rstrip('\0')


# if __name__ == '__main__':
#     e = encrypt("127.0.0.1:9995", "8E5EC1AC2191167DF9B753BA93A1E7B8", "0102030405060708")
#     d = decrypt(e, "8E5EC1AC2191167DF9B753BA93A1E7B8", "0102030405060708")
#     print("加密:", e)
#     print("解密:", d)
