#! coding=utf-8

import requests

http_pxc_write_node_url = 'http://dba.corp.xxxxxx.com:6500/prod/pxc/%s/write_node'
http_pxc_all_nodes_url = 'http://dba.corp.xxxxxx.com:6500/prod/pxc/%s/nodes'

namespace = 'hotel_mice'


def get_nodes(http_url, namespace):
	http_req_url = http_url % (namespace)
	resp = requests.get(http_req_url)
	data = resp.json()
	if 0 != data['ret']:
		raise requests.HTTPError
	return data['data']


def get_wirte_nodes(namespace):
	nodes_data = get_nodes(http_pxc_write_node_url, namespace)
	id_port = [(d['ip'], d['port'])for d in nodes_data if 'online' == d['online_status']]
	return id_port


def get_read_nodes(namespace):
	nodes_data = get_nodes(http_pxc_all_nodes_url, namespace)
	id_port = [(d['ip'], d['port']) for d in nodes_data if 'online' == d['online_status'] and 'read' == d['role_flag']]
	return id_port


def get_available_read_nodes(namespace):
	res = get_read_nodes(namespace)
	if res and len(res) > 0:
		return res
	else:
		res = get_wirte_nodes(namespace)
		return res