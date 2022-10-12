# -*- coding: utf-8 *-*
import os
from typing import Optional, Awaitable

import tornado.web


class HealthCheckHandler(tornado.web.RequestHandler):
    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        raise NotImplementedError()

    def get(self):
        (res, info) = self.check()
        if res:
            self.write(info)
        else:
            raise info

    def post(self):
        (res, info) = self.check()
        if res:
            self.write(info)
        else:
            raise info

    def head(self):
        self.write('ok')

    def check(self):
        if os.path.exists('healthcheck.html'):
            return True, "ok"
        else:
            return False, tornado.web.HTTPError(404, 'Page Not Found Error!!')