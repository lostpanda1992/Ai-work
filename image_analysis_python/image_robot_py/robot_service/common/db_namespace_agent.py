# -*- coding: UTF-8 -*-

# for python3

import json
from urllib import request
import random
import pymysql


# 获取qmha的 ip/port
# http://wiki.corp.xxxxxx.com/confluence/pages/viewpage.action?pageId=103752295
# 获取pxc的 ip/port
# https://wiki.corp.xxxxxx.com/confluence/pages/viewpage.action?pageId=105573352

class PyMysqlIpPort:
    def __init__(self, host, port, user_name, password, dbname):
        self.host = host
        self.port = port
        self._username = user_name
        self._password = password
        self._dbname = dbname

        self.conn = None

    def _reconnect(self):
        if self.conn is not None:
            self.conn.close()
        self.conn = pymysql.connect(host=self.host, port=self.port,
                                    user=self._username, passwd=self._password,
                                    database=self._dbname,
                                    charset='utf8')

    def get_connect(self):
        """主要对外接口,获取数据库连接"""
        if self.conn is None:
            self._reconnect()
        try:
            self.conn.ping()
        except Exception:
            self._reconnect()

        return self.conn


class PyMysqlPXC:
    """
    pymysql 的 pxc 连接.
    可预见的问题: 对 conn 的配置信息在自动重连发生后没有恢复.
    比如 conn.autocommit() 之类的配置.
    建议: 每次获取 conn 之后都进行自己的配置
    """
    write_query_dict = {
        'dev': 'http://dba.corp.xxxxxx.com:6500/beta/pxc/%s/write_node',
        'beta': 'http://dba.corp.xxxxxx.com:6500/beta/pxc/%s/write_node',
        'prod': 'http://dba.corp.xxxxxx.com:6500/prod/pxc/%s/write_node',
    }
    all_query_dict = {
        'dev': 'http://dba.corp.xxxxxx.com:6500/beta/pxc/query/namespace?namespace=%s',
        'beta': 'http://dba.corp.xxxxxx.com:6500/beta/pxc/query/namespace?namespace=%s',
        'prod': 'http://dba.corp.xxxxxx.com:6500/prod/pxc/%s/nodes',
    }

    @staticmethod
    def get_node(url_template, namespace):
        """获取 qmha 节点"""
        query_url = url_template % namespace
        response_obj = request.urlopen(query_url)
        response_str = response_obj.read().decode('utf-8')
        response = json.loads(response_str)

        # 请求失败
        if response['ret'] != 0:
            # 获取连接信息失败
            raise ConnectionError()

        conn_info_list = response['data']
        return conn_info_list

    def get_write_node(self):
        """随机获取一个可用的 qmha 写节点的 ip/端口"""
        conn_info_list = PyMysqlQMHA.get_node(
            PyMysqlPXC.write_query_dict[self.env], self._namespace)
        ip_port_list = [(node['ip'], node['port'])
                        for node in conn_info_list if node['online_status'] == 'online']
        return random.sample(ip_port_list, 1)[0]

    def get_read_node(self):
        """随机获取一个可用的 qmha 读节点的 ip/port"""
        conn_info_list = PyMysqlQMHA.get_node(
            PyMysqlPXC.all_query_dict[self.env], self._namespace)
        ip_port_list = [(node['ip'], node['port']) for node in conn_info_list if
                        node['online_status'] == 'online' and node['role_flag'] == 'read']
        return random.sample(ip_port_list, 1)[0]

    def __init__(self, namespace, user_name, password, dbname, conn_type='write', env='beta'):
        self._namespace = namespace
        self._username = user_name
        self._password = password
        self._dbname = dbname
        self._conn_type = conn_type
        self.env = env

        self.conn = None

    def _reconnect(self):
        """重新获取连接"""
        if self._conn_type == 'write':
            ip, port = self.get_write_node()
        else:
            ip, port = self.get_read_node()

        if self.conn is not None:
            self.conn.close()
        self.conn = pymysql.connect(host=ip, port=port,
                                    user=self._username, passwd=self._password,
                                    database=self._dbname,
                                    charset='utf8')

    def get_connect(self):
        """主要对外接口,获取数据库连接"""
        if self.conn is None:
            self._reconnect()
        try:
            self.conn.ping()
        except Exception:
            self._reconnect()

        return self.conn


