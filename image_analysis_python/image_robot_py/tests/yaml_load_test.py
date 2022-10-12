# -*- coding: UTF-8 -*-
import os

import yaml

local_dir = os.path.dirname(os.path.realpath(__file__))
# params = yaml.load(open(os.path.join(local_dir, '../models_config.yaml'), 'r'), Loader=yaml.BaseLoader)
params = yaml.load(open(os.path.join(local_dir, 'test.yaml'), 'r'), Loader=yaml.SafeLoader)

print(type(params))
print(params)
