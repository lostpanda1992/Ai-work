# -*- coding: UTF-8 -*-
import numpy as np
import tensorflow as tf


def load_vocab(file_path):
    word_dict = {}
    with open(file_path, encoding='utf8') as f:
        for idx, word in enumerate(f.readlines()):
            word = word.strip()
            word_dict[word] = idx
    return word_dict


class Config(object):
    def __init__(self, vocab_path, stdq_path):
        self.vocab_path = vocab_path
        self.stdq_path = stdq_path

        self.vocab_map = load_vocab(self.vocab_path)

        self.nwords = len(self.vocab_map)
        self.unk = '[UNK]'
        self.pad = '[PAD]'

        self.max_seq_len = 20
        self.hidden_size_rnn = 100
        self.use_stack_rnn = False


def convert_word2id(query, conf):
    ids = []
    for w in query:
        if w in conf.vocab_map:
            ids.append(conf.vocab_map[w])
        else:
            ids.append(conf.vocab_map[conf.unk])
    while len(ids) < conf.max_seq_len:
        ids.append(conf.vocab_map[conf.pad])
    return ids[:conf.max_seq_len]


def convert_seq2bow(query, conf):
    bow_ids = np.zeros(conf.nwords)
    for w in query:
        if w in conf.vocab_map:
            bow_ids[conf.vocab_map[w]] += 1
        else:
            bow_ids[conf.vocab_map[conf.unk]] += 1
    return bow_ids


class DssmModel:
    def __init__(self, conf):
        # 超参数
        TRIGRAM_D = 100
        # negative sample
        NEG = 4
        # query batch size
        query_BS = 100

        # #####################################################################
        # 定义模型结构
        with tf.name_scope('input'):
            # 预测时只用输入query即可，将其embedding为向量。
            self.query_batch = tf.placeholder(tf.int32, shape=[None, None], name='query_batch')
            self.doc_pos_batch = tf.placeholder(tf.int32, shape=[None, None], name='doc_positive_batch')
            self.doc_neg_batch = tf.placeholder(tf.int32, shape=[None, None], name='doc_negative_batch')
            self.query_seq_length = tf.placeholder(tf.int32, shape=[None], name='query_sequence_length')
            self.pos_seq_length = tf.placeholder(tf.int32, shape=[None], name='pos_seq_length')
            self.neg_seq_length = tf.placeholder(tf.int32, shape=[None], name='neg_sequence_length')
            self.on_train = tf.placeholder(tf.bool)
            self.drop_out_prob = tf.placeholder(tf.float32, name='drop_out_prob')

        with tf.name_scope('word_embeddings_layer'):
            _word_embedding = tf.get_variable(name="word_embedding_arr", dtype=tf.float32,
                                              shape=[conf.nwords, TRIGRAM_D])
            query_embed = tf.nn.embedding_lookup(_word_embedding, self.query_batch, name='query_batch_embed')
            doc_pos_embed = tf.nn.embedding_lookup(_word_embedding, self.doc_pos_batch, name='doc_positive_embed')
            doc_neg_embed = tf.nn.embedding_lookup(_word_embedding, self.doc_neg_batch, name='doc_negative_embed')

        with tf.name_scope('RNN'):

            if conf.use_stack_rnn:
                cell_fw = tf.contrib.rnn.GRUCell(conf.hidden_size_rnn, reuse=tf.AUTO_REUSE)
                stacked_gru_fw = tf.contrib.rnn.MultiRNNCell([cell_fw], state_is_tuple=True)
                cell_bw = tf.contrib.rnn.GRUCell(conf.hidden_size_rnn, reuse=tf.AUTO_REUSE)
                stacked_gru_bw = tf.contrib.rnn.MultiRNNCell([cell_fw], state_is_tuple=True)
                (output_fw, output_bw), (_, _) = tf.nn.bidirectional_dynamic_rnn(stacked_gru_fw, stacked_gru_bw)
                # not ready, to be continue ...
            else:
                cell_fw = tf.contrib.rnn.GRUCell(conf.hidden_size_rnn, reuse=tf.AUTO_REUSE)
                cell_bw = tf.contrib.rnn.GRUCell(conf.hidden_size_rnn, reuse=tf.AUTO_REUSE)
                # query
                (_, _), (query_output_fw, query_output_bw) = tf.nn.bidirectional_dynamic_rnn(cell_fw, cell_bw,
                                                                                             query_embed,
                                                                                             sequence_length=self.query_seq_length,
                                                                                             dtype=tf.float32)
                query_rnn_output = tf.concat([query_output_fw, query_output_bw], axis=-1)
                query_rnn_output = tf.nn.dropout(query_rnn_output, self.drop_out_prob)  # 是这儿吗？
                self.query_rnn_output = query_rnn_output

                # doc_pos
                (_, _), (doc_pos_output_fw, doc_pos_output_bw) = tf.nn.bidirectional_dynamic_rnn(cell_fw, cell_bw,
                                                                                                 doc_pos_embed,
                                                                                                 sequence_length=self.pos_seq_length,
                                                                                                 dtype=tf.float32)
                doc_pos_rnn_output = tf.concat([doc_pos_output_fw, doc_pos_output_bw], axis=-1)
                doc_pos_rnn_output = tf.nn.dropout(doc_pos_rnn_output, self.drop_out_prob)

                # doc_neg
                (_, _), (doc_neg_output_fw, doc_neg_output_bw) = tf.nn.bidirectional_dynamic_rnn(cell_fw, cell_bw,
                                                                                                 doc_neg_embed,
                                                                                                 sequence_length=self.neg_seq_length,
                                                                                                 dtype=tf.float32)
                doc_neg_rnn_output = tf.concat([doc_neg_output_fw, doc_neg_output_bw], axis=-1)
                doc_neg_rnn_output = tf.nn.dropout(doc_neg_rnn_output, self.drop_out_prob)

        with tf.name_scope('Merge_Negative_Doc'):
            # 合并负样本，tile可选择是否扩展负样本。
            doc_y = tf.tile(doc_pos_rnn_output, [1, 1])
            for i in range(NEG):
                for j in range(query_BS):
                    doc_y = tf.concat([doc_y, tf.slice(doc_neg_rnn_output, [j * NEG + i, 0], [1, -1])], 0)

        # 定义模型结构
        # #####################################################################
