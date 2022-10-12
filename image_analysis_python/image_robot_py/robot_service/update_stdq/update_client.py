# -*- coding: UTF-8 -*-
import os
import shutil
import sys
import time
from datetime import datetime
from tempfile import NamedTemporaryFile

import config
import robot_service.common.logger as logger

business_logger = logger.BusinessLogger()

query_all_sql_dict = {
    'hotel': '''-- 酒店 hotel
select
    distinct tag_name as stdq
from tag_config where sys_code=1005 and tag_show_type=7 and yn=1 and tag_pid!=0 and channel in ("user", "common")
order by update_time;
''',
    'hotel_agent': '''-- 酒店 hotel 客服侧
select
    distinct tag_name as stdq
from tag_config where sys_code=1005 and tag_show_type=7 and yn=1 and tag_pid!=0 and channel in ("agent", "common")
order by update_time;
''',
    'ticket': '''-- 门票 ticket
select
    distinct tag_name as stdq
from tag_config where sys_code=6000 and tag_show_type=7 and yn=1 and tag_pid!=0
order by update_time;
''',
    'travel': '''-- 度假 travel
select
    distinct tag_name as stdq
from tag_config where sys_code=4000 and tag_show_type=7 and yn=1 and tag_pid!=0
order by update_time;
''',
    'common': '''-- 小驼 common
select
    distinct tag_name as stdq
from tag_config
where sys_code in(9000,1005,6000,4000) and tag_show_type=7 and yn=1 and tag_pid!=0
order by update_time;
''',
}


def write_to_temp_file(lines, dir_name):
    with NamedTemporaryFile('w+t', dir=dir_name, suffix='.' + datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + '.tmp',
                            delete=False, encoding='utf-8') as f:
        temp_file = f.name
        business_logger.info('write to temp file: %s' % temp_file)
        for line in lines:
            f.write(line.strip() + '\n')
        return temp_file


def pull_all_stdq():
    """拉取最新的所有标准Q"""
    dn_agent = config.stdq_db_agent
    conn = dn_agent.get_connect()
    cursor = conn.cursor()
    try:
        for biz, data_config in config.models_config['data'].items():
            sql = query_all_sql_dict[biz]
            cursor.execute(sql)
            lines = [row[0] for row in cursor.fetchall()]

            assert len(lines) > 0

            # 写入临时文件
            dir_name = os.path.dirname(data_config['match_stdq'])
            temp_file = write_to_temp_file(lines=lines, dir_name=dir_name)
            # 复制临时文件为正式文件, 并删除临时文件
            shutil.copy(temp_file, data_config['match_stdq'])
            shutil.copy(temp_file, data_config['guess_stdq'])
            os.remove(temp_file)  # 删除临时文件
    except Exception as e:
        business_logger.error('[pull_all_stdq] 查询标准问题时失败, biz=%s, sql=%s' % (biz, sql))
        raise e
    finally:
        conn.close()


def reload_stdq_embedding():
    """重新对标准Q编码"""
    # # 使用 GPU, 调试用
    # os.environ['CUDA_DEVICE_ORDER'] = 'PCI_BUS_ID'
    # os.environ['CUDA_VISIBLE_DEVICES'] = ''

    # 只有这三个模型需要提前对 stdq 列表编码

    # 更新 embedding
    business_logger.info('[update_model_stdq] 开始更新标准Q embedding')
    import robot_service.sbert as sbert
    for client in sbert.clients.values():
        client.embedding_stdq()
    business_logger.info('[update_model_stdq] sbert 模型标准Q embedding 更新完成')
    import robot_service.dssm as dssm
    for client in dssm.clients.values():
        client.embedding_stdq()
    business_logger.info('[update_model_stdq] dssm 模型标准Q embedding 更新完成')
    business_logger.info('[update_model_stdq] 标准Q embedding 更新完成')


if __name__ == '__main__':
    # pull_all_stdq()
    start_time = time.time()
    reload_stdq_embedding()
    print('=' * 100)
    print('first time used: ', time.time() - start_time)

    start_time = time.time()
    reload_stdq_embedding()
    print('=' * 100)
    print('second time used: ', time.time() - start_time)
