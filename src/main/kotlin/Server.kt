import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.*
import java.io.*
import java.nio.file.Files

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)
    server.createContext("/", FileHandler())
    server.start()
}

class FileHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        when (exchange.requestMethod) {
            "GET" -> handleGet(exchange)
            "POST" -> handlePost(exchange)
            "PUT" -> handlePut(exchange)
            "DELETE" -> handleDelete(exchange)
            "COPY" -> handleCopy(exchange)
            "MOVE" -> handleMove(exchange)
            else -> {
                exchange.sendResponseHeaders(405, -1)
                exchange.close()
            }
        }
    }

    private fun handleGet(exchange: HttpExchange) {
        val uri = exchange.requestURI
        val filename = uri.path.substring(1)
        val file = File(filename)
        if (file.exists()) {
            val bytes = Files.readAllBytes(file.toPath())
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            val os = exchange.responseBody
            os.write(bytes)
            os.close()
        } else {
            println("File don't exist.")
            exchange.sendResponseHeaders(404, -1)
        }
        exchange.close()
    }

    private fun handlePost(exchange: HttpExchange) {
        val headers = exchange.requestHeaders
        if (headers.containsKey("Content-Type") && headers["Content-Type"]!![0] == "application/octet-stream") {
            val uri = exchange.requestURI
            val filename = uri.path.substring(1)
            val file = File(filename)
            if (file.exists()) {
                println("File already exists.")
                exchange.sendResponseHeaders(409, -1)
            } else {
                val bytes = exchange.requestBody.readAllBytes()
                Files.write(file.toPath(), bytes)
                exchange.sendResponseHeaders(201, -1)
            }
        } else {
            exchange.sendResponseHeaders(415, -1)
        }
        exchange.close()
    }

    private fun handlePut(exchange: HttpExchange) {
        val headers = exchange.requestHeaders
        if (headers.containsKey("Content-Type") && headers["Content-Type"]!![0] == "application/octet-stream") {
            val uri = exchange.requestURI
            val filename = uri.path.substring(1)
            val file = File(filename)
            if (file.exists()) {
                val bytes = exchange.requestBody.readAllBytes()
                Files.write(file.toPath(), bytes)
                exchange.sendResponseHeaders(200, -1)
            } else {
                println("File don't exists.")
                exchange.sendResponseHeaders(404, -1)
            }
        } else {
            exchange.sendResponseHeaders(415, -1)
        }
        exchange.close()
    }

    private fun handleDelete(exchange: HttpExchange) {
        val uri = exchange.requestURI
        val filename = uri.path.substring(1)
        val file = File(filename)
        if (file.exists()) {
            if (file.delete()) {
                exchange.sendResponseHeaders(204, -1)
            } else {
                exchange.sendResponseHeaders(500, -1)
            }
        } else {
            println("File don't exists.")
            exchange.sendResponseHeaders(404, -1)
        }
        exchange.close()
    }

    private fun handleCopy(exchange: HttpExchange) {
        val headers = exchange.requestHeaders
        if (headers.containsKey("Destination")) {
            val sourceUri = exchange.requestURI
            val sourceFilename = sourceUri.path.substring(1)
            val sourceFile = File(sourceFilename)
            if (sourceFile.exists()) {
                val destinationUri = URI.create(headers["Destination"]!![0])
                val destinationFilename = destinationUri.path.substring(1)
                val destinationFile = File(destinationFilename)
                if (destinationFile.exists()) {
                    exchange.sendResponseHeaders(409, -1)
                } else {
                    Files.copy(sourceFile.toPath(), destinationFile.toPath())
                    exchange.sendResponseHeaders(201, -1)
                }
            } else {
                println("File don't exists.")
                exchange.sendResponseHeaders(404, -1)
            }
        } else {
            exchange.sendResponseHeaders(400, -1)
        }
        exchange.close()
    }

    private fun handleMove(exchange: HttpExchange) {
        val headers = exchange.requestHeaders
        if (headers.containsKey("Destination")) {
            val sourceUri = exchange.requestURI
            val sourceFilename = sourceUri.path.substring(1)
            val sourceFile = File(sourceFilename)
            if (sourceFile.exists()) {
                val destinationUri = URI.create(headers["Destination"]!![0])
                val destinationFilename = destinationUri.path.substring(1)
                val destinationFile = File(destinationFilename)
                if (destinationFile.exists()) {
                    exchange.sendResponseHeaders(409, -1)
                } else {
                    if (sourceFile.renameTo(destinationFile)) {
                        exchange.sendResponseHeaders(201, -1)
                    } else {
                        exchange.sendResponseHeaders(500, -1)
                    }
                }
            } else {
                println("File don't exists.")
                exchange.sendResponseHeaders(404, -1)
            }
        } else {
            exchange.sendResponseHeaders(400, -1)
        }
        exchange.close()
    }
}
