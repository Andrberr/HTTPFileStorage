import java.net.HttpURLConnection
import java.net.URL
import java.util.*

const val baseUrl = "http://localhost:8080/"

fun main() {
    val `in` = Scanner(System.`in`)
    var option = ""
    var filename: String
    var content: String
    while (option != "0") {
        println("Enter an option:\n1 - GET\n2 - POST\n3 - PUT\n4 - DELETE\n5 - MOVE\n6 - COPY\n0 - Exit")
        option = `in`.nextLine()
        when (option) {
            "1" -> {
                println("Enter filename:")
                filename = `in`.nextLine()
                get(filename)
            }

            "2" -> {
                println("Enter filename:")
                filename = `in`.nextLine()
                println("Enter content:")
                content = `in`.nextLine()
                post(filename, content)
            }

            "3" -> {
                println("Enter filename:")
                filename = `in`.nextLine()
                println("Enter content:")
                content = `in`.nextLine()
                put(filename, content)
            }

            "4" -> {
                println("Enter filename:")
                filename = `in`.nextLine()
                delete(filename)
            }

            "5" -> {
                println("Enter filename:")
                filename = `in`.nextLine()
                println("Enter new filename:")
                val newFileName = `in`.nextLine()
                move(filename, newFileName)
            }

            "6" -> {
                println("Enter filename:")
                filename = `in`.nextLine()
                copy(filename)
            }

            "0" -> println("Exiting...")
            else -> println("Invalid option")
        }
    }
}

private fun get(fileName: String) {
    // GET request
    val fileUrl = baseUrl + fileName
    val getFileConnection = URL(fileUrl).openConnection() as HttpURLConnection
    getFileConnection.requestMethod = "GET"
    if (getFileConnection.responseCode in 200..299) {
        val content = getFileConnection.inputStream.bufferedReader().readText()
        println("GET response: $content")
    } else {
        println("GET request failed. Response code: ${getFileConnection.responseCode}")
    }
}

private fun post(filename: String, content: String) {
    // POST request
    val postFileUrl = baseUrl + filename
    val postFileBytes = content.toByteArray()
    val postFileConnection = URL(postFileUrl).openConnection() as HttpURLConnection
    postFileConnection.requestMethod = "POST"
    postFileConnection.setRequestProperty("Content-Type", "application/octet-stream")
    postFileConnection.doOutput = true
    postFileConnection.outputStream.write(postFileBytes)
    if (postFileConnection.responseCode in 200..299) {
        println("POST request succeeded. File appended.")
    } else {
        println("POST request failed. Response code: ${postFileConnection.responseCode}")
    }
}

private fun put(filename: String, content: String) {
    // PUT request
    val fileUrl = baseUrl + filename
    val putFileBytes = content.toByteArray()
    val putFileConnection = URL(fileUrl).openConnection() as HttpURLConnection
    putFileConnection.requestMethod = "PUT"
    putFileConnection.setRequestProperty("Content-Type", "application/octet-stream")
    putFileConnection.doOutput = true
    putFileConnection.outputStream.write(putFileBytes)
    if (putFileConnection.responseCode in 200..299) {
        println("PUT request succeeded. File updated.")
    } else {
        println("PUT request failed. Response code: ${putFileConnection.responseCode}")
    }
}

private fun delete(filename: String) {
    // DELETE request
    val fileUrl = baseUrl + filename
    val deleteFileConnection = URL(fileUrl).openConnection() as HttpURLConnection
    deleteFileConnection.requestMethod = "DELETE"
    if (deleteFileConnection.responseCode in 200..299) {
        println("DELETE request succeeded. File deleted.")
    } else {
        println("DELETE request failed. Response code: ${deleteFileConnection.responseCode}")
    }
}

private fun copy(fileName: String) {
    // COPY request
    // GET from 1 file
    val fileUrl = baseUrl + fileName
    val getFileConnection = URL(fileUrl).openConnection() as HttpURLConnection
    getFileConnection.requestMethod = "GET"
    if (getFileConnection.responseCode in 200..299) {
        val content = getFileConnection.inputStream.bufferedReader().readText()
        //Creating copy of file
        val copyFileUrl = baseUrl + getCopyName(fileName)
        val copyFileBytes = content.toByteArray()
        val copyFileConnection = URL(copyFileUrl).openConnection() as HttpURLConnection
        copyFileConnection.requestMethod = "PUT"
        copyFileConnection.setRequestProperty("Content-Type", "application/octet-stream")
        copyFileConnection.doOutput = true
        copyFileConnection.outputStream.write(copyFileBytes)
        if (copyFileConnection.responseCode in 200..299) {
            println("COPY request succeeded. File copied.")
        } else {
            println("COPY request failed. Response code: ${copyFileConnection.responseCode}")
        }
    } else {
        println("COPY request failed. Response code: ${getFileConnection.responseCode}")
    }
}

private fun getCopyName(name: String): String {
    val parts = name.split(".")
    return if (parts.size == 1) "$name-copy"
    else parts[0] + "-copy." + parts[1]
}

private fun move(filename: String, newName: String) {

    //GET info from old file
    val getFileUrl = baseUrl + filename
    val getFileConnection = URL(getFileUrl).openConnection() as HttpURLConnection
    getFileConnection.requestMethod = "GET"
    if (getFileConnection.responseCode in 200..299) {
        val content = getFileConnection.inputStream.bufferedReader().readText()

        // DELETE old file
        val fileUrl = baseUrl + filename
        val deleteFileConnection = URL(fileUrl).openConnection() as HttpURLConnection
        deleteFileConnection.requestMethod = "DELETE"
        if (deleteFileConnection.responseCode in 200..299) {
            //creating new file
            val newFileUrl = baseUrl + newName
            val newFileBytes = content.toByteArray()
            val newFileConnection = URL(newFileUrl).openConnection() as HttpURLConnection
            newFileConnection.requestMethod = "PUT"
            newFileConnection.setRequestProperty("Content-Type", "application/octet-stream")
            newFileConnection.doOutput = true
            newFileConnection.outputStream.write(newFileBytes)
            if (newFileConnection.responseCode in 200..299) {
                println("MOVE request succeeded. File moved.")
            } else {
                println("MOVE request failed. Response code: ${newFileConnection.responseCode}")
            }
        } else {
            println("MOVE request failed. Response code: ${deleteFileConnection.responseCode}")
        }
    } else {
        println("MOVE request failed. Response code: ${getFileConnection.responseCode}")
    }
}


