package com.programmer74.katolk.client.gui

import com.programmer74.katolk.dto.MessageDto
import com.programmer74.katolk.dto.UserInfoDto

fun buildHTML(messages: List<MessageDto>, me: UserInfoDto): String {
  var from: String
  var msg: String

  val historySb = StringBuilder()

  historySb.append("<html>")
  historySb.append("<head>")
  historySb.append("   <script language=\"javascript\" type=\"text/javascript\">")
  historySb.append("       function toBottom(){")
  historySb.append("           window.scrollTo(0, document.body.scrollHeight);")
  historySb.append("       }")
  historySb.append("   </script>")
  historySb.append("<style>")
  historySb.append(
      "body {\n" +
          "    background-color: white;\n" +
          "    color: white;\n" +
          "    font-family: Arial, Helvetica, sans-serif;\n" +
          "    font-size: 11pt;\n" +
          "}\n" +
          "div {\n" +
          "\tmax-width: 70%;\n" +
          "    clear:both;\n" +
          "    padding: 10px; \n" +
          "}\n" +
          ".my {\n" +
          "\tbackground-color: #4B966E;\n" +
          "    text-align: left;\n" +
          "    float:right;\n" +
          "    border-radius: 25px 25px 5px 25px;\n" +
          "}\n" +
          ".his {\n" +
          "\tbackground-color: #477187;\n" +
          "    text-align: left;\n" +
          "    float:left;\n" +
          "    border-radius: 25px 25px 25px 5px;\n" +
          "}" +
          ".unread {\n" +
          "\tbackground-color: #0F5A32;\n" +
          "}")
  historySb.append("</style>")
  historySb.append("</head>")
  historySb.append("<body onload='toBottom()'>")

  var prevUsername = ""
  var shouldIncludeUsername: Boolean

  for (message in messages.asReversed()) {

    from = message.author
    shouldIncludeUsername = false
    if (from != prevUsername) {
      prevUsername = from
      shouldIncludeUsername = true
    }
    msg = message.body

    historySb.append(
        "<div class=\"" + (
            if (message.authorId == me.id) {
              "my"
            } else {
              "his"
            }) + "\">")

    msg = msg.replace("[<]".toRegex(), "&lt;")
    msg = msg.replace("[>]".toRegex(), "&gt;")
    msg = msg.replace("\n|\r\n|\n\r|\r".toRegex(), "<br>")

    if (shouldIncludeUsername) {
      historySb.append("<b>").append(from).append("</b><br>")
    }
    if (!message.wasRead) {
      historySb.append("<span class=\"unread\">")
    }
    historySb.append(msg)
    if (!message.wasRead) {
      historySb.append("</span>\n")
    }

    historySb.append("</div>\n")
  }
  historySb.append("</body>")
  historySb.append("</html>")
  return historySb.toString()
}