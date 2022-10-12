# -*- coding: UTF-8 -*-

from Crypto import Random
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_v1_5
import base64
import urllib.parse
import urllib

# rsa算法生成实例
def get_key():
    rsa = RSA.generate(1024, Random.new().read)
    # master的秘钥对的生成
    private_pem = rsa.exportKey()
    public_pem = rsa.publickey().exportKey()

    return {
        'public_key': public_pem.decode(),
        'private_key': private_pem.decode(),
    }


# 用公钥加密
def rsa_encode(message, public_key) -> str:
    rsakey = RSA.importKey(public_key)  # 导入读取到的公钥
    cipher = PKCS1_v1_5.new(rsakey)  # 生成对象
    # 通过生成的对象加密message明文，注意，在python3中加密的数据必须是bytes类型的数据，不能是str类型的数据
    cipher_text = base64.b64encode(cipher.encrypt(message.encode(encoding='utf-8')))
    # 公钥每次加密的结果不一样跟对数据的padding（填充）有关
    return cipher_text.decode()


# 用私钥解密
def rsa_decode(cipher_text, private_key) -> str:
    rsakey = RSA.importKey(private_key)  # 导入读取到的私钥
    cipher = PKCS1_v1_5.new(rsakey)  # 生成对象
    # 将密文解密成明文，返回的是一个bytes类型数据，需要自己转换成str
    text = cipher.decrypt(base64.b64decode(cipher_text), b'ERROR')
    return text.decode(encoding='utf-8', errors='strict')


if __name__ == '__main__':
    # key_pair = get_key()
    # print('public_key', key_pair['public_key'], sep='\n')
    # print('\n\n')
    # print('private_key', key_pair['private_key'], sep='\n')
    #
    # message = "你好，世界！hello. world"
    # cipher = rsa_encode(message, key_pair['public_key'])
    # print('\n\n')
    # print('加密结果', type(cipher), cipher, sep='\n')
    #
    # decipher = rsa_decode(cipher, key_pair['private_key'])
    # print('\n\n')
    # print('解密结果', type(decipher), decipher, sep='\n')

    import os
    import config

    with open(os.path.join(config.project_dir, 'apiKey', 'update_private_key'), 'r') as fin:
        update_api_private_key = fin.read()
    with open(os.path.join(config.project_dir, 'apiKey', 'update_public_key'), 'r') as fin:
        update_api_public_key = fin.read()

    # message = '你好，世界！hello. world'
    message = 'all'
    cipher = rsa_encode(message, update_api_public_key)
    url = urllib.parse.quote(cipher)
    print('\n\n')
    print('加密结果', type(cipher), cipher, sep='\n')
    print('urlencode', type(url), url, sep='\n')

    cipher = urllib.parse.unquote(url)
    decipher = rsa_decode(cipher, update_api_private_key)
    print('\n\n')
    print('解密结果', type(decipher), decipher, sep='\n')
