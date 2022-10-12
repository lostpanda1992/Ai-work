#! coding=utf-8

import pymysql.cursors

import config
from image_score_service.common.hotel_mice_agent import get_available_read_nodes


class DbClient(object):
	def __init__(self, host, user, port, pwd, db):
		self.host = host
		self.user = user
		self.port = port
		self.pwd = pwd
		self.db = db
		
	def get_conn(self):
		return pymysql.connect(host=self.host,
		                       user=self.user,
		                       password=self.pwd,
		                       db=self.db,
		                       charset='utf8mb4',
		                       cursorclass=pymysql.cursors.DictCursor)
	
	def fetchall(self, sql, args=None):
		conn = self.get_conn()
		result = []
		try:
			with conn.cursor() as cursor:
				cursor.execute(sql, args)
				result = cursor.fetchall()
		except Exception as e:
			raise e
		finally:
			conn.close()
		return result
	
	def insertall(self, sql, args):
		conn = self.get_conn()
		try:
			with conn.cursor() as cursor:
				cursor.execute(sql, args)
				cursor.commit()
		except Exception as e:
			raise e
		finally:
			conn.close()
	

# client_db = DbClient()