class PyMysqlQMHA:
    """
    pymysql 的 qmha 连接.
    可预见的问题: 对 conn 的配置信息在自动重连发生后没有恢复.
    比如 conn.autocommit() 之类的配置.
    建议: 每次获取 conn 之后都进行自己的配置
    """
    write_query_dict = {
        'dev': 'http://dba.corp.xxxxxx.com:6500/beta/qmha/%s/write_node',
        'beta': 'http://dba.corp.xxxxxx.com:6500/beta/qmha/%s/write_node',
        'prod': 'http://dba.corp.xxxxxx.com:6500/prod/qmha/%s/write_node',
    }
    all_query_dict = {
        'dev': 'http://dba.corp.xxxxxx.com:6500/beta/qmha/query/namespace?namespace=%s',
        'beta': 'http://dba.corp.xxxxxx.com:6500/beta/qmha/query/namespace?namespace=%s',
        'prod': 'http://dba.corp.xxxxxx.com:6500/prod/qmha/%s/nodes',
    }

    @staticmethod
    def get_node(url_template, namespace):
        """获取 qmha 节点"""
        query_url = url_template % namespace
        response_obj = request.urlopen(query_url)
        response_str = response_obj.read().decode('utf-8')
        response = json.loads(response_str)

        # 请求失败
        if response['ret'] != 0:
            # 获取连接信息失败
            raise ConnectionError()

        conn_info_list = response['data']
        return conn_info_list

    def get_write_node(self):
        """随机获取一个可用的 qmha 写节点的 ip/端口"""
        conn_info_list = PyMysqlQMHA.get_node(
            PyMysqlQMHA.write_query_dict[self.env], self._namespace)
        ip_port_list = [(node['ip'], node['port'])
                        for node in conn_info_list if node['online_status'] == 'online']
        return random.sample(ip_port_list, 1)[0]

    def get_read_node(self):
        """随机获取一个可用的 qmha 读节点的 ip/port"""
        conn_info_list = PyMysqlQMHA.get_node(
            PyMysqlQMHA.all_query_dict[self.env], self._namespace)
        ip_port_list = [(node['ip'], node['port']) for node in conn_info_list if
                        node['online_status'] == 'online' and node['role_flag'] == 'read']
        return random.sample(ip_port_list, 1)[0]

    def __init__(self, namespace, user_name, password, dbname, conn_type='write', env='beta'):
        self._namespace = namespace
        self._username = user_name
        self._password = password
        self._dbname = dbname
        self._conn_type = conn_type
        self.env = env

        self.conn = None

    def _reconnect(self):
        """重新获取连接"""
        if self._conn_type == 'write':
            ip, port = self.get_write_node()
        else:
            ip, port = self.get_read_node()

        if self.conn is not None:
            self.conn.close()
        self.conn = pymysql.connect(host=ip, port=port,
                                    user=self._username, passwd=self._password,
                                    database=self._dbname,
                                    charset='utf8')

    def get_connect(self):
        """主要对外接口,获取数据库连接"""
        if self.conn is None:
            self._reconnect()
        try:
            self.conn.ping()
        except Exception:
            self._reconnect()

        return self.conn


def main():
    """测试"""
    # qmha prod
    name_space = 'dba_callcenter'
    all_node_list = PyMysqlQMHA.get_node(
        PyMysqlQMHA.write_query_dict['prod'], name_space)
    print(all_node_list)
    all_node_list = PyMysqlQMHA.get_node(
        PyMysqlQMHA.all_query_dict['prod'], name_space)
    print(all_node_list)

    # qmha beta
    name_space = 'noah47423_hotel_feed_stream_9669f'
    all_node_list = PyMysqlQMHA.get_node(
        PyMysqlQMHA.write_query_dict['beta'], name_space)
    print(all_node_list)
    all_node_list = PyMysqlQMHA.get_node(
        PyMysqlQMHA.all_query_dict['beta'], name_space)
    print(all_node_list)

    stdq_db_agent = PyMysqlPXC(
        namespace='tc_801_pxc',
        user_name='h_algo_robot_r', password='AT6d8zenqb30NoZW',
        dbname='order_complain',
        conn_type='read', env='prod')
    stdq_db_agent.get_connect()


if __name__ == "__main__":
    main()
