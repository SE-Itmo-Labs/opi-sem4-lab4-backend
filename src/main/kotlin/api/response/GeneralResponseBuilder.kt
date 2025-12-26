package api.response

import jakarta.json.Json
import jakarta.json.JsonArray
import jakarta.json.JsonObject
import jakarta.json.JsonObjectBuilder

open class GeneralResponseBuilder {

    protected open val body: JsonObjectBuilder = Json.createObjectBuilder()

    open fun add(title : String, message: String) {
        body.add(title, message)
    }

    open fun add(title : String, message : JsonObject) {
        body.add(title, message)
    }

    open fun add(title : String, message : Int) {
        body.add(title, message) // Kostyl
    }

    open fun add(title : String, message : JsonArray) {
        body.add(title, message)
    }

    open fun ok(message : String) : JsonObject {
        addStatus("ok")
        addMessage(message)
        return build()
    }

    open fun error(message: String) : JsonObject {
        addStatus("error")
        addMessage(message)
        return build()
    }

    override fun toString(): String {
        return build().toString()
    }

    protected open fun addStatus(status : String) {
        body.add("status", status)
    }

    protected open fun addMessage(message : String) {
        body.add("message", message)
    }

    open fun build() : JsonObject {
        return body.build()
    }
}