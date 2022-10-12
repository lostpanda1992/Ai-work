#! coding=utf-8

import time
import os
import numpy as np
import xgboost as xgb

from robot_service.common import logger

business_logger = logger.BusinessLogger()

local_path = os.path.dirname(os.path.realpath(__file__))


class XgbInitRecomModel(object):
    """
    xgboost model predict topN standard questions for init_recommend
    """

    def __init__(self, model_file, clsid_to_id_file, feature_dim=15):
        self.model_file = model_file
        self.feature_dim = feature_dim
        self.xgb_model = xgb.Booster()
        self.clsid_to_bid = self.load_id_pairs(clsid_to_id_file)

    def load_id_pairs(self, id_pairs_file):
        id1_to_id2 = {}
        with open(id_pairs_file, 'r') as f:
            for line in f:
                id1, id2 = line.splitlines()[0].split('###')
                id1_to_id2[int(id1)] = id2
        return id1_to_id2

    def load_weights(self):
        self.xgb_model.load_model(self.model_file)

    def predict(self, feature):
        feature = xgb.DMatrix(np.array(feature))
        pred = self.xgb_model.predict(feature)
        return pred

