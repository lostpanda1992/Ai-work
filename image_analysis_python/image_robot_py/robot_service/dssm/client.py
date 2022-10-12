# coding: utf-8

"""
Created on Tue Feb 25 14:36:32 2020

@author: mengqil.li
"""
import time

import numpy as np
import tensorflow as tf

import config
from robot_service.common import logger
from robot_service.common import tf_check
from robot_service.dssm import dssm_model

# os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
logger = logger.BusinessLogger()


class DssmClient:

    def __init__(self, model_path, conf: dssm_model.Config):
        self.model_path = model_path
        self.stdq_path = conf.stdq_path
        self.conf = conf

        # 标准问题编码字典
        self.stand_q_encode_map_new = dict()
        self.stand_q_text_map_new = dict()
        self.stdq_list = []
        self.stdq_embedding = None

        self.sess = None
        self.model = None

        self.load_sess()
        self.embedding_stdq()

    def load_sess(self):
        # 加载模型参数
        graph = tf.Graph()  # 通过设置不同的graph实现同时导入多个模型
        sess = tf.Session(graph=graph)
        # 使用GPU多个tf模型同时加载时,有显存冲突
        gpu_device_list = tf_check.get_available_gpus()
        # my_device = gpu_device_list[0] if len(gpu_device_list) > 0 else '/device:CPU:0'
        # with tf.device(my_device):
        with graph.as_default():
            self.model = dssm_model.DssmModel(self.conf)
            saver = tf.train.Saver()
            sess.run(tf.initialize_all_variables())
            model_file = tf.train.latest_checkpoint(self.model_path)
            saver.restore(sess, model_file)
            self.sess = sess

    def close(self):
        if self.sess is not None:
            self.sess.close()

    def feed_dict(self, query):
        query_in = [dssm_model.convert_word2id(query, self.conf)]
        doc_positive_in = [[]]
        doc_negative_in = [[]]
        query_len = len(query_in)
        pos_seq_len = [self.conf.max_seq_len] * query_len
        neg_seq_len = [self.conf.max_seq_len] * query_len
        query_seq_len = [self.conf.max_seq_len] * query_len

        return {
            self.model.query_batch: query_in,
            self.model.doc_pos_batch: doc_positive_in,
            self.model.doc_neg_batch: doc_negative_in,
            self.model.query_seq_length: query_seq_len,
            self.model.neg_seq_length: neg_seq_len,
            self.model.pos_seq_length: pos_seq_len,
            self.model.on_train: False,
            self.model.drop_out_prob: 1.,
        }

    def get_embedding(self, text):
        embedding = self.sess.run(self.model.query_rnn_output, feed_dict=self.feed_dict(text))[0]
        return embedding

    def embedding_stdq(self):
        # 编码标准问题
        embedding_list = []
        stdq_list_tmp = []
        idx = 0
        with open(self.stdq_path, 'r', encoding='utf8') as fin:
            for line in fin:
                stdq = line.strip()
                if stdq == '':
                    continue
                stdq_list_tmp.append(stdq)
                embedding = self.get_embedding(stdq)
                embedding_list.append(embedding)
                idx += 1
        if len(embedding_list) == len(stdq_list_tmp) and len(embedding_list) > 0:
            self.stdq_embedding = np.array(embedding_list)  # n*200
            self.stdq_list = stdq_list_tmp


    def predict(self, query, topk):
        start = time.time()
        query_embedding = self.get_embedding(query)
        time_used = time.time() - start
        logger.info('dssm get_embedding time_used: %.6f' % time_used)

        # 计算 cos 距离
        query_mat = query_embedding[np.newaxis, :]  # 1*200
        inner_dot = np.dot(query_mat, self.stdq_embedding.T)  # 1*n  点乘结果
        norm = np.linalg.norm(query_embedding) * np.linalg.norm(self.stdq_embedding, axis=1)  # n  二范数乘积
        dis_cos = np.divide(inner_dot, norm)  # 1*n  cos 距离

        # 取出 cos 距离最大的(cos距离越大,相似性越大)
        dis_cos = dis_cos.reshape((-1))
        result_idx = np.argsort(dis_cos)
        stdq_list = []
        score_list = []
        for no in range(min(topk, len(result_idx))):
            idx = int(result_idx[-no - 1])
            question = self.stdq_list[idx]
            score = dis_cos[idx]
            stdq_list.append(question)
            score_list.append(score)
        return stdq_list, score_list


def init_clients():
    '''
    初始化各业务模型客户端
    :return:
    '''
    common_data = config.models_config['data']
    dssm_config = config.models_config['dssm']
    clients = {}
    for business in dssm_config.keys():
        conf = dssm_model.Config(
            vocab_path=dssm_config[business]['vocab_path'],
            stdq_path=common_data[business]['guess_stdq'])
        model_path = dssm_config[business]['model_path']
        clients[business] = DssmClient(model_path=model_path, conf=conf)

        logger.info('dssm 模型初始化成功, biz=%s' % (business))
    return clients


if __name__ == '__main__':
    # # 单个模型测试
    # stdq_path = 'data/hotel/standQ_355.txt'
    # vocab_path = 'models/dssm/hotel/vocab.txt'
    # model_path = 'models/dssm/hotel'
    # conf = dssm_model.Config(vocab_path, stdq_path)
    # client = DssmClient(model_path, conf)
    #
    # topk = 10
    # line = '付款'
    #
    # prob = client.predict(line, topk)
    # print(prob)

    # 一组模型测试
    clients = init_clients()
    topk = 3
    line = '付款'

    prob = clients['hotel'].predict(line, topk)
    print('hotel', prob, sep='\n')

    # prob = clients['flight'].predict(line, topk)
    # print('flight', prob, sep='\n')
    #
    # prob = clients['train'].predict(line, topk)
    # print('train', prob, sep='\n')
