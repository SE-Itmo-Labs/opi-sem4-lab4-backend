package api.configs

import jakarta.websocket.HandshakeResponse
import jakarta.websocket.server.HandshakeRequest
import jakarta.websocket.server.ServerEndpointConfig

class AuthConfig : ServerEndpointConfig.Configurator() {
    override fun modifyHandshake(
        conf: ServerEndpointConfig,
        request: HandshakeRequest,
        response: HandshakeResponse
    ) {
        super.modifyHandshake(conf, request, response)
    }
}