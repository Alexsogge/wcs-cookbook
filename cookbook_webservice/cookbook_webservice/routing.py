from channels.auth import AuthMiddlewareStack
from channels.routing import ProtocolTypeRouter, URLRouter
import api.routing
from .jwttokenauth import JwtTokenAuthMiddleware

application = ProtocolTypeRouter({
    # Empty for now (http->django views is added by default)
    'websocket': JwtTokenAuthMiddleware(
        URLRouter(
            api.routing.websocket_urlpatterns
        )
    ),
})