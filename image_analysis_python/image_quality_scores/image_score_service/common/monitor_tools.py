# -*- coding: UTF-8 -*-

import time
from functools import wraps

current_sec = lambda: int(round(time.time()))
current_msec = lambda: time.time() * 1000
current_tick = lambda x: x - x % 60

monitor_cache = {}


def api_monitor(source):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            # 更新monitor_cache
            global monitor_cache
            now = current_sec()
            cp_source = source

            if monitor_cache.get(cp_source) is None:
                monitor_cache[cp_source] = {
                    'request_count': [[0, 0], [0, 0]],
                    'fail_count': [[0, 0], [0, 0]],
                    'resptime_sum': [[0, 0], [0, 0]],
                }
            for item in monitor_cache:
                for k in ('request_count', 'fail_count', 'resptime_sum'):
                    if now - monitor_cache[item][k][1][0] > 120:
                        monitor_cache[item][k][0] = [current_tick(now) - 60, 0]
                        monitor_cache[item][k][1] = [current_tick(now), 0]
                        continue
                    if now - monitor_cache[item][k][1][0] > 60:
                        monitor_cache[item][k][0] = monitor_cache[item][k][1]
                        monitor_cache[item][k][1] = [current_tick(now), 0]
                        continue

            start = current_msec()
            result = func(*args, **kwargs)
            finish = current_msec()
            make_add(cp_source, 'resptime_sum', finish - start)
            make_add(cp_source, 'request_count', 1)
            return result
        return wrapper
    return decorator


def make_add(source, k, v):
    global monitor_cache
    monitor_cache[source][k][1][1] += v


def add_fail(source):
    make_add(source, 'fail_count', 1)
