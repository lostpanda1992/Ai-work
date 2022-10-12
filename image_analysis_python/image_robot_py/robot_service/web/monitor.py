# -*-coding:utf-8-*-
import tornado.web

from robot_service.common.monitor_tools import api_monitor, monitor_cache


class QMonitorHandler(tornado.web.RequestHandler):
    @api_monitor('other')
    def get(self):
        '''
        供监控抓取的页面
        '''
        output_items = ['<pre>']
        for k, q_cache_val in monitor_cache.items():
            output_items.append('\n')
            output_items.append(k + '_request_Count=')
            output_items.append(str(q_cache_val['request_count'][0][1]))
            output_items.append('\n')
            output_items.append(k + '_fail_Count=')
            output_items.append(str(q_cache_val['fail_count'][0][1]))
            output_items.append('\n')
            output_items.append(k + '_resptime_avarage_Time=')
            output_items.append(str(
                q_cache_val['resptime_sum'][0][1] / (q_cache_val['request_count'][0][1] or 1)
            ))
        output_items.append('\n')
        output_items.append('</pre>')
        self.write(''.join(output_items))
