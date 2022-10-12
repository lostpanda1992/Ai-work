# -*- coding: UTF-8 -*-

import config


class ExactlySearch:

    def __init__(self):
        data_config = config.models_config['data']

        self.match_stdq = dict()
        self.guess_stdq = dict()

        for biz in data_config:
            stdq_file = data_config[biz]['match_stdq']
            with open(stdq_file, 'r', encoding='utf-8') as fin:
                for line in fin:
                    line = line.strip()
                    self.match_stdq[line] = biz

            guess_file = data_config[biz]['guess_stdq']
            with open(guess_file, 'r', encoding='utf-8') as fin:
                for line in fin:
                    line = line.strip()
                    self.guess_stdq[line] = biz

    def search(self, query, stype='match'):
        if stype == 'match':
            return self.match_stdq.get(query, None)
        elif stype == 'guess':
            return self.guess_stdq.get(query, None)
        else:
            raise ValueError('不支持的搜索场景')


search_client = ExactlySearch()
