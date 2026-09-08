package com.humsafar.app

import fi.iki.elonen.NanoHTTPD
import java.net.URLEncoder

class LocalReportServer(private val onReport: (String, String, String) -> Unit) : NanoHTTPD(8080) {
    override fun serve(session: IHTTPSession): Response {
        if (session.method == Method.POST && session.uri == "/report") {
            val files = HashMap<String, String>()
            return try {
                session.parseBody(files)
                val params = session.parameters
                val text = params["text"]?.firstOrNull()?.trim().orEmpty()
                val language = params["language"]?.firstOrNull() ?: "en"
                if (text.isNotBlank()) onReport("Browser guest", text, language)
                redirect("/")
            } catch (_: Exception) { newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Invalid report") }
        }
        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", page())
    }

    private fun redirect(path: String) = newFixedLengthResponse(Response.Status.REDIRECT, MIME_HTML, "<a href=\"$path\">Back to Humsafar</a>")
    private fun page() = """
      <!doctype html><meta name=viewport content='width=device-width,initial-scale=1'>
      <style>body{font:16px system-ui;max-width:560px;margin:40px auto;padding:20px;background:#f7f8f3;color:#09292b}textarea,select,button{width:100%;padding:14px;margin:8px 0;font:inherit}button{background:#09292b;color:white;border:0}</style>
      <h1>humsafar</h1><p>Send an offline crowd signal to the event hub.</p>
      <form method=post action=/report><textarea name=text rows=5 placeholder='Gate 3 parking is full' required></textarea>
      <select name=language><option value=en>English</option><option value=hi>Hindi</option><option value=ta>Tamil</option></select><button>Submit signal</button></form>
    """.trimIndent()
}
